package com.raf.mrworldwide.domain.dto.ai;

import java.time.ZonedDateTime;

public record AIMessageResponse(
        String reply,
        ZonedDateTime timestamp
) {}

