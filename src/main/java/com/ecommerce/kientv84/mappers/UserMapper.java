package com.ecommerce.kientv84.mappers;

import com.ecommerce.kientv84.dtos.response.UserResponse;
import com.ecommerce.kientv84.dtos.response.kafka.KafkaUserResponse;
import com.ecommerce.kientv84.entites.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse mapToUserResponse(UserEntity userEntity);

    @Mapping(target = "userId", source = "id")
    KafkaUserResponse mapToKafkaUserResponse(UserEntity userEntity);
}
