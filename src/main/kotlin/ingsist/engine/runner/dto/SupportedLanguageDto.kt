package ingsist.engine.runner.dto

data class SupportedLanguageDto(
    val name: String,
    val version: List<String>,
    val extension: String,
)
