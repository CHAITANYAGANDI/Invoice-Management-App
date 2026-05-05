package org.example.invoicemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.invoicemanagement.dto.ClientRequestDTO;
import org.example.invoicemanagement.dto.ClientResponseDTO;
import org.example.invoicemanagement.exception.ClientNotFoundException;
import org.example.invoicemanagement.exception.GlobalExceptionHandler;
import org.example.invoicemanagement.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import(GlobalExceptionHandler.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService clientService;

    @Test
    void createClient_WhenValidRequest_ShouldReturnCreatedClient() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO(
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );

        ClientResponseDTO responseDTO = new ClientResponseDTO(
                1L,
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(clientService.createClient(any(ClientRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sai Technologies"))
                .andExpect(jsonPath("$.email").value("sai@example.com"))
                .andExpect(jsonPath("$.phone").value("+1 437 123 4567"))
                .andExpect(jsonPath("$.billingAddress").value("Toronto, ON"));

        verify(clientService, times(1)).createClient(any(ClientRequestDTO.class));
    }

    @Test
    void createClient_WhenNameIsBlank_ShouldReturnBadRequest() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO(
                "",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON"
        );

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Client name is required"));

        verify(clientService, never()).createClient(any(ClientRequestDTO.class));
    }

    @Test
    void createClient_WhenEmailIsInvalid_ShouldReturnBadRequest() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO(
                "Sai Technologies",
                "invalid-email",
                "+1 437 123 4567",
                "Toronto, ON"
        );

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email should be valid"));

        verify(clientService, never()).createClient(any(ClientRequestDTO.class));
    }

    @Test
    void getAllClients_ShouldReturnClients() throws Exception {
        ClientResponseDTO client = new ClientResponseDTO(
                1L,
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(clientService.getAllClients()).thenReturn(List.of(client));

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Sai Technologies"))
                .andExpect(jsonPath("$[0].email").value("sai@example.com"));

        verify(clientService, times(1)).getAllClients();
    }

    @Test
    void getClientById_WhenClientExists_ShouldReturnClient() throws Exception {
        ClientResponseDTO client = new ClientResponseDTO(
                1L,
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(clientService.getClientById(1L)).thenReturn(client);

        mockMvc.perform(get("/api/v1/clients/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sai Technologies"))
                .andExpect(jsonPath("$.email").value("sai@example.com"));

        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    void getClientById_WhenClientDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(clientService.getClientById(100L))
                .thenThrow(new ClientNotFoundException("Client not found with id: 100"));

        mockMvc.perform(get("/api/v1/clients/{id}", 100L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Client not found with id: 100"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/100"));

        verify(clientService, times(1)).getClientById(100L);
    }

    @Test
    void updateClient_WhenValidRequest_ShouldReturnUpdatedClient() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO(
                "Updated Client",
                "updated@example.com",
                "2222222222",
                "Updated Address"
        );

        ClientResponseDTO responseDTO = new ClientResponseDTO(
                1L,
                "Updated Client",
                "updated@example.com",
                "2222222222",
                "Updated Address",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(clientService.updateClientById(eq(1L), any(ClientRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/clients/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Client"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(clientService, times(1))
                .updateClientById(eq(1L), any(ClientRequestDTO.class));
    }

    @Test
    void deleteClient_WhenClientExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(clientService).deleteClientById(1L);

        mockMvc.perform(delete("/api/v1/clients/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClientById(1L);
    }

    @Test
    void searchClients_WhenNameMatches_ShouldReturnClients() throws Exception {
        ClientResponseDTO client = new ClientResponseDTO(
                1L,
                "Sai Technologies",
                "sai@example.com",
                "+1 437 123 4567",
                "Toronto, ON",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(clientService.searchClient("sai")).thenReturn(List.of(client));

        mockMvc.perform(get("/api/v1/clients/search")
                        .param("name", "sai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Sai Technologies"));

        verify(clientService, times(1)).searchClient("sai");
    }
}