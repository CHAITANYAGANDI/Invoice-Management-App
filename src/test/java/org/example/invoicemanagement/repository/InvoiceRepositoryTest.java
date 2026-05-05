package org.example.invoicemanagement.repository;

import org.example.invoicemanagement.entity.Client;
import org.example.invoicemanagement.entity.Invoice;
import org.example.invoicemanagement.entity.InvoiceItem;
import org.example.invoicemanagement.enums.InvoiceStatus;
import org.example.invoicemanagement.specification.InvoiceSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void save_WhenInvoiceHasItems_ShouldPersistInvoiceAndItems() {
        Client client = saveClient("Sai Technologies", "sai@example.com");

        Invoice invoice = createInvoice(
                "INV-C1-20260505-ABC12345",
                client,
                InvoiceStatus.DRAFT,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20)
        );

        Invoice savedInvoice = invoiceRepository.save(invoice);

        assertThat(savedInvoice.getId()).isNotNull();
        assertThat(savedInvoice.getItems()).hasSize(1);
        assertThat(savedInvoice.getItems().get(0).getId()).isNotNull();
        assertThat(savedInvoice.getClient().getName()).isEqualTo("Sai Technologies");
    }

    @Test
    void findByClientId_WhenClientHasInvoices_ShouldReturnInvoices() {
        Client client = saveClient("Sai Technologies", "sai@example.com");

        Invoice invoice1 = createInvoice(
                "INV-C1-20260505-ABC12345",
                client,
                InvoiceStatus.DRAFT,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20)
        );

        Invoice invoice2 = createInvoice(
                "INV-C1-20260506-XYZ12345",
                client,
                InvoiceStatus.PAID,
                LocalDate.of(2026, 5, 6),
                LocalDate.of(2026, 5, 25)
        );

        invoiceRepository.saveAll(List.of(invoice1, invoice2));

        List<Invoice> result = invoiceRepository.findByClientId(client.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Invoice::getClient)
                .extracting(Client::getId)
                .containsOnly(client.getId());
    }

    @Test
    void findByStatus_WhenStatusMatches_ShouldReturnInvoices() {
        Client client = saveClient("Sai Technologies", "sai@example.com");

        Invoice draftInvoice = createInvoice(
                "INV-C1-20260505-ABC12345",
                client,
                InvoiceStatus.DRAFT,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20)
        );

        Invoice paidInvoice = createInvoice(
                "INV-C1-20260506-XYZ12345",
                client,
                InvoiceStatus.PAID,
                LocalDate.of(2026, 5, 6),
                LocalDate.of(2026, 5, 25)
        );

        invoiceRepository.saveAll(List.of(draftInvoice, paidInvoice));

        List<Invoice> result = invoiceRepository.findByStatus(InvoiceStatus.PAID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-C1-20260506-XYZ12345");
    }

    @Test
    void findByInvoiceNumber_WhenInvoiceExists_ShouldReturnInvoice() {
        Client client = saveClient("Sai Technologies", "sai@example.com");

        Invoice invoice = createInvoice(
                "INV-C1-20260505-ABC12345",
                client,
                InvoiceStatus.DRAFT,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20)
        );

        invoiceRepository.save(invoice);

        Optional<Invoice> result =
                invoiceRepository.findByInvoiceNumber("INV-C1-20260505-ABC12345");

        assertThat(result).isPresent();
        assertThat(result.get().getInvoiceNumber()).isEqualTo("INV-C1-20260505-ABC12345");
    }

    @Test
    void existsByInvoiceNumber_WhenInvoiceExists_ShouldReturnTrue() {
        Client client = saveClient("Sai Technologies", "sai@example.com");

        Invoice invoice = createInvoice(
                "INV-C1-20260505-ABC12345",
                client,
                InvoiceStatus.DRAFT,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20)
        );

        invoiceRepository.save(invoice);

        boolean exists = invoiceRepository.existsByInvoiceNumber("INV-C1-20260505-ABC12345");

        assertThat(exists).isTrue();
    }

    @Test
    void findAll_WithSpecification_ShouldReturnFilteredInvoices() {
        Client client1 = saveClient("Sai Technologies", "sai@example.com");
        Client client2 = saveClient("ABC Consulting", "abc@example.com");

        Invoice invoice1 = createInvoice(
                "INV-C1-20260505-ABC12345",
                client1,
                InvoiceStatus.PAID,
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 20)
        );

        Invoice invoice2 = createInvoice(
                "INV-C2-20260605-XYZ12345",
                client2,
                InvoiceStatus.PAID,
                LocalDate.of(2026, 6, 5),
                LocalDate.of(2026, 6, 20)
        );

        Invoice invoice3 = createInvoice(
                "INV-C1-20260515-DEF12345",
                client1,
                InvoiceStatus.SENT,
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 30)
        );

        invoiceRepository.saveAll(List.of(invoice1, invoice2, invoice3));

        Specification<Invoice> spec = InvoiceSpecification.hasStatus(InvoiceStatus.PAID)
                .and(InvoiceSpecification.hasClientId(client1.getId()))
                .and(InvoiceSpecification.issueDateBetween(
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 31)
                ));

        List<Invoice> result = invoiceRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-C1-20260505-ABC12345");
        assertThat(result.get(0).getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(result.get(0).getClient().getId()).isEqualTo(client1.getId());
    }

    private Client saveClient(String name, String email) {
        Client client = new Client(
                name,
                email,
                "+1 437 123 4567",
                "Toronto, ON"
        );

        return clientRepository.save(client);
    }

    private Invoice createInvoice(
            String invoiceNumber,
            Client client,
            InvoiceStatus status,
            LocalDate issueDate,
            LocalDate dueDate
    ) {
        Invoice invoice = new Invoice(
                invoiceNumber,
                client,
                issueDate,
                dueDate,
                status,
                new BigDecimal("100.00"),
                new BigDecimal("13.00"),
                new BigDecimal("13.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("113.00"),
                new BigDecimal("0.00"),
                new BigDecimal("113.00")
        );

        InvoiceItem item = new InvoiceItem(
                "Website development",
                1,
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
        );

        invoice.addItem(item);

        return invoice;
    }
}