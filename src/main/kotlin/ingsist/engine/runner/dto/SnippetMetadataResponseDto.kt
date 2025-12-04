package ingsist.engine.runner.dto

import java.util.UUID

data class SnippetMetadataResponseDto(
    val id: UUID,
    val name: String,
    val language: String,
    val description: String,
    val ownerId: String,
    val version: String,
    val compliance: String? = "pending",
)
