package org.example.invoicemanagement.dto;

import org.example.invoicemanagement.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceResponseDTO {

    private Long id;
    private String invoiceNumber;
    private Long clientId;
    private String clientName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private BigDecimal subTotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private List<InvoiceItemResponseDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InvoiceResponseDTO(
            Long id,
            String invoiceNumber,
            Long clientId,
            String clientName,
            LocalDate issueDate,
            LocalDate dueDate,
            InvoiceStatus status,
            BigDecimal subTotal,
            BigDecimal taxRate,
            BigDecimal taxAmount,
            BigDecimal discountRate,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            BigDecimal amountPaid,
            BigDecimal balanceDue,
            List<InvoiceItemResponseDTO> items,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ){

        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.clientId = clientId;
        this.clientName = clientName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.subTotal = subTotal;
        this.taxRate = taxRate;
        this.taxAmount = taxAmount;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.balanceDue = balanceDue;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getBalanceDue() {
        return balanceDue;
    }

    public List<InvoiceItemResponseDTO> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
