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
import org.example.invoicemanagement.specification.InvoiceSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(

            "id",
            "invoiceNumber",
            "issueDate",
            "dueDate",
            "status",
            "totalAmount",
            "balanceDue",
            "createdAt"
    );

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

    private void validateSortField(String sortBy){

        if(sortBy == null || sortBy.isBlank()){

            throw new IllegalArgumentException("Sort field cannot be empty");
        }

        if(!ALLOWED_SORT_FIELDS.contains(sortBy)){

            throw new IllegalArgumentException("Invalid sort field: "+ sortBy + ". Allowed fields are: " + ALLOWED_SORT_FIELDS);

        }
    }

    private void validateSortDirection(String sortDir){

        if(sortDir == null || sortDir.isBlank()){

            throw new IllegalArgumentException("Sort direction cannot be empty");
        }

        if(!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")){

            throw new IllegalArgumentException("Invalid sort direction: " + sortDir + ". Allowed values are: asc, desc");
        }
    }

    private void validateFromAndToDate(Long clientId, LocalDate fromDate, LocalDate toDate){

        if ((fromDate == null && toDate != null) || (fromDate != null && toDate == null)) {
            throw new IllegalArgumentException("Both fromDate and toDate must be provided together");
        }

        if (fromDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be equal to or before toDate");
        }

        if (clientId != null && !clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException("Client not found with id: " + clientId);
        }

    }

    private Sort sortData(String sortDir, String sortBy){

        Sort sort;

        if (sortDir.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }

        return sort;

    }

    private Specification<Invoice> buildSpecifiedData(InvoiceStatus status, Long clientId, LocalDate fromDate, LocalDate toDate){

        Specification<Invoice> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();

        if (status != null) {
            spec = spec.and(InvoiceSpecification.hasStatus(status));
        }

        if (clientId != null) {
            spec = spec.and(InvoiceSpecification.hasClientId(clientId));
        }

        if (fromDate != null) {
            spec = spec.and(InvoiceSpecification.issueDateBetween(fromDate, toDate));
        }

        return spec;
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


    @Transactional(readOnly = true)
    public Page<InvoiceResponseDTO> filterInvoices(

            InvoiceStatus status,
            Long clientId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size,
            String sortBy,
            String sortDir

    ){

        validateSortField(sortBy);
        validateSortDirection(sortDir);
        validateFromAndToDate(clientId,fromDate,toDate);

        Sort sort = sortData(sortDir,sortBy);

        Specification<Invoice> spec = buildSpecifiedData(status,clientId,fromDate,toDate);

        Pageable pageable = PageRequest.of(page,size,sort);

        Page<Invoice> invoice = invoiceRepository.findAll(spec,pageable);

        return invoice.map(this::convertToResponseDTO);

    }



}
