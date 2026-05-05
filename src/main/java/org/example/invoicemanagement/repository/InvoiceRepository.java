package org.example.invoicemanagement.repository;

import org.example.invoicemanagement.entity.Invoice;
import org.example.invoicemanagement.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    List<Invoice> findByClientId(Long clientId);
    List<Invoice> findByStatus(InvoiceStatus status);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumber(String invoiceNumber);
}
