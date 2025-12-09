package ingsist.engine.redis.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import ingsist.engine.runner.dto.StreamReqDto
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate

@ExtendWith(MockitoExtension::class)
class LintingSnippetConsumerTest {
    @Mock
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    lateinit var consumerStreamService: ConsumerStreamService

    @Mock
    lateinit var objectMapper: ObjectMapper

    @Mock
    lateinit var objectRecord: ObjectRecord<String, String>

    @Mock
    lateinit var streamReqDto: StreamReqDto

    @Test
    fun `should process message successfully`() {
        val streamKey = "streamKey"
        val groupId = "groupId"
        val json = "json"

        val consumer =
            LintingSnippetConsumer(
                redisTemplate,
                streamKey,
                groupId,
                consumerStreamService,
                objectMapper,
            )

        `when`(objectRecord.value).thenReturn(json)
        `when`(objectMapper.readValue(json, StreamReqDto::class.java)).thenReturn(streamReqDto)

        consumer.onMessage(objectRecord)

        verify(consumerStreamService).lintAndSaveSnippet(streamReqDto)
    }

    @Test
    fun `should return correct options`() {
        val streamKey = "streamKey"
        val groupId = "groupId"

        val consumer =
            LintingSnippetConsumer(
                redisTemplate,
                streamKey,
                groupId,
                consumerStreamService,
                objectMapper,
            )

        val options = consumer.options()
        assertNotNull(options)
    }
}
