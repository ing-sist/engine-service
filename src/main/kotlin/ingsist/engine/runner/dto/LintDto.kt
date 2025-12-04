package ingsist.engine.runner.dto

import java.util.UUID

data class LintReqDTO(
    val snippetId: UUID,
    val assetKey: String,
    val content: String,
    val version: String,
    val config: OwnerConfigDto,
)

data class LintResDTO(
    val snippetId: UUID,
    val report: List<String>,
)
