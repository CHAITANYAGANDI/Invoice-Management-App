package org.example.invoicemanagement.service;

import org.example.invoicemanagement.dto.ClientRequestDTO;
import org.example.invoicemanagement.dto.ClientResponseDTO;
import org.example.invoicemanagement.entity.Client;
import org.example.invoicemanagement.exception.ClientNotFoundException;
import org.example.invoicemanagement.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository){

        this.clientRepository = clientRepository;
    }

    private ClientResponseDTO convertToResponseDTO(Client client){

        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getBillingAddress(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }

    @Transactional
    public ClientResponseDTO createClient(ClientRequestDTO clientRequestDTO){

        Client client = new Client(

                clientRequestDTO.getName(),
                clientRequestDTO.getEmail(),
                clientRequestDTO.getPhone(),
                clientRequestDTO.getBillingAddress()
        );

        Client savedClient = clientRepository.save(client);

        return convertToResponseDTO(savedClient);

    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAllClients(){

        return clientRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long id){

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: "+id));

        return convertToResponseDTO(client);
    }

    @Transactional
    public ClientResponseDTO updateClientById(Long id, ClientRequestDTO updateClient){

        Client client = clientRepository.findById(id).
                orElseThrow(() -> new ClientNotFoundException("Client not found with id: "+ id));

        client.setName(updateClient.getName());
        client.setEmail(updateClient.getEmail());
        client.setPhone(updateClient.getPhone());
        client.setBillingAddress(updateClient.getBillingAddress());

        Client savedClient = clientRepository.save(client);

        return convertToResponseDTO(savedClient);
    }

    @Transactional
    public void deleteClientById(Long id){

        Client client = clientRepository.findById(id).
                orElseThrow(() -> new ClientNotFoundException("Client not found with id: "+id));

        clientRepository.delete(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> searchClient(String name){

        return clientRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


}
