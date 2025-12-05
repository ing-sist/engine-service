package ingsist.engine.runner.dto

import java.util.UUID

data class LintingComplianceStatusDto(
    val snippetId: UUID,
    val status: ComplianceStatus,
)
