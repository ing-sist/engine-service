package ingsist.engine.redis.producer

import com.fasterxml.jackson.databind.ObjectMapper
import ingsist.engine.runner.dto.LintingConformanceStatusDto
import org.austral.ingsis.redis.RedisStreamProducer
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class LintingSnippetProducer
    @Autowired
    constructor(
        @Value("\${stream.linting.conformance.key}") streamKey: String,
        redis: RedisTemplate<String, String>,
        val objectMapper: ObjectMapper,
    ) : LintingConformanceProducer, RedisStreamProducer(streamKey, redis) {
        companion object {
            private const val CORRELATION_ID_KEY = "correlation-id"
        }

        override fun publishConformance(conformance: LintingConformanceStatusDto) {
            val corrId = MDC.get(CORRELATION_ID_KEY)
            val conformanceWithId = conformance.copy(correlationId = corrId)
            val json = objectMapper.writeValueAsString(conformanceWithId)
            emit(json)
        }
    }
