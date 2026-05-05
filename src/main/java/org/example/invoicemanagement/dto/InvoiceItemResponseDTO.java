package org.example.invoicemanagement.dto;

import java.math.BigDecimal;

public class InvoiceItemResponseDTO {

    private Long id;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public InvoiceItemResponseDTO(Long id, String description,Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal){

        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
