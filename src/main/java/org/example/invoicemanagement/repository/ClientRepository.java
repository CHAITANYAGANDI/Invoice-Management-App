package org.example.invoicemanagement.repository;

import org.example.invoicemanagement.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long>{

    List<Client> findByNameContainingIgnoreCase(String name);

}
