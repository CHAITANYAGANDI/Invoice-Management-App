package org.example.invoicemanagement;

import org.springframework.boot.SpringApplication;

public class TestInvoiceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.from(InvoiceManagementApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
