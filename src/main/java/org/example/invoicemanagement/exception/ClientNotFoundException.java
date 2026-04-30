package org.example.invoicemanagement.exception;

public class ClientNotFoundException extends RuntimeException{

    public ClientNotFoundException(String message){

        super(message);
    }
}
