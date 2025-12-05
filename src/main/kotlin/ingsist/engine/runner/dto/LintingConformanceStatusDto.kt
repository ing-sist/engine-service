package ingsist.engine.runner.dto

import java.util.UUID

data class LintingConformanceStatusDto(
    val snippetId: UUID,
    val status: ConformanceStatus,
)
