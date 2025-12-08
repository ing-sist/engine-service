package ingsist.engine.redis.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import ingsist.engine.runner.dto.StreamReqDto
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("!test")
class FormattingSnippetConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${stream.formatting.key}") streamKey: String,
        @Value("\${groups.format}") groupId: String,
        private val formattingService: ConsumerStreamService,
        private val objectMapper: ObjectMapper,
    ) : RedisStreamConsumer<String>(streamKey, groupId, redis) {
        override fun options(): StreamReceiver.StreamReceiverOptions<
            String,
            ObjectRecord<String, String>,
        > {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(Duration.ofMillis(10000))
                .targetType(String::class.java)
                .build()
        }

        override fun onMessage(record: ObjectRecord<String, String>) {
            val json = record.value
            val dto = objectMapper.readValue(json, StreamReqDto::class.java)
            formattingService.formatAndSaveSnippet(dto)
        }
    }
