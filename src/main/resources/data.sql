-- Création des schémas pour nos tenants. La commande est compatible avec PostgreSQL.
CREATE SCHEMA IF NOT EXISTS tenant_a;
CREATE SCHEMA IF NOT EXISTS tenant_b;
-- Le schéma 'public' existe déjà par défaut dans PostgreSQL.

-- =================================================================
-- Table pour le tenant 'tenant_a'
-- NOTE: Remplacement de 'BIGINT AUTO_INCREMENT' par 'BIGSERIAL' pour PostgreSQL
-- =================================================================
CREATE TABLE IF NOT EXISTS tenant_a.produit (
                                                id BIGSERIAL PRIMARY KEY,
                                                nom VARCHAR(255) NOT NULL,
                                                description VARCHAR(255)
);

-- =================================================================
-- Table pour le tenant 'tenant_b'
-- =================================================================
CREATE TABLE IF NOT EXISTS tenant_b.produit (
                                                id BIGSERIAL PRIMARY KEY,
                                                nom VARCHAR(255) NOT NULL,
                                                description VARCHAR(255)
);

-- =================================================================
-- Table pour le schéma 'public' (tenant par défaut)
-- =================================================================
CREATE TABLE IF NOT EXISTS public.produit (
                                              id BIGSERIAL PRIMARY KEY,
                                              nom VARCHAR(255) NOT NULL,
                                              description VARCHAR(255)
);


-- =================================================================
-- Insertion des données de test (cette partie ne change pas)
-- =================================================================
-- On s'assure de ne pas insérer de doublons si le script est exécuté plusieurs fois.
INSERT INTO tenant_a.produit (nom, description)
SELECT 'Produit A1', 'Description pour le produit A1 du tenant A'
WHERE NOT EXISTS (SELECT 1 FROM tenant_a.produit WHERE nom = 'Produit A1');

INSERT INTO tenant_a.produit (nom, description)
SELECT 'Produit A2', 'Description pour le produit A2 du tenant A'
WHERE NOT EXISTS (SELECT 1 FROM tenant_a.produit WHERE nom = 'Produit A2');


INSERT INTO tenant_b.produit (nom, description)
SELECT 'Produit B1', 'Description pour le produit B1 du tenant B'
WHERE NOT EXISTS (SELECT 1 FROM tenant_b.produit WHERE nom = 'Produit B1');

