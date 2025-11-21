-- ======================================
-- V2__insert_sample_data.sql
-- ======================================

-- 1. Thêm dữ liệu mẫu cho bảng role_entity
INSERT INTO role_entity (
    role_name,
    role_description,
    status,
    created_by
)
VALUES
    ('ROLE_ADMIN', 'Quản trị viên hệ thống', 'ACTIVE', 'SYSTEM'),
    ('ROLE_USER', 'Người dùng thông thường', 'ACTIVE', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- 2. Thêm dữ liệu mẫu cho bảng user_entity
INSERT INTO user_entity (
    user_code,
    user_name,
    user_birthday,
    user_gender,
    user_phone_number,
    user_address,
    user_email,
    role_id,
    user_password,
    status,
    created_by,
    updated_by
)
VALUES (
    'USER001',
    'Trương Chí Kiên',
    '2002-11-15',
    'Nam',
    '0968727900',
    '268/23 Lê Văn Việt, Tăng Nhơn Phú B, Quận 9, TP.HCM',
    'truongchikien2021@example.com',
    (SELECT id FROM role_entity WHERE role_name = 'ROLE_ADMIN' LIMIT 1),
    '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu',
    'active',
    'admin',
    'admin'
),('USER002', 'Kien1', '2002-11-15', 'Nam', '0968000001', '268/23 Lê Văn Việt, Tăng Nhơn Phú B, Quận 9, TP.HCM', 'kien1@example.com', (SELECT id FROM role_entity WHERE role_name = 'ROLE_ADMIN' LIMIT 1), '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu', 'active', 'admin', 'admin'),
  ('USER003', 'Kien2', '2002-11-15', 'Nam', '0968000002', '268/23 Lê Văn Việt, Tăng Nhơn Phú B, Quận 9, TP.HCM', 'kien2@example.com', (SELECT id FROM role_entity WHERE role_name = 'ROLE_ADMIN' LIMIT 1), '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu', 'active', 'admin', 'admin'),
  ('USER004', 'Kien3', '2002-11-15', 'Nam', '0968000003', '268/23 Lê Văn Việt, Tăng Nhơn Phú B, Quận 9, TP.HCM', 'kien3@example.com', (SELECT id FROM role_entity WHERE role_name = 'ROLE_ADMIN' LIMIT 1), '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu', 'active', 'admin', 'admin'),
  ('USER005', 'Kien4', '2002-11-15', 'Nam', '0968000004', '268/23 Lê Văn Việt, Tăng Nhơn Phú B, Quận 9, TP.HCM', 'kien4@example.com', (SELECT id FROM role_entity WHERE role_name = 'ROLE_ADMIN' LIMIT 1), '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu', 'active', 'admin', 'admin'),
  ('USER006', 'Kien5', '2002-11-15', 'Nam', '0968000005', '268/23 Lê Văn Việt, Tăng Nhơn Phú B, Quận 9, TP.HCM', 'kien5@example.com', (SELECT id FROM role_entity WHERE role_name = 'ROLE_ADMIN' LIMIT 1), '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu', 'active', 'admin', 'admin'),
  ('USER007', 'Nguyen Van A', '1990-05-20', 'Nam', '0968000010', '123 Đường ABC, Quận 1, TP.HCM', 'nguyenvana@example.com',
        (SELECT id FROM role_entity WHERE role_name = 'ROLE_USER' LIMIT 1),
        '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu',
        'inactive', 'admin', 'admin'),
    ('USER008', 'Tran Thi B', '1992-08-15', 'Nữ', '0968000011', '456 Đường DEF, Quận 2, TP.HCM', 'tranthib@example.com',
        (SELECT id FROM role_entity WHERE role_name = 'ROLE_USER' LIMIT 1),
        '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu',
        'inactive', 'admin', 'admin'),
    ('USER009', 'Le Van C', '1985-12-10', 'Nam', '0968000012', '789 Đường GHI, Quận 3, TP.HCM', 'levanc@example.com',
        (SELECT id FROM role_entity WHERE role_name = 'ROLE_USER' LIMIT 1),
        '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu',
        'deleted', 'admin', 'admin'),
    ('USER010', 'Pham Thi D', '1995-03-25', 'Nữ', '0968000013', '101 Đường JKL, Quận 4, TP.HCM', 'phamthid@example.com',
        (SELECT id FROM role_entity WHERE role_name = 'ROLE_USER' LIMIT 1),
        '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu',
        'deleted', 'admin', 'admin'),
    ('USER011', 'Hoang Van E', '1998-07-30', 'Nam', '0968000014', '202 Đường MNO, Quận 5, TP.HCM', 'hoangvane@example.com',
        (SELECT id FROM role_entity WHERE role_name = 'ROLE_USER' LIMIT 1),
        '$2a$10$j0xNytrAvZkRCwf2XPzChe9bH5LsEqdyGmxsYF7GvN4WMF.C9WyNu',
        'inactive', 'admin', 'admin')
ON CONFLICT (user_email) DO NOTHING;

-- 3. Thêm dữ liệu mẫu cho bảng brand_entity
INSERT INTO brand_entity (
    brand_name,
    brand_code,
    origin,
    description,
    thumbnail_url,
    status,
    created_by,
    updated_by
)
VALUES
    ('Gymshark', 'GYMSHARK', 'UK', 'Thương hiệu thể thao nổi tiếng', 'https://cdn.gymshark.com/logo.jpg', 'active', 'SYSTEM', 'SYSTEM'),
    ('Nike', 'NIKE', 'USA', 'Thương hiệu thể thao nổi tiếng toàn cầu', 'https://cdn.nike.com/logo.jpg', 'active', 'SYSTEM', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- 4. Thêm dữ liệu mẫu cho bảng category_entity
INSERT INTO category_entity (
    category_name,
    category_code,
    description,
    thumbnail_url,
    status,
    created_by,
    updated_by
)
VALUES
    ('Men', 'MEN', 'Thời trang nam', 'https://cdn.example.com/men.jpg', 'active', 'SYSTEM', 'SYSTEM'),
    ('Women', 'WOMEN', 'Thời trang nữ', 'https://cdn.example.com/women.jpg', 'active', 'SYSTEM', 'SYSTEM'),
     ('Accessory', 'ACCESSORY', 'Phụ Kiện', 'https://cdn.example.com/accessory.jpg', 'ACTIVE', 'SYSTEM', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- 5. Thêm dữ liệu mẫu cho bảng sub_category_entity
INSERT INTO sub_category_entity (
    sub_category_name,
    sub_category_code,
    description,
    thumbnail_url,
    status,
    category_id,
    created_by,
    updated_by
)
VALUES
    ('T-shirt', 'T_SHIRT', 'Áo thun nam', 'https://cdn.example.com/tshirt.jpg', 'active',
     (SELECT id FROM category_entity WHERE category_code='MEN' LIMIT 1), 'SYSTEM', 'SYSTEM'),
    ('Shorts', 'SHORTS', 'Quần short nam', 'https://cdn.example.com/shorts.jpg', 'active',
     (SELECT id FROM category_entity WHERE category_code='MEN' LIMIT 1), 'SYSTEM', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- 6. Thêm dữ liệu mẫu cho bảng collection_entity
INSERT INTO collection_entity (
    collection_name,
    collection_code,
    description,
    status,
    created_by,
    updated_by
)
VALUES
    ('Apex', 'APEX', 'Bộ sưu tập Apex', 'active', 'SYSTEM', 'SYSTEM'),
    ('Vital', 'VITAL', 'Bộ sưu tập Vital', 'active', 'SYSTEM', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- 7. Thêm dữ liệu mẫu cho bảng material_entity
INSERT INTO material_entity (
    material_name,
    material_code,
    description,
    status,
    created_by,
    updated_by
)
VALUES
    ('Polyester', 'POLY', 'Vải Polyester', 'active', 'SYSTEM', 'SYSTEM'),
    ('Nylon', 'NYLON', 'Vải Nylon', 'active', 'SYSTEM', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- 8. Thêm dữ liệu mẫu cho bảng product_entity
INSERT INTO product_entity (
    product_name,
    product_code,
    description,
    brand_id,
    category_id,
    sub_category_id,
    collection_id,
    material_id,
    base_price,
    discount_percent,
    origin,
    fit_type,
    care_instruction,
    count_in_stock,
    status,
    rating_average,
    rating_count,
    created_by,
    updated_by
)
VALUES (
    'Apex Seamless T-Shirt',
    'APEX-TSHIRT-001',
    'Áo tập thể thao nam với thiết kế co giãn, thoáng khí.',
    (SELECT id FROM brand_entity WHERE brand_code='GYMSHARK' LIMIT 1),
    (SELECT id FROM category_entity WHERE category_code='MEN' LIMIT 1),
    (SELECT id FROM sub_category_entity WHERE sub_category_code='T_SHIRT' LIMIT 1),
    (SELECT id FROM collection_entity WHERE collection_code='APEX' LIMIT 1),
    (SELECT id FROM material_entity WHERE material_code='NYLON' LIMIT 1),
    499000,
    10.0,
    'Vietnam',
    'Slim Fit',
    'Machine wash cold',
    50,
    'active',
    4.8,
    120,
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT DO NOTHING;

-- 9. Thêm dữ liệu mẫu cho bảng product_image_entity
INSERT INTO product_image_entity (
    product_id,
    image_url,
    sort_order
)
VALUES
       (
           (SELECT id FROM product_entity WHERE product_code = 'APEX-TSHIRT-001' LIMIT 1),
           'https://cdn.example.com/image1.jpg',
           1
       ),
       (
           (SELECT id FROM product_entity WHERE product_code = 'APEX-TSHIRT-001' LIMIT 1),
           'https://cdn.example.com/image2.jpg',
           2
       ),
       (
           (SELECT id FROM product_entity WHERE product_code = 'APEX-TSHIRT-001' LIMIT 1),
           'https://cdn.example.com/image3.jpg',
           3
       )
ON CONFLICT DO NOTHING;

---- Cập nhật cột document_tsv cho user_entity sau khi insert dữ liệu mẫu
--UPDATE user_entity
--SET document_tsv = to_tsvector(
--    'simple',
--    coalesce(user_name,'') || ' ' || coalesce(user_email,'') || ' ' || coalesce(user_phone_number,'')
--);

