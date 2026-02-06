package org.backendintern.Mapper;


import org.backendintern.Models.Entities.Doctor;
import org.backendintern.Models.Entities.Slot;
import org.backendintern.Payloads.Request.CreateSlotRequest;
import org.backendintern.Payloads.Response.SlotResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SlotMapper {



    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "currentCapacity" , constant = "0")
    Slot toEntity(CreateSlotRequest slotRequest , Doctor  doctor);

    @Mapping(source = "id" , target = "slotId")
    @Mapping(source = "doctor.id" , target = "doctorId")
    @Mapping(target = "full", expression = "java(slot.isFull())")
    SlotResponse toResponse(Slot slot);
}
