package org.example.invoicemanagement.controller;

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
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService){

        this.invoiceService = invoiceService;

    }

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO requestDTO){

        InvoiceResponseDTO createdInvoice = invoiceService.createInvoice(requestDTO);

        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceById(@PathVariable  Long id,@Valid @RequestBody InvoiceRequestDTO requestDTO){

        InvoiceResponseDTO updatedInvoice = invoiceService.updateInvoiceById(id,requestDTO);

        return ResponseEntity.ok(updatedInvoice);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices(){

        List<InvoiceResponseDTO> invoices = invoiceService.getAllInvoices();

        return ResponseEntity.ok(invoices);
    }

    @GetMapping("{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id){

        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id);

        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByClientId(@PathVariable Long clientId){

        List<InvoiceResponseDTO> invoicesByClientId = invoiceService.getInvoicesByClientId(clientId);

        return ResponseEntity.ok(invoicesByClientId);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByStatus(@PathVariable InvoiceStatus status){

        List<InvoiceResponseDTO> invoiceByStatus = invoiceService.getInvoicesByStatus(status);

        return ResponseEntity.ok(invoiceByStatus);

    }

    @PutMapping("/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatusById(@PathVariable Long id, @RequestParam InvoiceStatus status){

        InvoiceResponseDTO updatedInvoiceStatus = invoiceService.updateInvoiceStatusById(id, status);

        return ResponseEntity.ok(updatedInvoiceStatus);
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<InvoiceResponseDTO> updateInvoicePaymentById(@PathVariable Long id, @RequestParam BigDecimal amountPaid){

        InvoiceResponseDTO updatedInvoice = invoiceService.updateInvoicePaymentById(id,amountPaid);

        return ResponseEntity.ok(updatedInvoice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoiceById(@PathVariable Long id){

        invoiceService.deleteInvoiceById(id);

        return ResponseEntity.noContent().build();
    }

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
