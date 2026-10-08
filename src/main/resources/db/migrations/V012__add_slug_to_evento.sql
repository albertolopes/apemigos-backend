ALTER TABLE evento ADD COLUMN slug VARCHAR(150);

UPDATE evento
SET slug = LOWER(REGEXP_REPLACE(REGEXP_REPLACE(titulo, '[^a-zA-Z0-9]+', '-', 'g'), '(^-|-$)', '', 'g'))
WHERE slug IS NULL;

UPDATE evento
SET slug = CONCAT('evento-', id)
WHERE slug IS NULL OR slug = '';

ALTER TABLE evento ALTER COLUMN slug SET NOT NULL;

CREATE UNIQUE INDEX idx_evento_slug ON evento(slug);
