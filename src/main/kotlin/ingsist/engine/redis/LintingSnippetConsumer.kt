package ingsist.engine.redis

import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
@Profile("!test")
class LintingSnippetConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${stream.linting.key}") streamKey: String,
        @Value("\${groups.lint}") groupId: String,
        private val lintingService: StreamService,
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
            println("arrived")
            println("Linting: Id: ${record.id}, Stream: ${record.stream}, Group: $groupId")
            val uuidId = UUID.fromString(record.value)
            lintingService.formatAndSaveSnippet(uuidId)
        }
    }
