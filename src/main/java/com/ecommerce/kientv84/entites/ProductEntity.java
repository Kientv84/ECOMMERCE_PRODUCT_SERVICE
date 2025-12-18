package com.ecommerce.kientv84.entites;
import com.ecommerce.kientv84.commons.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_entity")
@EntityListeners(AuditingEntityListener.class)
public class ProductEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    // ====== Thông tin chung ======
    @Column(name ="product_name", nullable = false, length = 255)
    private String productName;

//    @Column(name ="product_code", nullable = false, length = 255)

    @Column(name ="product_code",nullable = false , length = 255)
    private String productCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id",nullable = false)
    private BrandEntity brand; // Gymshark, Nike, Adidas...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",nullable = false)
    private CategoryEntity category; // Men / Women / Accessories


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategoryEntity subCategory; // T-shirt / Shorts / Sports Bra...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private CollectionEntity collection; // Apex / Vital / Legacy...

    @Column(name ="base_price")
    private BigDecimal basePrice; // Giá gốc + float và double là kiểu số dấu phẩy động (floating-point) vd 0.30000000000000004 != 0.3

    @Column(name ="discount_percent")
    private Float discountPercent; // % giảm giá

    private String origin; // Xuất xứ (UK, Vietnam...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id") // Một material có thể dùng cho nhiều product
    private MaterialEntity material;

    @Column(name ="fit_type")
    private String fitType; // Slim fit, Regular fit...

    @Column(name ="care_instruction")
    private String careInstruction; // "Machine wash cold", ...

    @Column(name ="count_in_stock")
    private Integer stock;

    @Column(name = "status", length = 50)
    private String status; // ACTIVE / INACTIVE

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImageEntity> images = new ArrayList<>();

    @Column(name ="rating_average")
    private Double ratingAverage; // Trung bình đánh giá

    @Column(name ="rating_count")
    private Integer ratingCount; // Số lượng review

    // ===== Metadata =====
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name ="create_date", updatable = false)
    private Date createdDate;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name ="update_date")
    private Date updatedDate;

    @CreatedBy
    @Column(name ="created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name ="updated_by")
    private String updatedBy;

//    // ====== Quan hệ ======
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ProductVariantEntity> variants;
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ProductImageEntity> images;
}