package ingsist.engine.redis.formatter
import ingsist.engine.redis.StreamService
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
class FormattingSnippetConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${stream.key}") streamKey: String,
        @Value("\${groups.product}") groupId: String,
        private val formattingService: StreamService,
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
            println("Id: ${record.id}, Stream: ${record.stream}, Group: $groupId")
            val uuidId = UUID.fromString(record.value)
            formattingService.formatAndSaveSnippet(uuidId)
        }
    }
