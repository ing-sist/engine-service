package ingsist.engine.redis.producer

import ingsist.engine.runner.dto.LintingConformanceStatusDto

interface LintingConformanceProducer {
    fun publishConformance(conformance: LintingConformanceStatusDto)
}
