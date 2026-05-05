CREATE TABLE client (
                        id BIGSERIAL PRIMARY KEY,

                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(150) NOT NULL,
                        phone VARCHAR(20),
                        billing_address VARCHAR(255),

                        created_at TIMESTAMP,
                        updated_at TIMESTAMP
);

CREATE TABLE invoice (
                         id BIGSERIAL PRIMARY KEY,

                         invoice_number VARCHAR(50) NOT NULL UNIQUE,

                         client_id BIGINT NOT NULL,

                         issue_date DATE NOT NULL,
                         due_date DATE NOT NULL,

                         status VARCHAR(30) NOT NULL,

                         sub_total NUMERIC(12, 2) NOT NULL,
                         tax_rate NUMERIC(5, 2) NOT NULL,
                         tax_amount NUMERIC(12, 2) NOT NULL,

                         discount_rate NUMERIC(5, 2) NOT NULL,
                         discount_amount NUMERIC(12, 2) NOT NULL,

                         total_amount NUMERIC(12, 2) NOT NULL,
                         amount_paid NUMERIC(12, 2) NOT NULL,
                         balance_due NUMERIC(12, 2) NOT NULL,

                         created_at TIMESTAMP,
                         updated_at TIMESTAMP,

                         CONSTRAINT fk_invoice_client
                             FOREIGN KEY (client_id)
                                 REFERENCES client(id),

                         CONSTRAINT chk_invoice_dates
                             CHECK (due_date >= issue_date),

                         CONSTRAINT chk_tax_rate
                             CHECK (tax_rate >= 0 AND tax_rate <= 100),

                         CONSTRAINT chk_discount_rate
                             CHECK (discount_rate >= 0 AND discount_rate <= 100),

                         CONSTRAINT chk_invoice_amounts
                             CHECK (
                                 sub_total >= 0
                                     AND tax_amount >= 0
                                     AND discount_amount >= 0
                                     AND total_amount >= 0
                                     AND amount_paid >= 0
                                     AND balance_due >= 0
                                 )
);

CREATE TABLE invoice_item (
                              id BIGSERIAL PRIMARY KEY,

                              invoice_id BIGINT NOT NULL,

                              description VARCHAR(255) NOT NULL,
                              quantity INTEGER NOT NULL,
                              unit_price NUMERIC(12, 2) NOT NULL,
                              line_total NUMERIC(12, 2) NOT NULL,

                              CONSTRAINT fk_invoice_item_invoice
                                  FOREIGN KEY (invoice_id)
                                      REFERENCES invoice(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT chk_invoice_item_quantity
                                  CHECK (quantity >= 1),

                              CONSTRAINT chk_invoice_item_amounts
                                  CHECK (
                                      unit_price > 0
                                          AND line_total > 0
                                      )
);

CREATE INDEX idx_invoice_client_id ON invoice(client_id);
CREATE INDEX idx_invoice_status ON invoice(status);
CREATE INDEX idx_invoice_issue_date ON invoice(issue_date);
CREATE INDEX idx_invoice_due_date ON invoice(due_date);
CREATE INDEX idx_invoice_item_invoice_id ON invoice_item(invoice_id);