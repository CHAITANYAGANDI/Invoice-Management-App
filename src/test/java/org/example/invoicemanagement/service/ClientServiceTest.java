package org.example.invoicemanagement.service;

import org.example.invoicemanagement.dto.ClientRequestDTO;
import org.example.invoicemanagement.dto.ClientResponseDTO;
import org.example.invoicemanagement.entity.Client;
import org.example.invoicemanagement.exception.ClientNotFoundException;
import org.example.invoicemanagement.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createClient_WhenValidRequest_ShouldReturnClientResponseDTO() {
        ClientRequestDTO requestDTO = new ClientRequestDTO(
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            client.setId(1L);
            return client;
        });

        ClientResponseDTO responseDTO = clientService.createClient(requestDTO);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("Sai Technologies", responseDTO.getName());
        assertEquals("sai@example.com", responseDTO.getEmail());
        assertEquals("+1 437 123 4567", responseDTO.getPhone());
        assertEquals("Toronto, ON", responseDTO.getBillingAddress());

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void getClientById_WhenClientExists_ShouldReturnClientResponseDTO() {
        Client client = new Client(
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );
        client.setId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientResponseDTO responseDTO = clientService.getClientById(1L);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("Sai Technologies", responseDTO.getName());
        assertEquals("sai@example.com", responseDTO.getEmail());

        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void getClientById_WhenClientDoesNotExist_ShouldThrowClientNotFoundException() {
        when(clientRepository.findById(100L)).thenReturn(Optional.empty());

        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.getClientById(100L)
        );

        assertEquals("Client not found with id: 100", exception.getMessage());

        verify(clientRepository, times(1)).findById(100L);
    }

    @Test
    void updateClientById_WhenClientExists_ShouldUpdateAndReturnClientResponseDTO() {
        Client existingClient = new Client(
                "Old Name",
                "old@example.com",
                "1111111111",
                "Old Address"
        );
        existingClient.setId(1L);

        ClientRequestDTO updateRequest = new ClientRequestDTO(
                "New Name",
                "new@example.com",
                "2222222222",
                "New Address"
        );

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientResponseDTO responseDTO = clientService.updateClientById(1L, updateRequest);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("New Name", responseDTO.getName());
        assertEquals("new@example.com", responseDTO.getEmail());
        assertEquals("2222222222", responseDTO.getPhone());
        assertEquals("New Address", responseDTO.getBillingAddress());

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(existingClient);
    }

    @Test
    void deleteClientById_WhenClientExists_ShouldDeleteClient() {
        Client client = new Client(
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );
        client.setId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientService.deleteClientById(1L);

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).delete(client);
    }

    @Test
    void searchClient_WhenNameMatches_ShouldReturnMatchingClients() {
        Client client = new Client(
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );
        client.setId(1L);

        when(clientRepository.findByNameContainingIgnoreCase("sai"))
                .thenReturn(List.of(client));

        List<ClientResponseDTO> result = clientService.searchClient("sai");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sai Technologies", result.get(0).getName());

        verify(clientRepository, times(1)).findByNameContainingIgnoreCase("sai");
    }
}