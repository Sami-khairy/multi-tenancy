CREATE TABLE installed_module
(
    module_name VARCHAR(255) NOT NULL,
    active      BOOLEAN      NOT NULL,
    CONSTRAINT pk_installed_module PRIMARY KEY (module_name)
);