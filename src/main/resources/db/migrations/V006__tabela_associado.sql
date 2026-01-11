-- V007__tabela_associado.sql

-- Tabela para armazenar associados (dados do formulário)
CREATE TABLE IF NOT EXISTS associado (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    sobrenome VARCHAR(255),
    data_nascimento DATE,
    cpf VARCHAR(50),
    rg VARCHAR(50),
    email VARCHAR(255),
    telefone_contato VARCHAR(50),
    telefone_emergencia VARCHAR(50),
    medico_responsavel VARCHAR(255),
    telefone_medico VARCHAR(50),
    possui_convenio BOOLEAN,
    convenio_nome VARCHAR(255),
    cidade VARCHAR(255),
    estado VARCHAR(100),
    bairro VARCHAR(255),
    logradouro VARCHAR(255),
    complemento VARCHAR(255),
    cep VARCHAR(50),
    observacoes TEXT,
    created_at TIMESTAMP DEFAULT now()
);

-- Tabela para armazenar metadados dos arquivos do associado e a resposta do Cloudinary
CREATE TABLE IF NOT EXISTS associado_file (
    id BIGSERIAL PRIMARY KEY,
    associado_id BIGINT NOT NULL,
    field_name VARCHAR(255),
    original_name VARCHAR(255),
    content_type VARCHAR(255),
    size BIGINT,
    cloud_public_id VARCHAR(255),
    cloud_url TEXT,
    cloud_folder VARCHAR(255),
    cloud_success BOOLEAN,
    cloud_message TEXT,
    created_at TIMESTAMP DEFAULT now(),
    FOREIGN KEY (associado_id) REFERENCES associado(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_associado_cpf ON associado(cpf);
CREATE INDEX IF NOT EXISTS idx_associadofile_associd ON associado_file(associado_id);

