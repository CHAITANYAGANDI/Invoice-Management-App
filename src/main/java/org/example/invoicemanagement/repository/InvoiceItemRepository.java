package org.example.invoicemanagement.repository;

import org.example.invoicemanagement.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem,Long> {


}
