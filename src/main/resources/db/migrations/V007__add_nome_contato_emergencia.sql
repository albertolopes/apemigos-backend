-- Migration V007: adiciona coluna nome_contato_emergencia à tabela associado

ALTER TABLE associado
    ADD COLUMN nome_contato_emergencia VARCHAR(255);

-- Nota: coluna nullable por padrão; caso queira NOT NULL, ajustar e fornecer valor padrão.

