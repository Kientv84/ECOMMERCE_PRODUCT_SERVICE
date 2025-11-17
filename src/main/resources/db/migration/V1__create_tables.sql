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
    discount_percent REAL,
    origin VARCHAR(255),
    fit_type VARCHAR(100),
    care_instruction VARCHAR(255),
    count_in_stock INTEGER,
    status VARCHAR(100),
    thumbnail_url TEXT,
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

-- 1. Thêm cột tsvector
ALTER TABLE user_entity
ADD COLUMN document_tsv tsvector;

-- 2. Cập nhật dữ liệu hiện có
UPDATE user_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(user_name,'') || ' ' || coalesce(user_email,'') || ' ' || coalesce(user_phone_number,'')
);

-- 3. Tạo GIN index để search nhanh
CREATE INDEX idx_user_document_tsv ON user_entity USING GIN(document_tsv);

-- 4. Tạo function trigger để cập nhật tự động khi insert/update
CREATE FUNCTION user_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(NEW.user_name,'') || ' ' || coalesce(NEW.user_email,'') || ' ' || coalesce(NEW.user_phone_number,'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- 5. Tạo trigger gắn vào bảng
CREATE TRIGGER tsvectorupdate_user BEFORE INSERT OR UPDATE
ON user_entity FOR EACH ROW EXECUTE PROCEDURE user_tsv_trigger();

-- ===== BRAND ENTITY SEARCH =====

ALTER TABLE brand_entity
ADD COLUMN document_tsv tsvector;

UPDATE brand_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(brand_name,'') || ' ' || coalesce(brand_code,'') || ' ' || coalesce(description,'')
);

CREATE INDEX idx_brand_document_tsv ON brand_entity USING GIN(document_tsv);

CREATE FUNCTION brand_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(NEW.brand_name,'') || ' ' || coalesce(NEW.brand_code,'') || ' ' || coalesce(NEW.description,'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate_brand BEFORE INSERT OR UPDATE
ON brand_entity FOR EACH ROW EXECUTE PROCEDURE brand_tsv_trigger();

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
ADD COLUMN document_tsv tsvector;

UPDATE category_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(category_name,'') || ' ' || coalesce(category_code,'') || ' ' || coalesce(description,'')
);

CREATE INDEX idx_category_document_tsv ON category_entity USING GIN(document_tsv);

CREATE FUNCTION category_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(NEW.category_name,'') || ' ' || coalesce(NEW.category_code,'') || ' ' || coalesce(NEW.description,'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate_category BEFORE INSERT OR UPDATE
ON category_entity FOR EACH ROW EXECUTE PROCEDURE category_tsv_trigger();

-- ===== SUB CATEGORY ENTITY SEARCH =====

ALTER TABLE sub_category_entity
ADD COLUMN document_tsv tsvector;

UPDATE sub_category_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(sub_category_name,'') || ' ' || coalesce(sub_category_code,'') || ' ' || coalesce(description,'')
);

CREATE INDEX idx_sub_category_document_tsv ON sub_category_entity USING GIN(document_tsv);

CREATE FUNCTION sub_category_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(NEW.sub_category_name,'') || ' ' || coalesce(NEW.sub_category_code,'') || ' ' || coalesce(NEW.description,'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate_sub_category BEFORE INSERT OR UPDATE
ON sub_category_entity FOR EACH ROW EXECUTE PROCEDURE sub_category_tsv_trigger();

-- ===== COLLECTION ENTITY SEARCH =====

ALTER TABLE collection_entity
ADD COLUMN document_tsv tsvector;

UPDATE collection_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(collection_name,'') || ' ' || coalesce(collection_code,'') || ' ' || coalesce(description,'')
);

CREATE INDEX idx_collection_document_tsv ON collection_entity USING GIN(document_tsv);

CREATE FUNCTION collection_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(NEW.collection_name,'') || ' ' || coalesce(NEW.collection_code,'') || ' ' || coalesce(NEW.description,'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate_collection BEFORE INSERT OR UPDATE
ON collection_entity FOR EACH ROW EXECUTE PROCEDURE collection_tsv_trigger();


-- ===== MATERIAL ENTITY SEARCH =====

ALTER TABLE material_entity
ADD COLUMN document_tsv tsvector;

UPDATE material_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(material_name,'') || ' ' || coalesce(material_code,'') || ' ' || coalesce(description,'')
);

CREATE INDEX idx_material_document_tsv ON material_entity USING GIN(document_tsv);

CREATE FUNCTION material_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(NEW.material_name,'') || ' ' || coalesce(NEW.material_code,'') || ' ' || coalesce(NEW.description,'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate_material BEFORE INSERT OR UPDATE
ON material_entity FOR EACH ROW EXECUTE PROCEDURE material_tsv_trigger();

-- ===== PRODUCT ENTITY SEARCH =====

ALTER TABLE product_entity
ADD COLUMN document_tsv tsvector;

UPDATE product_entity
SET document_tsv = to_tsvector(
    'simple',
    coalesce(product_name,'') || ' ' ||
    coalesce(product_code,'') || ' ' ||
    coalesce(origin,'') || ' ' ||
    coalesce(fit_type,'') || ' ' ||
    coalesce(description,'')
);

CREATE INDEX idx_product_document_tsv ON product_entity USING GIN(document_tsv);

CREATE FUNCTION product_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.document_tsv := to_tsvector(
        'simple',
        coalesce(NEW.product_name,'') || ' ' ||
        coalesce(NEW.product_code,'') || ' ' ||
        coalesce(NEW.origin,'') || ' ' ||
        coalesce(NEW.fit_type,'') || ' ' ||
        coalesce(NEW.description,'')
    );
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate_product BEFORE INSERT OR UPDATE
ON product_entity FOR EACH ROW EXECUTE PROCEDURE product_tsv_trigger();

