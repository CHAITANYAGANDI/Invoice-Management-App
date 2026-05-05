package org.example.invoicemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.invoicemanagement.dto.InvoiceItemRequestDTO;
import org.example.invoicemanagement.dto.InvoiceItemResponseDTO;
import org.example.invoicemanagement.dto.InvoiceRequestDTO;
import org.example.invoicemanagement.dto.InvoiceResponseDTO;
import org.example.invoicemanagement.enums.InvoiceStatus;
import org.example.invoicemanagement.exception.GlobalExceptionHandler;
import org.example.invoicemanagement.exception.InvoiceNotFoundException;
import org.example.invoicemanagement.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@Import(GlobalExceptionHandler.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceService invoiceService;

    @Test
    void createInvoice_WhenValidRequest_ShouldReturnCreatedInvoice() throws Exception {
        InvoiceRequestDTO requestDTO = createInvoiceRequestDTO();

        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.DRAFT,
                new BigDecimal("0.00"),
                new BigDecimal("113.00")
        );

        when(invoiceService.createInvoice(any(InvoiceRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNumber").value("INV-C1-20260505-ABC12345"))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.clientName").value("Sai Technologies"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.subTotal").value(100.00))
                .andExpect(jsonPath("$.taxAmount").value(13.00))
                .andExpect(jsonPath("$.totalAmount").value(113.00))
                .andExpect(jsonPath("$.balanceDue").value(113.00))
                .andExpect(jsonPath("$.items[0].description").value("Website development"));

        verify(invoiceService, times(1)).createInvoice(any(InvoiceRequestDTO.class));
    }

    @Test
    void createInvoice_WhenItemsAreEmpty_ShouldReturnBadRequest() throws Exception {
        InvoiceRequestDTO requestDTO = new InvoiceRequestDTO(
                1L,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20),
                new BigDecimal("13.00"),
                new BigDecimal("0.00"),
                List.of()
        );

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.items").value("Invoice must have at least one item"));

        verify(invoiceService, never()).createInvoice(any(InvoiceRequestDTO.class));
    }

    @Test
    void getInvoiceById_WhenInvoiceExists_ShouldReturnInvoice() throws Exception {
        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.DRAFT,
                new BigDecimal("0.00"),
                new BigDecimal("113.00")
        );

        when(invoiceService.getInvoiceById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/invoices/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNumber").value("INV-C1-20260505-ABC12345"))
                .andExpect(jsonPath("$.clientName").value("Sai Technologies"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(invoiceService, times(1)).getInvoiceById(1L);
    }

    @Test
    void getInvoiceById_WhenInvoiceDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(invoiceService.getInvoiceById(100L))
                .thenThrow(new InvoiceNotFoundException("Invoice not found with id: 100"));

        mockMvc.perform(get("/api/v1/invoices/{id}", 100L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Invoice not found with id: 100"))
                .andExpect(jsonPath("$.path").value("/api/v1/invoices/100"));

        verify(invoiceService, times(1)).getInvoiceById(100L);
    }

    @Test
    void updateInvoice_WhenValidRequest_ShouldReturnUpdatedInvoice() throws Exception {
        InvoiceRequestDTO requestDTO = createInvoiceRequestDTO();

        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.DRAFT,
                new BigDecimal("0.00"),
                new BigDecimal("113.00")
        );

        when(invoiceService.updateInvoiceById(eq(1L), any(InvoiceRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/invoices/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalAmount").value(113.00))
                .andExpect(jsonPath("$.balanceDue").value(113.00));

        verify(invoiceService, times(1))
                .updateInvoiceById(eq(1L), any(InvoiceRequestDTO.class));
    }

    @Test
    void getAllInvoices_ShouldReturnInvoices() throws Exception {
        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.DRAFT,
                new BigDecimal("0.00"),
                new BigDecimal("113.00")
        );

        when(invoiceService.getAllInvoices()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-C1-20260505-ABC12345"));

        verify(invoiceService, times(1)).getAllInvoices();
    }

    @Test
    void getInvoicesByClientId_ShouldReturnInvoices() throws Exception {
        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.DRAFT,
                new BigDecimal("0.00"),
                new BigDecimal("113.00")
        );

        when(invoiceService.getInvoicesByClientId(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/invoices/client/{clientId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(1))
                .andExpect(jsonPath("$[0].clientName").value("Sai Technologies"));

        verify(invoiceService, times(1)).getInvoicesByClientId(1L);
    }

    @Test
    void getInvoicesByStatus_ShouldReturnInvoices() throws Exception {
        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.PAID,
                new BigDecimal("113.00"),
                new BigDecimal("0.00")
        );

        when(invoiceService.getInvoicesByStatus(InvoiceStatus.PAID))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/invoices/status/{status}", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PAID"));

        verify(invoiceService, times(1)).getInvoicesByStatus(InvoiceStatus.PAID);
    }

    @Test
    void updateInvoiceStatus_WhenValidRequest_ShouldReturnUpdatedInvoice() throws Exception {
        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.SENT,
                new BigDecimal("0.00"),
                new BigDecimal("113.00")
        );

        when(invoiceService.updateInvoiceStatusById(1L, InvoiceStatus.SENT))
                .thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/invoices/{id}/status", 1L)
                        .param("status", "SENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(invoiceService, times(1))
                .updateInvoiceStatusById(1L, InvoiceStatus.SENT);
    }

    @Test
    void updateInvoicePayment_WhenPartialPayment_ShouldReturnPartiallyPaidInvoice() throws Exception {
        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.PARTIALLY_PAID,
                new BigDecimal("50.00"),
                new BigDecimal("63.00")
        );

        when(invoiceService.updateInvoicePaymentById(1L, new BigDecimal("50.00")))
                .thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/invoices/{id}/payment", 1L)
                        .param("amountPaid", "50.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountPaid").value(50.00))
                .andExpect(jsonPath("$.balanceDue").value(63.00))
                .andExpect(jsonPath("$.status").value("PARTIALLY_PAID"));

        verify(invoiceService, times(1))
                .updateInvoicePaymentById(1L, new BigDecimal("50.00"));
    }

    @Test
    void filterInvoices_WhenValidParams_ShouldReturnPagedInvoices() throws Exception {
        InvoiceResponseDTO responseDTO = createInvoiceResponseDTO(
                1L,
                InvoiceStatus.PAID,
                new BigDecimal("113.00"),
                new BigDecimal("0.00")
        );

        Page<InvoiceResponseDTO> page = new PageImpl<>(List.of(responseDTO));

        when(invoiceService.filterInvoices(
                eq(InvoiceStatus.PAID),
                eq(1L),
                eq(LocalDate.of(2026, 5, 1)),
                eq(LocalDate.of(2026, 5, 31)),
                eq(0),
                eq(5),
                eq("dueDate"),
                eq("desc")
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/invoices/filter")
                        .param("status", "PAID")
                        .param("clientId", "1")
                        .param("fromDate", "2026-05-01")
                        .param("toDate", "2026-05-31")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "dueDate")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PAID"))
                .andExpect(jsonPath("$.content[0].balanceDue").value(0.00));

        verify(invoiceService, times(1)).filterInvoices(
                InvoiceStatus.PAID,
                1L,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                0,
                5,
                "dueDate",
                "desc"
        );
    }

    @Test
    void deleteInvoice_WhenInvoiceExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(invoiceService).deleteInvoiceById(1L);

        mockMvc.perform(delete("/api/v1/invoices/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(invoiceService, times(1)).deleteInvoiceById(1L);
    }

    private InvoiceRequestDTO createInvoiceRequestDTO() {
        return new InvoiceRequestDTO(
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
    }

    private InvoiceResponseDTO createInvoiceResponseDTO(
            Long id,
            InvoiceStatus status,
            BigDecimal amountPaid,
            BigDecimal balanceDue
    ) {
        return new InvoiceResponseDTO(
                id,
                "INV-C1-20260505-ABC12345",
                1L,
                "Sai Technologies",
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20),
                status,
                new BigDecimal("100.00"),
                new BigDecimal("13.00"),
                new BigDecimal("13.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("113.00"),
                amountPaid,
                balanceDue,
                List.of(
                        new InvoiceItemResponseDTO(
                                1L,
                                "Website development",
                                1,
                                new BigDecimal("100.00"),
                                new BigDecimal("100.00")
                        )
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}