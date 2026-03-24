package com.raf.mrworldwide.domain.dto.trip;

import java.util.UUID;

public record SegmentOrderItem(
        UUID segmentId,
        Integer orderIndex
) {}

