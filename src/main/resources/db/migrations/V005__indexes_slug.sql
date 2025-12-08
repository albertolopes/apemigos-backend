CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Índice trigram GIN para ILIKE '%termo%'
CREATE INDEX IF NOT EXISTS idx_noticia_slug_trgm ON noticia USING gin (slug gin_trgm_ops);

-- Índice único case-insensitive em LOWER(slug)
CREATE UNIQUE INDEX IF NOT EXISTS idx_noticia_slug_unique_lower ON noticia (LOWER(slug));
