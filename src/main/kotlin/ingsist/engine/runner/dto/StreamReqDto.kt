package ingsist.engine.runner.dto

import java.util.UUID

data class StreamReqDto(
    val id: UUID,
    val assetKey: String,
    val version: String,
    val language: String,
    val config: OwnerConfigDto,
    val correlationId: String? = null,
)
