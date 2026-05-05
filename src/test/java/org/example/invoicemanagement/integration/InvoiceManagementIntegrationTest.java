package org.example.invoicemanagement.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.invoicemanagement.repository.ClientRepository;
import org.example.invoicemanagement.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class InvoiceManagementIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void createClient_ThenCreateInvoice_ThenGetInvoiceById_ShouldWorkEndToEnd() throws Exception {
        Long clientId = createClientAndReturnId();

        String invoiceRequestBody = """
                {
                  "clientId": %d,
                  "issueDate": "2030-05-05",
                  "dueDate": "2030-05-20",
                  "taxRate": 13.00,
                  "discountRate": 5.00,
                  "items": [
                    {
                      "description": "Website development",
                      "quantity": 2,
                      "unitPrice": 100.00
                    }
                  ]
                }
                """.formatted(clientId);

        String invoiceResponseBody = mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invoiceRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.clientName").value("Sai Technologies"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.subTotal").value(200.00))
                .andExpect(jsonPath("$.taxAmount").value(26.00))
                .andExpect(jsonPath("$.discountAmount").value(10.00))
                .andExpect(jsonPath("$.totalAmount").value(216.00))
                .andExpect(jsonPath("$.amountPaid").value(0.00))
                .andExpect(jsonPath("$.balanceDue").value(216.00))
                .andExpect(jsonPath("$.items[0].description").value("Website development"))
                .andExpect(jsonPath("$.items[0].lineTotal").value(200.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode invoiceJson = objectMapper.readTree(invoiceResponseBody);
        Long invoiceId = invoiceJson.get("id").asLong();

        mockMvc.perform(get("/api/v1/invoices/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.totalAmount").value(216.00))
                .andExpect(jsonPath("$.balanceDue").value(216.00));
    }

    @Test
    void updateInvoicePayment_WhenPartialPayment_ShouldUpdateBalanceAndStatus() throws Exception {
        Long clientId = createClientAndReturnId();
        Long invoiceId = createInvoiceAndReturnId(clientId);

        mockMvc.perform(patch("/api/v1/invoices/{id}/payment", invoiceId)
                        .param("amountPaid", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId))
                .andExpect(jsonPath("$.amountPaid").value(100.00))
                .andExpect(jsonPath("$.balanceDue").value(116.00))
                .andExpect(jsonPath("$.status").value("PARTIALLY_PAID"));
    }

    @Test
    void updateInvoicePayment_WhenAmountPaidGreaterThanTotal_ShouldReturnBadRequest() throws Exception {
        Long clientId = createClientAndReturnId();
        Long invoiceId = createInvoiceAndReturnId(clientId);

        mockMvc.perform(patch("/api/v1/invoices/{id}/payment", invoiceId)
                        .param("amountPaid", "500.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Amount paid cannot be greater than total amount"));
    }

    @Test
    void filterInvoices_WhenStatusClientAndDateRangeMatch_ShouldReturnPagedInvoices() throws Exception {
        Long clientId = createClientAndReturnId();
        Long invoiceId = createInvoiceAndReturnId(clientId);

        mockMvc.perform(patch("/api/v1/invoices/{id}/payment", invoiceId)
                        .param("amountPaid", "216.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/v1/invoices/filter")
                        .param("status", "PAID")
                        .param("clientId", clientId.toString())
                        .param("fromDate", "2030-05-01")
                        .param("toDate", "2030-05-31")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "dueDate")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(invoiceId))
                .andExpect(jsonPath("$.content[0].status").value("PAID"))
                .andExpect(jsonPath("$.content[0].clientId").value(clientId))
                .andExpect(jsonPath("$.content[0].balanceDue").value(0.00));
    }

    @Test
    void deleteInvoice_ThenGetInvoiceById_ShouldReturnNotFound() throws Exception {
        Long clientId = createClientAndReturnId();
        Long invoiceId = createInvoiceAndReturnId(clientId);

        mockMvc.perform(delete("/api/v1/invoices/{id}", invoiceId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/invoices/{id}", invoiceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Invoice not found with id: " + invoiceId));
    }

    @Test
    void createInvoice_WhenDueDateIsBeforeIssueDate_ShouldReturnBadRequest() throws Exception {
        Long clientId = createClientAndReturnId();

        String requestBody = """
                {
                  "clientId": %d,
                  "issueDate": "2030-05-20",
                  "dueDate": "2030-05-05",
                  "taxRate": 13.00,
                  "discountRate": 5.00,
                  "items": [
                    {
                      "description": "Website development",
                      "quantity": 2,
                      "unitPrice": 100.00
                    }
                  ]
                }
                """.formatted(clientId);

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Due date must be equal to or after issue date"));
    }

    private Long createClientAndReturnId() throws Exception {
        String clientRequestBody = """
                {
                  "name": "Sai Technologies",
                  "email": "sai@example.com",
                  "phone": "+1 437 123 4567",
                  "billingAddress": "Toronto, ON"
                }
                """;

        String clientResponseBody = mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sai Technologies"))
                .andExpect(jsonPath("$.email").value("sai@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode clientJson = objectMapper.readTree(clientResponseBody);

        return clientJson.get("id").asLong();
    }

    private Long createInvoiceAndReturnId(Long clientId) throws Exception {
        String invoiceRequestBody = """
                {
                  "clientId": %d,
                  "issueDate": "2030-05-05",
                  "dueDate": "2030-05-20",
                  "taxRate": 13.00,
                  "discountRate": 5.00,
                  "items": [
                    {
                      "description": "Website development",
                      "quantity": 2,
                      "unitPrice": 100.00
                    }
                  ]
                }
                """.formatted(clientId);

        String invoiceResponseBody = mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invoiceRequestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode invoiceJson = objectMapper.readTree(invoiceResponseBody);

        return invoiceJson.get("id").asLong();
    }
}