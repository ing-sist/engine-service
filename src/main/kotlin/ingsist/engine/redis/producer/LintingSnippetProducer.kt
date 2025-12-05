package ingsist.engine.redis.producer

import com.fasterxml.jackson.databind.ObjectMapper
import ingsist.engine.runner.dto.LintingComplianceStatusDto
import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class LintingSnippetProducer
    @Autowired
    constructor(
        @Value("\${stream.linting.compliance.key}") streamKey: String,
        redis: RedisTemplate<String, String>,
        val objectMapper: ObjectMapper,
    ) : LintingComplianceProducer, RedisStreamProducer(streamKey, redis) {
        override fun publishCompliance(compliance: LintingComplianceStatusDto) {
            val json = objectMapper.writeValueAsString(compliance)
            emit(json)
        }
    }
