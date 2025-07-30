-- Ce script ne mentionne ni tenant_a, ni tenant_b. Il est générique.
CREATE TABLE produit (
                         id BIGSERIAL PRIMARY KEY,
                         nom VARCHAR(255) NOT NULL,
                         description VARCHAR(255)
);