package org.example.invoicemanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceRequestDTO {


    @NotNull(message = "Client id is required")
    private Long clientId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date must be today or a future date")
    private LocalDate dueDate;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Tax Rate should be at most 100.00")
    private BigDecimal taxRate;

    @NotNull(message = "Discount rate is required")
    @DecimalMin(value = "0.00", message = "Discount rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Discount Rate should be at most 100.00")
    private BigDecimal discountRate;

    @Valid
    @NotEmpty(message = "Invoice must have at least one item")
    private List<InvoiceItemRequestDTO> items;

    public InvoiceRequestDTO(){

    }

    public InvoiceRequestDTO(

            Long clientId,
            LocalDate issueDate,
            LocalDate dueDate,
            BigDecimal taxRate,
            BigDecimal discountRate,
            List<InvoiceItemRequestDTO> items
    ){

        this.clientId = clientId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.taxRate = taxRate;
        this.discountRate = discountRate;
        this.items = items;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public List<InvoiceItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemRequestDTO> items) {
        this.items = items;
    }
}
