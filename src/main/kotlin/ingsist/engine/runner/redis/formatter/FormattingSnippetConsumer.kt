package ingsist.engine.runner.redis.formatter
import ingsist.engine.runner.service.RunnerService
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
        @Value("\${stream.key}") streamKey: String,
        @Value("\${groups.product}") groupId: String,
        private val formattingService: RunnerService,
    ) : RedisStreamConsumer<FormatSnippetEventDTO>(streamKey, groupId, redis) {
        override fun options(): StreamReceiver.StreamReceiverOptions<
            String,
            ObjectRecord<String, FormatSnippetEventDTO>,
        > {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(Duration.ofMillis(10000))
                .targetType(FormatSnippetEventDTO::class.java)
                .build()
        }

        override fun onMessage(record: ObjectRecord<String, FormatSnippetEventDTO>) {
            println("Id: ${record.id}, Stream: ${record.stream}, Group: $groupId")
            formattingService.formatAndSaveSnippet(record.value.snippetId)
        }
    }
