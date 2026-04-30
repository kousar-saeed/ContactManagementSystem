-- SQL Server schema for Contact Management System
-- Note: Hibernate can also manage this schema via ddl-auto=update.

IF OBJECT_ID('dbo.phone_numbers', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.phone_numbers (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        contact_id BIGINT NOT NULL,
        number NVARCHAR(50) NULL,
        label NVARCHAR(20) NULL
    );
END

IF OBJECT_ID('dbo.email_addresses', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.email_addresses (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        contact_id BIGINT NOT NULL,
        email NVARCHAR(320) NULL,
        label NVARCHAR(20) NULL
    );
END

IF OBJECT_ID('dbo.contacts', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.contacts (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        first_name NVARCHAR(100) NULL,
        last_name NVARCHAR(100) NULL,
        title NVARCHAR(100) NULL,
        user_id BIGINT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL
    );
END

IF OBJECT_ID('dbo.users', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        first_name NVARCHAR(100) NULL,
        last_name NVARCHAR(100) NULL,
        email NVARCHAR(320) NOT NULL,
        phone NVARCHAR(50) NULL,
        password_hash NVARCHAR(255) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT UQ_users_email UNIQUE (email)
    );
END

-- Foreign keys (create only if missing)
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_contacts_users')
BEGIN
    ALTER TABLE dbo.contacts
    ADD CONSTRAINT FK_contacts_users FOREIGN KEY (user_id) REFERENCES dbo.users(id);
END

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_email_addresses_contacts')
BEGIN
    ALTER TABLE dbo.email_addresses
    ADD CONSTRAINT FK_email_addresses_contacts FOREIGN KEY (contact_id) REFERENCES dbo.contacts(id);
END

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_phone_numbers_contacts')
BEGIN
    ALTER TABLE dbo.phone_numbers
    ADD CONSTRAINT FK_phone_numbers_contacts FOREIGN KEY (contact_id) REFERENCES dbo.contacts(id);
END

