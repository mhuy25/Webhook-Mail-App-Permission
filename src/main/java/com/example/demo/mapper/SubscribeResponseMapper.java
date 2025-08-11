package com.example.demo.mapper;

import com.example.demo.dto.response.microsoft.SubscribeResponse;
import com.example.demo.entity.OutLookSubscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscribeResponseMapper {
    SubscribeResponse toSubscribeResponse(OutLookSubscription outLookSubscription);
}
