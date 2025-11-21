package com.ecommerce.kientv84.commons;

import com.ecommerce.kientv84.exceptions.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EnumError {
    // lỗi 400: Lỗi logic BAD_REQUEST lõi validate
    // Lỗi 401: NOT_FOUND hoặc SERVICE_UNAVAILABLE ( cho system vd redis )
    // lỗi 500: INTERNAL_SERVER_ERROR lỗi hệ thống
    // lỗi 409: Lỗi conflict CONFLICT

    // ========== Validation ==========
//    ACC_U_V001("ACC-U-V001", "Invalid email format", HttpStatus.BAD_REQUEST),
//    ACC_U_V002("ACC-U-V002", "Password too short", HttpStatus.BAD_REQUEST),

    // ========== Business ==========
//    ACC_D_B001("ACC-D-B001", "No vehicles available in Redis", HttpStatus.NOT_FOUND),
//    ACC_D_B002("ACC-D-B002", "No vehicle matched type", HttpStatus.BAD_REQUEST),
//    ACC_D_B003("ACC-D-B003", "Driver not accepted booking", HttpStatus.BAD_REQUEST),

    // ========== System ==========
//    ACC_S_S001("ACC-S-S001", "Redis connection failed", HttpStatus.INTERNAL_SERVER_ERROR),
//    ACC_S_S002("ACC-S-S002", "Kafka publish error", HttpStatus.INTERNAL_SERVER_ERROR),

//----------- ACCOUNT ------------
    ACC_DATA_EXISTED("ACC-DTE", "Data exit", HttpStatus.CONFLICT),
    ACC_ERR_INVALID_PASSWORD("ACC_ERR_INVALID_PASSWORD", "Invalid password", HttpStatus.BAD_REQUEST),
    //get
    ACC_NOT_FOUND("ACC_NOT_FOUND", "Not found account with email", HttpStatus.BAD_REQUEST),
    ACC_ERR_GET("ACC-GA", "Have error in process get", HttpStatus.BAD_REQUEST),

    // update
    ACC_ERR_UPD("ACC-UD", "Have error in process update account", HttpStatus.BAD_REQUEST),

    //Delete
    ACC_ERR_DEL("ACC-DL", "Have error in process delete", HttpStatus.BAD_REQUEST),
    ACC_ERR_DEL_EM("ACC-DL-E", "List ids to delete is empty", HttpStatus.BAD_REQUEST),
    ACC_ERR_NOT_FOUND("ACC_EL_NF", "Not found user with id", HttpStatus.BAD_REQUEST),

    AUTH_ERR_INVALID_TOKEN("AUTH_ERR_INVALID_TOKEN", "Authentication token invalid", HttpStatus.BAD_REQUEST),
    //----------- UPLOAD ------------
    FILE_EMPTY("FILE_EMPTY", "File is empty", HttpStatus.BAD_REQUEST),
    FILE_INVALID("FILE_INVALID", "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "File too large (max 5MB)", HttpStatus.BAD_REQUEST),

    //----------- PRODUCT ------------
    PRO_ERR_GET("PRO-GA", "Have error in process get", HttpStatus.BAD_REQUEST),

    PRO_ERR_DEL("PRO-DL", "Have error in process delete", HttpStatus.BAD_REQUEST),
    PRO_ERR_DEL_EM("PRO-DL-E", "List ids to delete is empty", HttpStatus.BAD_REQUEST),
    PRO_ERR_NOT_FOUND("PRO_EL_NF", "Not found user with id", HttpStatus.BAD_REQUEST),

    //----------- PRODUCT IMAGE------------
    INVALID_REQUEST("PRO-INVALID_REQUEST", "No sortOrders provided", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND("IMAGE_NOT_FOUND", "No images found for given sortOrders", HttpStatus.BAD_REQUEST),

    //----------- CATEGORY ------------
    CATE_ERR_GET("CATE-GA", "Have error in process get", HttpStatus.BAD_REQUEST),
    CATE_ERR_DEL_EM("CATE-DL-E", "List ids to delete is empty", HttpStatus.BAD_REQUEST),
    CATE_DATA_EXISTED("CATE-DTE", "Data exit", HttpStatus.CONFLICT),
    CATE_ERR_NOT_FOUND("CATE_EL_NF", "Not found category with id", HttpStatus.BAD_REQUEST),

    //-----------  SUBCATEGORY ------------
    SUB_CATE_ERR_GET("SUB-CATE-GA", "Have error in process get", HttpStatus.BAD_REQUEST),
    SUB_CATE_ERR_DEL_EM("SUB-CATE-GA", "List ids to delete is empty", HttpStatus.BAD_REQUEST),
    SUB_CATE_DATA_EXISTED("SUB-CATE-GA", "Data exit", HttpStatus.CONFLICT),
    SUB_CATE_ERR_NOT_FOUND("SUB-CATE_NF", "Not found sub category with id", HttpStatus.BAD_REQUEST),

    //----------- BRAND ------------
    BRAND_ERR_GET("BRAND-GA", "Have error in process get brand", HttpStatus.BAD_REQUEST),
    BRAND_ERR_DEL_EM("BRAND-DL-E", "List ids of brand to delete is empty", HttpStatus.BAD_REQUEST),
    BRAND_DATA_EXISTED("BRAND-DTE", "Data brand exit", HttpStatus.CONFLICT),
    BRAND_ERR_NOT_FOUND("BRAND_NF", "Not found brand with id", HttpStatus.BAD_REQUEST),

    //----------- COLLECTION ------------
    COLLECTION_ERR_GET("COLLECTION-GA", "Have error in process get collection", HttpStatus.BAD_REQUEST),
    COLLECTION_ERR_DEL_EM("COLLECTION-DL-E", "List ids of collection to delete is empty", HttpStatus.BAD_REQUEST),
    COLLECTION_DATA_EXISTED("COLLECTION-DTE", "Data collection exit", HttpStatus.CONFLICT),
    COLLECTION_ERR_NOT_FOUND("COLLECTION_NF", "Not found collection with id", HttpStatus.BAD_REQUEST),

    //----------- MATERIAL ------------
    MATERIAL_ERR_GET("MATERIAL-GA", "Have error in process get material", HttpStatus.BAD_REQUEST),
    MATERIAL_ERR_DEL_EM("MATERIAL-DL-E", "List ids of brand to delete is empty", HttpStatus.BAD_REQUEST),
    MATERIAL_DATA_EXISTED("MATERIAL-DTE", "Data material exit", HttpStatus.CONFLICT),
    MATERIAL_ERR_NOT_FOUND("MATERIAL_NF", "Not found material with id", HttpStatus.BAD_REQUEST),

    UPLOAD_FAILED("UPLOAD_FAILED", "Failed to upload avata", HttpStatus.BAD_REQUEST),

    INTERNAL_ERROR("ACC-S-999", "Unexpected internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;



    public static EnumError fromCode(String code) {
        for (EnumError e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown DispatchError code: " + code);
    }
}
