package ingsist.engine.redis.producer

import ingsist.engine.runner.dto.LintingComplianceStatusDto

interface LintingComplianceProducer {
    fun publishCompliance(compliance: LintingComplianceStatusDto)
}
