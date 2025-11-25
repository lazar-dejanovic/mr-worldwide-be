package com.raf.mrworldwide.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@Schema(description = "Base class for entity data transfer objects")
public class BaseEntityDto {

	@Schema(description = "Unique identifier of the entity")
	protected UUID id;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Schema(description = "Date and time when the entity was created", accessMode = Schema.AccessMode.READ_ONLY)
	protected ZonedDateTime createdOn;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Schema(description = "Date and time when the entity was last updated", accessMode = Schema.AccessMode.READ_ONLY)
	protected ZonedDateTime updatedOn;

}
