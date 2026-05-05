package org.example.invoicemanagement.repository;

import org.example.invoicemanagement.entity.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void findByNameContainingIgnoreCase_WhenNameMatches_ShouldReturnClients() {
        Client client1 = new Client(
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );

        Client client2 = new Client(
                "ABC Consulting",
                "abc@example.com",
                "+1 416 111 2222",
                "Markham, ON"
        );

        clientRepository.saveAll(List.of(client1, client2));

        List<Client> result = clientRepository.findByNameContainingIgnoreCase("sai");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sai Technologies");
        assertThat(result.get(0).getEmail()).isEqualTo("sai@example.com");
    }

    @Test
    void save_WhenValidClient_ShouldPersistClient() {
        Client client = new Client(
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );

        Client savedClient = clientRepository.save(client);

        assertThat(savedClient.getId()).isNotNull();
        assertThat(savedClient.getName()).isEqualTo("Sai Technologies");
        assertThat(savedClient.getEmail()).isEqualTo("sai@example.com");
    }
}