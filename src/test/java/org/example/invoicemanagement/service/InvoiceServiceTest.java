package org.example.invoicemanagement.service;

import org.example.invoicemanagement.dto.InvoiceItemRequestDTO;
import org.example.invoicemanagement.dto.InvoiceRequestDTO;
import org.example.invoicemanagement.dto.InvoiceResponseDTO;
import org.example.invoicemanagement.entity.Client;
import org.example.invoicemanagement.entity.Invoice;
import org.example.invoicemanagement.entity.InvoiceItem;
import org.example.invoicemanagement.enums.InvoiceStatus;
import org.example.invoicemanagement.exception.InvoiceNotFoundException;
import org.example.invoicemanagement.repository.ClientRepository;
import org.example.invoicemanagement.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void createInvoice_WhenValidRequest_ShouldCalculateTotalsAndReturnInvoiceResponseDTO() {
        Client client = createClient();

        InvoiceRequestDTO requestDTO = new InvoiceRequestDTO(
                1L,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20),
                new BigDecimal("13.00"),
                new BigDecimal("0.00"),
                List.of(
                        new InvoiceItemRequestDTO(
                                "Website development",
                                1,
                                new BigDecimal("100.00")
                        )
                )
        );

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(invoiceRepository.existsByInvoiceNumber(anyString())).thenReturn(false);

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(1L);

            Long itemId = 1L;
            for (InvoiceItem item : invoice.getItems()) {
                item.setId(itemId++);
            }

            return invoice;
        });

        InvoiceResponseDTO responseDTO = invoiceService.createInvoice(requestDTO);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(1L, responseDTO.getClientId());
        assertEquals("Sai Technologies", responseDTO.getClientName());
        assertEquals(InvoiceStatus.DRAFT, responseDTO.getStatus());

        assertEquals(new BigDecimal("100.00"), responseDTO.getSubTotal());
        assertEquals(new BigDecimal("13.00"), responseDTO.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), responseDTO.getDiscountAmount());
        assertEquals(new BigDecimal("113.00"), responseDTO.getTotalAmount());
        assertEquals(new BigDecimal("0.00"), responseDTO.getAmountPaid());
        assertEquals(new BigDecimal("113.00"), responseDTO.getBalanceDue());

        assertEquals(1, responseDTO.getItems().size());
        assertEquals("Website development", responseDTO.getItems().get(0).getDescription());
        assertEquals(new BigDecimal("100.00"), responseDTO.getItems().get(0).getLineTotal());

        verify(clientRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void getInvoiceById_WhenInvoiceExists_ShouldReturnInvoiceResponseDTO() {
        Invoice invoice = createInvoice();
        invoice.setId(1L);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        InvoiceResponseDTO responseDTO = invoiceService.getInvoiceById(1L);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("Sai Technologies", responseDTO.getClientName());
        assertEquals(InvoiceStatus.DRAFT, responseDTO.getStatus());
        assertEquals(new BigDecimal("113.00"), responseDTO.getTotalAmount());

        verify(invoiceRepository, times(1)).findById(1L);
    }

    @Test
    void getInvoiceById_WhenInvoiceDoesNotExist_ShouldThrowInvoiceNotFoundException() {
        when(invoiceRepository.findById(100L)).thenReturn(Optional.empty());

        InvoiceNotFoundException exception = assertThrows(
                InvoiceNotFoundException.class,
                () -> invoiceService.getInvoiceById(100L)
        );

        assertEquals("Invoice not found with id: 100", exception.getMessage());

        verify(invoiceRepository, times(1)).findById(100L);
    }

    @Test
    void updateInvoicePaymentById_WhenPartialPayment_ShouldSetPartiallyPaidStatus() {
        Invoice invoice = createInvoice();
        invoice.setId(1L);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO responseDTO =
                invoiceService.updateInvoicePaymentById(1L, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), responseDTO.getAmountPaid());
        assertEquals(new BigDecimal("63.00"), responseDTO.getBalanceDue());
        assertEquals(InvoiceStatus.PARTIALLY_PAID, responseDTO.getStatus());

        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void updateInvoicePaymentById_WhenFullPayment_ShouldSetPaidStatus() {
        Invoice invoice = createInvoice();
        invoice.setId(1L);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO responseDTO =
                invoiceService.updateInvoicePaymentById(1L, new BigDecimal("113.00"));

        assertEquals(new BigDecimal("113.00"), responseDTO.getAmountPaid());
        assertEquals(new BigDecimal("0.00"), responseDTO.getBalanceDue());
        assertEquals(InvoiceStatus.PAID, responseDTO.getStatus());

        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void updateInvoicePaymentById_WhenAmountPaidIsGreaterThanTotal_ShouldThrowIllegalArgumentException() {
        Invoice invoice = createInvoice();
        invoice.setId(1L);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.updateInvoicePaymentById(1L, new BigDecimal("200.00"))
        );

        assertEquals("Amount paid cannot be greater than total amount", exception.getMessage());

        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    private Client createClient() {
        Client client = new Client(
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );
        client.setId(1L);
        return client;
    }

    private Invoice createInvoice() {
        Client client = createClient();

        Invoice invoice = new Invoice(
                "INV-C1-20260505-ABC12345",
                client,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20),
                InvoiceStatus.DRAFT,
                new BigDecimal("100.00"),
                new BigDecimal("13.00"),
                new BigDecimal("13.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("113.00"),
                new BigDecimal("0.00"),
                new BigDecimal("113.00")
        );

        InvoiceItem item = new InvoiceItem(
                "Website development",
                1,
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
        );
        item.setId(1L);

        invoice.addItem(item);

        return invoice;
    }
}