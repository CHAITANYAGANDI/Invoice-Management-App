package org.example.invoicemanagement.dto;

import java.time.LocalDateTime;

public class ClientResponseDTO {


    private Long id;
    private String name;
    private String email;
    private String phone;
    private String billingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public ClientResponseDTO(
            Long id,
            String name,
            String email,
            String phone,
            String billingAddress,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ){

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.billingAddress = billingAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
