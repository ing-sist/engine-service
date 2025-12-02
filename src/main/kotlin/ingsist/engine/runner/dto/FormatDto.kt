package ingsist.engine.runner.dto

import java.util.UUID

data class FormatReqDTO(
    val snippetId: UUID,
    val assetKey: String,
    val content: String,
    val version: String,
    val config: OwnerConfigDTO,
)

data class FormatResDTO(
    val snippetId: UUID,
    val content: String,
    val errors: List<String> = emptyList(),
)
