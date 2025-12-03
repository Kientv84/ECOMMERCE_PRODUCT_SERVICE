-- ======================================
-- V1__create_tables.sql
-- ======================================
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE IF NOT EXISTS role_entity (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL,
    role_description TEXT,
    status VARCHAR(50),
    created_by VARCHAR(500),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(500),
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_code VARCHAR(50) UNIQUE,
    user_name VARCHAR(100) NOT NULL,
    user_birthday DATE,
    user_gender VARCHAR(20),
    user_phone_number VARCHAR(50),
    user_address VARCHAR(255),
    user_email VARCHAR(100) UNIQUE,
    role_id BIGINT REFERENCES role_entity(id) ON DELETE SET NULL,
    user_password VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    user_avatar VARCHAR(255),
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS brand_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand_name VARCHAR(255) NOT NULL,
    brand_code VARCHAR(100) UNIQUE,
    origin VARCHAR(255),
    description TEXT,
    thumbnail_url TEXT,
    status VARCHAR(50),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS category_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_name VARCHAR(255) NOT NULL,
    category_code VARCHAR(100) UNIQUE,
    description TEXT,
    thumbnail_url TEXT,
    status VARCHAR(50),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS sub_category_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sub_category_name VARCHAR(255) NOT NULL,
    sub_category_code VARCHAR(100) UNIQUE,
    description TEXT,
    thumbnail_url TEXT,
    status VARCHAR(50),
    category_id UUID NOT NULL REFERENCES category_entity(id) ON DELETE CASCADE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS collection_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_name VARCHAR(255) NOT NULL,
    collection_code VARCHAR(100) UNIQUE,
    description TEXT,
    status VARCHAR(50),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS material_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_name VARCHAR(255) NOT NULL,
    material_code VARCHAR(100) UNIQUE,
    description TEXT,
    status VARCHAR(50),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 8. Product
CREATE TABLE IF NOT EXISTS product_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL,
    product_code VARCHAR(255),
    description TEXT,

    brand_id UUID NOT NULL REFERENCES brand_entity(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES category_entity(id) ON DELETE CASCADE,
    sub_category_id UUID REFERENCES sub_category_entity(id) ON DELETE SET NULL,
    collection_id UUID REFERENCES collection_entity(id) ON DELETE SET NULL,
    material_id UUID REFERENCES material_entity(id) ON DELETE SET NULL,

    base_price NUMERIC(18,2),
    discount_percent NUMERIC(5,2),
    origin VARCHAR(255),
    fit_type VARCHAR(100),
    care_instruction VARCHAR(255),
    count_in_stock INTEGER,
    status VARCHAR(50),
    rating_average DOUBLE PRECISION,
    rating_count INTEGER,

    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_product_category ON product_entity (category_id);
CREATE INDEX IF NOT EXISTS idx_product_brand ON product_entity (brand_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON product_entity (status);

-- ===============================
-- PRODUCT IMAGE (NEW)
-- ===============================
CREATE TABLE IF NOT EXISTS product_image_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES product_entity(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    sort_order INTEGER
);

CREATE INDEX IF NOT EXISTS idx_product_image_product ON product_image_entity(product_id);

-- ==========================
-- USER_ENTITY FULL-TEXT SEARCH SETUP
-- ==========================

-- 1. Thêm cột tsvector (nếu chưa có)
ALTER TABLE user_entity
ADD COLUMN IF NOT EXISTS document_tsv tsvector;

-- 2. Cập nhật dữ liệu hiện có với unaccent + lower
UPDATE user_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(unaccent(lower(user_name)),'') || ' ' ||
    coalesce(unaccent(lower(user_email)),'') || ' ' ||
    coalesce(unaccent(lower(user_phone_number)),'')
);

-- 3. Tạo GIN index để search nhanh (nếu chưa có)
CREATE INDEX IF NOT EXISTS idx_user_document_tsv
ON user_entity USING GIN(document_tsv);

-- 4. Tạo function trigger để tự động cập nhật khi insert/update
CREATE OR REPLACE FUNCTION user_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(unaccent(lower(NEW.user_name)),'') || ' ' ||
        coalesce(unaccent(lower(NEW.user_email)),'') || ' ' ||
        coalesce(unaccent(lower(NEW.user_phone_number)),'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- 5. Tạo trigger gắn vào bảng (BEFORE INSERT OR UPDATE)
DROP TRIGGER IF EXISTS tsvectorupdate_user ON user_entity;

CREATE TRIGGER tsvectorupdate_user
BEFORE INSERT OR UPDATE ON user_entity
FOR EACH ROW
EXECUTE FUNCTION user_tsv_trigger();

-- ==========================
-- USER_ENTITY SEARCH SUGGEST FUNCTION
-- ==========================

-- Tạo function để gọi từ API hoặc query trực tiếp
CREATE OR REPLACE FUNCTION user_search_suggest(input_text text, limit_count int DEFAULT 5)

RETURNS TABLE (
    user_id uuid,
    user_name text,
    user_email text,
    user_phone_number text,
    rank float
) AS $$
DECLARE
    q text;
BEGIN
    -- Chuẩn hóa input: unaccent + lower
    q := unaccent(lower(input_text));

    RETURN QUERY
    SELECT
        id AS user_id,
        user_name,
        user_email,
        user_phone_number,
        ts_rank(document_tsv, to_tsquery('simple', q || ':*')) AS rank
    FROM user_entity
    WHERE document_tsv @@ to_tsquery('simple', q || ':*')
    ORDER BY rank DESC
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;

-- ==========================
-- Cách dùng:
-- SELECT * FROM user_search_suggest('ki', 10);
-- ==========================


-- ===== BRAND ENTITY SEARCH =====

ALTER TABLE brand_entity
ADD COLUMN IF NOT EXISTS document_tsv tsvector;

UPDATE brand_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(unaccent(lower(brand_name)),'') || ' ' ||
    coalesce(unaccent(lower(brand_code)),'') || ' ' ||
    coalesce(unaccent(lower(description)),'')
);


CREATE INDEX IF NOT EXISTS idx_brand_document_tsv ON brand_entity USING GIN(document_tsv);

CREATE OR REPLACE FUNCTION brand_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
         coalesce(unaccent(lower(NEW.brand_name)),'') || ' ' ||
         coalesce(unaccent(lower(NEW.brand_code)),'') || ' ' ||
         coalesce(unaccent(lower(NEW.description)),'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tsvectorupdate_brand ON brand_entity;

CREATE TRIGGER tsvectorupdate_brand
BEFORE INSERT OR UPDATE ON brand_entity
FOR EACH ROW
EXECUTE FUNCTION brand_tsv_trigger();

CREATE OR REPLACE FUNCTION brand_search_suggest( input_text text, limit_count int DEFAULT 5)

RETURNS TABLE (
    brand_id uuid,
    brand_name text,
    brand_code text,
    description text,
    rank float
) AS $$ DECLARE
    q text;
BEGIN
    q:=unaccent(lower(input_text));

   RETURN QUERY
   SELECT
         id AS brand_id,
         brand_name,
         brand_code,
         description,
         ts_rank(document_tsv, to_tsquery('simple', q || ':*')) AS rank
        FROM brand_entity
        WHERE document_tsv @@ to_tsquery('simple', q || ':*')
        ORDER BY rank DESC
        LIMIT limit_count;
    END;
    $$ LANGUAGE plpgsql;



-- ===== ROLE ENTITY SEARCH =====

ALTER TABLE role_entity
ADD COLUMN document_tsv tsvector;

UPDATE role_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(role_name,'') || ' ' || coalesce(role_description,'')
);

CREATE INDEX idx_role_document_tsv ON role_entity USING GIN(document_tsv);

CREATE FUNCTION role_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(NEW.role_name,'') || ' ' || coalesce(NEW.role_description,'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate_role BEFORE INSERT OR UPDATE
ON role_entity FOR EACH ROW EXECUTE PROCEDURE role_tsv_trigger();


-- ===== CATEGORY ENTITY SEARCH =====

ALTER TABLE category_entity
ADD COLUMN IF NOT EXISTS document_tsv tsvector;

UPDATE category_entity
SET document_tsv = to_tsvector(
    'simple',
     coalesce(unaccent(lower(category_name)),'') || ' ' ||
     coalesce(unaccent(lower(category_code)),'') || ' ' ||
     coalesce(unaccent(lower(description)),'')
);

CREATE INDEX IF NOT EXISTS idx_category_document_tsv ON category_entity USING GIN(document_tsv);

CREATE FUNCTION category_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(unaccent(lower(NEW.category_name)),'') || ' ' ||
        coalesce(unaccent(lower(NEW.category_code)),'') || ' ' ||
        coalesce(unaccent(lower(NEW.description)),'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS tsvectorupdate_category ON category_entity;

CREATE TRIGGER tsvectorupdate_category
BEFORE INSERT OR UPDATE ON category_entity
FOR EACH ROW
EXECUTE FUNCTION category_tsv_trigger();

CREATE OR REPLACE FUNCTION category_search_suggest( input_text text, limit_count int DEFAULT 5)

RETURNS TABLE (
    category_id uuid,
    category_name text,
    category_code text,
    description text,
    rank float
) AS $$ DECLARE
    q text;
BEGIN
    q:=unaccent(lower(input_text));

   RETURN QUERY
   SELECT
         id AS category_id,
         category_name,
         category_code,
         description,
         ts_rank(document_tsv, to_tsquery('simple', q || ':*')) AS rank
        FROM category_entity
        WHERE document_tsv @@ to_tsquery('simple', q || ':*')
        ORDER BY rank DESC
        LIMIT limit_count;
    END;
    $$ LANGUAGE plpgsql;


-- ===== COLLECTION ENTITY SEARCH =====

ALTER TABLE collection_entity
ADD COLUMN IF NOT EXISTS document_tsv tsvector;

UPDATE collection_entity
SET document_tsv = to_tsvector(
    'simple',
     coalesce(unaccent(lower(collection_name)),'') || ' ' ||
     coalesce(unaccent(lower(collection_code)),'') || ' ' ||
     coalesce(unaccent(lower(description)),'')
);

CREATE INDEX IF NOT EXISTS idx_collection_document_tsv ON collection_entity USING GIN(document_tsv);

CREATE FUNCTION collection_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
         coalesce(unaccent(lower(NEW.collection_name)),'') || ' ' ||
         coalesce(unaccent(lower(NEW.collection_code)),'') || ' ' ||
         coalesce(unaccent(lower(NEW.description)),'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS tsvectorupdate_collection ON collection_entity;

CREATE TRIGGER tsvectorupdate_collection
BEFORE INSERT OR UPDATE ON collection_entity
FOR EACH ROW
EXECUTE FUNCTION collection_tsv_trigger();

CREATE OR REPLACE FUNCTION collection_search_suggest( input_text text, limit_count int DEFAULT 5)

RETURNS TABLE (
    collection_id uuid,
    collection_name text,
    collection_code text,
    description text,
    rank float
) AS $$ DECLARE
    q text;
BEGIN
    q:=unaccent(lower(input_text));

   RETURN QUERY
   SELECT
         id AS collection_id,
         collection_name,
         collection_code,
         description,
         ts_rank(document_tsv, to_tsquery('simple', q || ':*')) AS rank
        FROM collection_entity
        WHERE document_tsv @@ to_tsquery('simple', q || ':*')
        ORDER BY rank DESC
        LIMIT limit_count;
    END;
    $$ LANGUAGE plpgsql;

-- ===== PRODUCT ENTITY SEARCH =====

ALTER TABLE product_entity
ADD COLUMN IF NOT EXISTS document_tsv tsvector;

UPDATE product_entity
SET document_tsv = to_tsvector(
    'simple',
     coalesce(unaccent(lower(product_name)),'') || ' ' ||
     coalesce(unaccent(lower(product_code)),'') || ' ' ||
     coalesce(unaccent(lower(origin)),'') || ' ' ||
     coalesce(unaccent(lower(fit_type)),'') || ' ' ||
     coalesce(unaccent(lower(description)),'')
);

CREATE INDEX IF NOT EXISTS idx_product_document_tsv ON product_entity USING GIN(document_tsv);

CREATE FUNCTION product_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
       coalesce(unaccent(lower(NEW.product_name)),'') || ' ' ||
       coalesce(unaccent(lower(NEW.product_code)),'') || ' ' ||
       coalesce(unaccent(lower(NEW.origin)),'') || ' ' ||
       coalesce(unaccent(lower(NEW.fit_type)),'') || ' ' ||
       coalesce(unaccent(lower(NEW.description)),'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tsvectorupdate_product ON product_entity;

CREATE TRIGGER tsvectorupdate_product
BEFORE INSERT OR UPDATE ON product_entity
FOR EACH ROW
EXECUTE FUNCTION product_tsv_trigger();

CREATE OR REPLACE FUNCTION product_search_suggest( input_text text, limit_count int DEFAULT 5)

RETURNS TABLE (
    product_id uuid,
    product_name text,
    product_code text,
    origin text,
    fit_type text,
    description text,
    rank float
) AS $$ DECLARE
    q text;
BEGIN
    q:=unaccent(lower(input_text));

   RETURN QUERY
   SELECT
         id AS product_id,
         product_name,
         product_code,
         origin,
         fit_type,
         description,
         ts_rank(document_tsv, to_tsquery('simple', q || ':*')) AS rank
        FROM product_entity
        WHERE document_tsv @@ to_tsquery('simple', q || ':*')
        ORDER BY rank DESC
        LIMIT limit_count;
    END;
    $$ LANGUAGE plpgsql;

-- ===== SUB_CATEGORY ENTITY SEARCH =====

ALTER TABLE sub_category_entity
ADD COLUMN IF NOT EXISTS document_tsv tsvector;

-- Cập nhật dữ liệu hiện có
UPDATE sub_category_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(unaccent(lower(sub_category_name)),'') || ' ' ||
    coalesce(unaccent(lower(sub_category_code)),'') || ' ' ||
    coalesce(unaccent(lower(description)),'')
);

-- Tạo index GIN
CREATE INDEX IF NOT EXISTS idx_sub_category_document_tsv
ON sub_category_entity USING GIN(document_tsv);

-- Trigger function
CREATE OR REPLACE FUNCTION sub_category_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(unaccent(lower(NEW.sub_category_name)),'') || ' ' ||
        coalesce(unaccent(lower(NEW.sub_category_code)),'') || ' ' ||
        coalesce(unaccent(lower(NEW.description)),'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Tạo trigger
DROP TRIGGER IF EXISTS tsvectorupdate_sub_category ON sub_category_entity;

CREATE TRIGGER tsvectorupdate_sub_category
BEFORE INSERT OR UPDATE ON sub_category_entity
FOR EACH ROW
EXECUTE FUNCTION sub_category_tsv_trigger();

-- Search suggest function
CREATE OR REPLACE FUNCTION sub_category_search_suggest(input_text text, limit_count int DEFAULT 5)
RETURNS TABLE (
    sub_category_id uuid,
    sub_category_name text,
    sub_category_code text,
    description text,
    rank float
) AS $$
DECLARE
    q text;
BEGIN
    q := unaccent(lower(input_text));

    RETURN QUERY
    SELECT
        id AS sub_category_id,
        sub_category_name,
        sub_category_code,
        description,
        ts_rank(document_tsv, to_tsquery('simple', q || ':*')) AS rank
    FROM sub_category_entity
    WHERE document_tsv @@ to_tsquery('simple', q || ':*')
    ORDER BY rank DESC
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;

-- ===== MATERIAL ENTITY SEARCH =====

ALTER TABLE material_entity
ADD COLUMN IF NOT EXISTS document_tsv tsvector;

-- Cập nhật dữ liệu hiện có
UPDATE material_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(unaccent(lower(material_name)),'') || ' ' ||
    coalesce(unaccent(lower(material_code)),'') || ' ' ||
    coalesce(unaccent(lower(description)),'')
);

-- Tạo index GIN
CREATE INDEX IF NOT EXISTS idx_material_document_tsv
ON material_entity USING GIN(document_tsv);

-- Trigger function
CREATE OR REPLACE FUNCTION material_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(unaccent(lower(NEW.material_name)),'') || ' ' ||
        coalesce(unaccent(lower(NEW.material_code)),'') || ' ' ||
        coalesce(unaccent(lower(NEW.description)),'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Tạo trigger
DROP TRIGGER IF EXISTS tsvectorupdate_material ON material_entity;

CREATE TRIGGER tsvectorupdate_material
BEFORE INSERT OR UPDATE ON material_entity
FOR EACH ROW
EXECUTE FUNCTION material_tsv_trigger();

-- Search suggest function
CREATE OR REPLACE FUNCTION material_search_suggest(input_text text, limit_count int DEFAULT 5)
RETURNS TABLE (
    material_id uuid,
    material_name text,
    material_code text,
    description text,
    rank float
) AS $$
DECLARE
    q text;
BEGIN
    q := unaccent(lower(input_text));

    RETURN QUERY
    SELECT
        id AS material_id,
        material_name,
        material_code,
        description,
        ts_rank(document_tsv, to_tsquery('simple', q || ':*')) AS rank
    FROM material_entity
    WHERE document_tsv @@ to_tsquery('simple', q || ':*')
    ORDER BY rank DESC
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;
