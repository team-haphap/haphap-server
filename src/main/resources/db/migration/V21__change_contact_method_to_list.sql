ALTER TABLE registration RENAME COLUMN contact_method TO contact_methods;
ALTER TABLE registration ALTER COLUMN contact_methods TYPE VARCHAR(255);