package org.backendintern.Mapper;

import org.backendintern.Models.Entities.Token;
import org.backendintern.Payloads.Response.TokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface   TokenMapper {

    TokenMapper INSTANCE = Mappers.getMapper(TokenMapper.class);

    @Mapping(source = "timeSlot.id", target = "slotId")
    @Mapping(source = "timeSlot.doctor.id", target = "doctorId")
    TokenResponse toResponse(Token token);
}
