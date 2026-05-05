package org.example.invoicemanagement.repository;

import org.example.invoicemanagement.entity.Invoice;
import org.example.invoicemanagement.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByClientId(Long clientId);
    List<Invoice> findByStatus(InvoiceStatus status);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumber(String invoiceNumber);
}
