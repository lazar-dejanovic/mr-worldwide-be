package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.ai.AIInteractionDto;
import com.raf.mrworldwide.domain.entities.ai.AIInteraction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AIInteractionMapper {

    AIInteractionMapper INSTANCE = Mappers.getMapper(AIInteractionMapper.class);

    @Mapping(target = "base", source = "interaction")
    AIInteractionDto toDto(AIInteraction interaction);

    default BaseEntityDto toBase(AIInteraction interaction) {
        if (interaction == null) return null;
        return new BaseEntityDto(interaction.getId(), interaction.getCreatedOn(), interaction.getUpdatedOn());
    }
}

