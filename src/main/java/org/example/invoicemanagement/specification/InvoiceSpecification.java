package org.example.invoicemanagement.specification;

import org.example.invoicemanagement.entity.Invoice;
import org.example.invoicemanagement.enums.InvoiceStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class InvoiceSpecification {

    public static Specification<Invoice> hasStatus (InvoiceStatus status){

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"),status);

    }

    public static Specification<Invoice> hasClientId(Long clientId){

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("client").get("id"),clientId);
    }

    public static Specification<Invoice> issueDateBetween(LocalDate fromDate, LocalDate toDate){

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.between(root.get("issueDate"), fromDate, toDate);
    }
}
