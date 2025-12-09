package ingsist.engine.redis.consumer

import ingsist.engine.asset.AssetService
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.FormattingRulesDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.LintingRulesDTO
import ingsist.engine.runner.dto.OwnerConfigDto
import ingsist.engine.runner.dto.StreamReqDto
import ingsist.engine.runner.service.RunnerService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RedisConsumerServiceTest {
    @Mock
    lateinit var assetService: AssetService

    @Mock
    lateinit var runnerService: RunnerService

    @InjectMocks
    lateinit var redisConsumerService: RedisConsumerService

    private fun <T> any(value: T): T {
        Mockito.any<T>()
        return value
    }

    @Test
    fun `should format and save snippet`() {
        val snippetId = UUID.randomUUID()
        val assetKey = "assetKey"
        val content = "content"
        val version = "1.0"
        val language = "kotlin"
        val config =
            OwnerConfigDto(
                LintingRulesDTO("camelCase", true, true),
                FormattingRulesDTO(4, true, true, true, true, true, 1, true, true, true, 1, true),
            )
        val streamReqDto = StreamReqDto(snippetId, assetKey, version, language, config)

        val dummyFormatReq = FormatReqDTO(snippetId, assetKey, content, version, language, config)

        `when`(assetService.get("snippets", assetKey)).thenReturn(content)

        redisConsumerService.formatAndSaveSnippet(streamReqDto)

        verify(assetService).get("snippets", assetKey)
        verify(runnerService).formatSnippet(any(dummyFormatReq))
    }

    @Test
    fun `should lint and save snippet`() {
        val snippetId = UUID.randomUUID()
        val assetKey = "assetKey"
        val content = "content"
        val version = "1.0"
        val language = "kotlin"
        val config =
            OwnerConfigDto(
                LintingRulesDTO("camelCase", true, true),
                FormattingRulesDTO(4, true, true, true, true, true, 1, true, true, true, 1, true),
            )
        val streamReqDto = StreamReqDto(snippetId, assetKey, version, language, config)

        val dummyLintReq = LintReqDTO(snippetId, assetKey, content, version, language, config)

        `when`(assetService.get("snippets", assetKey)).thenReturn(content)

        redisConsumerService.lintAndSaveSnippet(streamReqDto)

        verify(assetService).get("snippets", assetKey)
        verify(runnerService).lintSnippet(any(dummyLintReq))
    }
}
