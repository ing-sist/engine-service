package ingsist.engine.runner.dto

import java.util.UUID

data class ExecuteReqDTO(
    val snippetId: UUID,
    val content: String,
    val version: String,
)

data class ExecuteResDTO(
    val snippetId: UUID,
    val outputs: List<String>,
    val errors: List<String>,
)
