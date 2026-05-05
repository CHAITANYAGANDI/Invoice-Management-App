package org.example.invoicemanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.invoicemanagement.dto.InvoiceRequestDTO;
import org.example.invoicemanagement.dto.InvoiceResponseDTO;
import org.example.invoicemanagement.enums.InvoiceStatus;
import org.example.invoicemanagement.service.InvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoices", description = "APIs for creating, updating, filtering, payment tracking, and managing invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService){

        this.invoiceService = invoiceService;

    }

    @Operation(summary = "Create a new invoice")
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO requestDTO){

        InvoiceResponseDTO createdInvoice = invoiceService.createInvoice(requestDTO);

        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }


    @Operation(summary = "Update invoice by ID")
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceById(@PathVariable  Long id,@Valid @RequestBody InvoiceRequestDTO requestDTO){

        InvoiceResponseDTO updatedInvoice = invoiceService.updateInvoiceById(id,requestDTO);

        return ResponseEntity.ok(updatedInvoice);
    }

    @Operation(summary = "Get all invoices")
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices(){

        List<InvoiceResponseDTO> invoices = invoiceService.getAllInvoices();

        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoice by ID")
    @GetMapping("{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id){

        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id);

        return ResponseEntity.ok(invoice);
    }

    @Operation(summary = "Get invoices by client ID")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByClientId(@PathVariable Long clientId){

        List<InvoiceResponseDTO> invoicesByClientId = invoiceService.getInvoicesByClientId(clientId);

        return ResponseEntity.ok(invoicesByClientId);
    }

    @Operation(summary = "Get invoices by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByStatus(@PathVariable InvoiceStatus status){

        List<InvoiceResponseDTO> invoiceByStatus = invoiceService.getInvoicesByStatus(status);

        return ResponseEntity.ok(invoiceByStatus);

    }

    @Operation(summary = "Update invoice status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatusById(@PathVariable Long id, @RequestParam InvoiceStatus status){

        InvoiceResponseDTO updatedInvoiceStatus = invoiceService.updateInvoiceStatusById(id, status);

        return ResponseEntity.ok(updatedInvoiceStatus);
    }

    @Operation(summary = "Update invoice payment amount")
    @PatchMapping("/{id}/payment")
    public ResponseEntity<InvoiceResponseDTO> updateInvoicePaymentById(@PathVariable Long id, @RequestParam BigDecimal amountPaid){

        InvoiceResponseDTO updatedInvoice = invoiceService.updateInvoicePaymentById(id,amountPaid);

        return ResponseEntity.ok(updatedInvoice);
    }

    @Operation(summary = "Delete invoice by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoiceById(@PathVariable Long id){

        invoiceService.deleteInvoiceById(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Filter invoices with pagination and sorting")
    @GetMapping("/filter")
    public ResponseEntity<Page<InvoiceResponseDTO>> filterInvoices(

            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir){


        Page<InvoiceResponseDTO> filteredInvoices = invoiceService.filterInvoices(
                status,clientId,fromDate,toDate,page,size,sortBy,sortDir);

        return ResponseEntity.ok(filteredInvoices);
    }

}
