package org.example.invoicemanagement.service;

import org.example.invoicemanagement.dto.InvoiceItemRequestDTO;
import org.example.invoicemanagement.dto.InvoiceItemResponseDTO;
import org.example.invoicemanagement.dto.InvoiceRequestDTO;
import org.example.invoicemanagement.dto.InvoiceResponseDTO;
import org.example.invoicemanagement.entity.Client;
import org.example.invoicemanagement.entity.Invoice;
import org.example.invoicemanagement.entity.InvoiceItem;
import org.example.invoicemanagement.enums.InvoiceStatus;
import org.example.invoicemanagement.exception.ClientNotFoundException;
import org.example.invoicemanagement.exception.InvoiceNotFoundException;
import org.example.invoicemanagement.repository.ClientRepository;
import org.example.invoicemanagement.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, ClientRepository clientRepository){

        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
    }

    private InvoiceResponseDTO convertToResponseDTO(Invoice invoice){

        List<InvoiceItemResponseDTO> itemResponseDTOS = invoice.getItems()
                .stream()
                .map(this::convertToItemResponseDTO)
                .collect(Collectors.toList());

        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getClient().getId(),
                invoice.getClient().getName(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getSubTotal(),
                invoice.getTaxRate(),
                invoice.getTaxAmount(),
                invoice.getDiscountRate(),
                invoice.getDiscountAmount(),
                invoice.getTotalAmount(),
                invoice.getAmountPaid(),
                invoice.getBalanceDue(),
                itemResponseDTOS,
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    private String generateInvoiceNumber(Long clientId){

        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        String invoiceNumber;

        do{

            String randomPart = UUID.randomUUID()
                    .toString()
                    .substring(0,8)
                    .toUpperCase();

            invoiceNumber = "INV-C" + clientId + "-" + datePart + "-" + randomPart;

        }while (invoiceRepository.existsByInvoiceNumber((invoiceNumber)));

        return invoiceNumber;
    }

    private InvoiceItem convertToInvoiceItem(InvoiceItemRequestDTO itemRequestDTO){

        BigDecimal lineTotal = itemRequestDTO.getUnitPrice()
                .multiply(BigDecimal.valueOf(itemRequestDTO.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        return new InvoiceItem(
                itemRequestDTO.getDescription(),
                itemRequestDTO.getQuantity(),
                itemRequestDTO.getUnitPrice(),
                lineTotal
        );
    }

    private InvoiceItemResponseDTO convertToItemResponseDTO(InvoiceItem item){

        return new InvoiceItemResponseDTO(
                item.getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    private BigDecimal calculateSubtotal(List<InvoiceItem> invoiceItems){

        return invoiceItems.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage){

        return amount
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
    }

    private void updatePaymentAndStatus(Invoice invoice, BigDecimal amountPaid) {

        if (amountPaid == null) {
            throw new IllegalArgumentException("Amount paid is required");
        }

        BigDecimal normalizedAmountPaid = amountPaid.setScale(2, RoundingMode.HALF_UP);

        if (normalizedAmountPaid.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount paid cannot be negative");
        }

        if (normalizedAmountPaid.compareTo(invoice.getTotalAmount()) > 0) {
            throw new IllegalArgumentException("Amount paid cannot be greater than total amount");
        }

        BigDecimal balanceDue = invoice.getTotalAmount()
                .subtract(normalizedAmountPaid)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        invoice.setAmountPaid(normalizedAmountPaid);
        invoice.setBalanceDue(balanceDue);

        if (normalizedAmountPaid.compareTo(BigDecimal.ZERO) == 0) {
            if (invoice.getStatus() == InvoiceStatus.PAID ||
                    invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID) {
                invoice.setStatus(InvoiceStatus.SENT);
            }
        } else if (normalizedAmountPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
    }

    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO requestDTO){

        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(()-> new ClientNotFoundException(
                        "Client not found with id: "+ requestDTO.getClientId()
                ));

        if(requestDTO.getDueDate().isBefore(requestDTO.getIssueDate())){

            throw new IllegalArgumentException("Due date must be equal to or after issue date");
        }

        String invoiceNumber = generateInvoiceNumber(client.getId());

        List<InvoiceItem> invoiceItems = requestDTO.getItems()
                .stream()
                .map(this::convertToInvoiceItem)
                .collect(Collectors.toList());

        BigDecimal subTotal = calculateSubtotal(invoiceItems);

        BigDecimal taxAmount = calculatePercentage(subTotal,requestDTO.getTaxRate());

        BigDecimal discountAmount = calculatePercentage(subTotal,requestDTO.getDiscountRate());

        BigDecimal totalAmount = subTotal
                .add(taxAmount)
                .subtract(discountAmount)
                .setScale(2,RoundingMode.HALF_UP);

        BigDecimal amountPaid = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal balanceDue = totalAmount.subtract(amountPaid)
                .setScale(2,RoundingMode.HALF_UP);

        Invoice invoice = new Invoice(
                invoiceNumber,
                client,
                requestDTO.getIssueDate(),
                requestDTO.getDueDate(),
                InvoiceStatus.DRAFT,
                subTotal,
                requestDTO.getTaxRate(),
                taxAmount,
                requestDTO.getDiscountRate(),
                discountAmount,
                totalAmount,
                amountPaid,
                balanceDue
        );

        for(InvoiceItem item: invoiceItems){

            invoice.addItem(item);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return convertToResponseDTO(savedInvoice);

    }

    @Transactional
    public InvoiceResponseDTO updateInvoiceById(Long id, InvoiceRequestDTO requestDTO){

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(()-> new InvoiceNotFoundException("Invoice not found with id: "+id));

        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(()-> new ClientNotFoundException(
                        "Client not found with id: "+ requestDTO.getClientId()
                ));

        if(requestDTO.getDueDate().isBefore(requestDTO.getIssueDate())){

            throw new IllegalArgumentException("Due date must be equal to or after issue date");
        }


        invoice.clearItems();

        List<InvoiceItem> invoiceItems = requestDTO.getItems()
                .stream()
                .map(this::convertToInvoiceItem)
                .collect(Collectors.toList());

        BigDecimal subTotal = calculateSubtotal(invoiceItems);

        BigDecimal taxAmount = calculatePercentage(subTotal,requestDTO.getTaxRate());

        BigDecimal discountAmount = calculatePercentage(subTotal,requestDTO.getDiscountRate());

        BigDecimal totalAmount = subTotal
                .add(taxAmount)
                .subtract(discountAmount)
                .setScale(2,RoundingMode.HALF_UP);

        invoice.setClient(client);
        invoice.setIssueDate(requestDTO.getIssueDate());
        invoice.setDueDate(requestDTO.getDueDate());
        invoice.setSubTotal(subTotal);
        invoice.setTaxRate(requestDTO.getTaxRate());
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscountRate(requestDTO.getDiscountRate());
        invoice.setDiscountAmount(discountAmount);
        invoice.setTotalAmount(totalAmount);

        updatePaymentAndStatus(invoice,invoice.getAmountPaid());

        for(InvoiceItem item: invoiceItems){

            invoice.addItem(item);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return convertToResponseDTO(savedInvoice);


    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getAllInvoices(){

        return invoiceRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceById(Long id){

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with id:"+id));


        return convertToResponseDTO(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByClientId(Long clientId){

        return invoiceRepository.findByClientId(clientId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByStatus(InvoiceStatus status){

        return invoiceRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

    }

    @Transactional
    public InvoiceResponseDTO updateInvoiceStatusById(Long id, InvoiceStatus status){

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(()-> new InvoiceNotFoundException("Invoice not found with id: "+id));

        invoice.setStatus(status);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return convertToResponseDTO(savedInvoice);
    }

    @Transactional
    public void deleteInvoiceById(Long id){

        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(()-> new InvoiceNotFoundException("Issue not found with id: "+id));

        invoiceRepository.delete(existingInvoice);

    }

    @Transactional
    public InvoiceResponseDTO updateInvoicePaymentById(Long id, BigDecimal amountPaid){

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(()-> new InvoiceNotFoundException("Invoice not found with id: "+id));

        updatePaymentAndStatus(invoice, amountPaid);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return convertToResponseDTO(savedInvoice);

    }




}
