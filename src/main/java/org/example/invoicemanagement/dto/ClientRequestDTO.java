package org.example.invoicemanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClientRequestDTO {

    @NotBlank(message = "Client name is required")
    @Size(max = 100,message = "Client name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phone;

    @Size(max = 255, message = "Billing address must be at most 255 characters")
    private String billingAddress;

    public ClientRequestDTO(){

    }

    public ClientRequestDTO(String name, String email, String phone, String billingAddress){

        this.name = name;
        this.email = email;
        this.phone = phone;
        this.billingAddress = billingAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
