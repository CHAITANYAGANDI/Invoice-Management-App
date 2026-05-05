package org.example.invoicemanagement.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class InvoiceItemRequestDTO {


    @NotBlank(message = "Item description is required")
    @Size(max = 255, message = "Item description must be at most 255 characters")
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    public InvoiceItemRequestDTO(){

    }

    public InvoiceItemRequestDTO(String description, Integer quantity, BigDecimal unitPrice){

        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
