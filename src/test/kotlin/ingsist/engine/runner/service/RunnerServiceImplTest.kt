package ingsist.engine.runner.service

import com.fasterxml.jackson.databind.ObjectMapper
import ingsist.engine.asset.AssetService
import ingsist.engine.redis.producer.LintingConformanceProducer
import ingsist.engine.runner.dto.ExecuteReqDTO
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.FormattingRulesDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.LintingRulesDTO
import ingsist.engine.runner.dto.OwnerConfigDto
import ingsist.engine.runner.dto.ValidateReqDto
import ingsist.engine.runner.utils.FileAdapter
import ingsist.engine.runner.utils.exception.ValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import progress.ProgressReporter
import java.io.File
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RunnerServiceImplTest {
    @Mock
    private lateinit var progressReporter: ProgressReporter

    @Mock
    private lateinit var fileAdapter: FileAdapter

    @Mock
    private lateinit var assetService: AssetService

    @Mock
    private lateinit var objectMapper: ObjectMapper

    @Mock
    private lateinit var lintingConformanceProducer: LintingConformanceProducer

    private lateinit var runnerService: RunnerServiceImpl

    @BeforeEach
    fun setUp() {
        runnerService =
            RunnerServiceImpl(
                progressReporter,
                fileAdapter,
                assetService,
                objectMapper,
                lintingConformanceProducer,
            )
    }

    private fun <T> any(value: T): T {
        Mockito.any<T>()
        return value
    }

    @Test
    fun `should get supported languages`() {
        val languages = runnerService.getSupportedLanguages()
        assertEquals(1, languages.size)
        assertEquals("printscript", languages[0].name)
    }

    @Test
    fun `should fail to create engine for unsupported language`() {
        val req =
            ValidateReqDto(
                snippetId = UUID.randomUUID(),
                assetKey = "key",
                content = "content",
                version = "1.1",
                language = "unsupported",
            )

        assertThrows(ValidationException::class.java) {
            runnerService.validateSnippet(req)
        }
    }

    @Test
    fun `should fail to create engine for unsupported version`() {
        val req =
            ValidateReqDto(
                snippetId = UUID.randomUUID(),
                assetKey = "key",
                content = "content",
                version = "9.9",
                language = "printscript",
            )

        `when`(fileAdapter.withTempFile<Any>(anyString(), anyString(), any { _: File -> })).thenAnswer { invocation ->
            val block = invocation.getArgument<(File) -> Any>(2)
            block(File.createTempFile("test", ".ps"))
        }

        assertThrows(ValidationException::class.java) {
            runnerService.validateSnippet(req)
        }
    }

    @Test
    fun `should validate snippet successfully`() {
        val req =
            ValidateReqDto(
                snippetId = UUID.randomUUID(),
                assetKey = "key",
                content = "println(1);",
                version = "1.1",
                language = "printscript",
            )

        `when`(fileAdapter.withTempFile<Any>(anyString(), anyString(), any { _: File -> })).thenAnswer { invocation ->
            val block = invocation.getArgument<(File) -> Any>(2)
            block(File.createTempFile("test", ".ps"))
        }

        val response = runnerService.validateSnippet(req)
        assertNotNull(response)
        assertEquals(req.snippetId, response.snippetId)
        assertTrue(response.error.isEmpty())
    }

    @Test
    fun `should execute snippet successfully`() {
        val req =
            ExecuteReqDTO(
                snippetId = UUID.randomUUID(),
                assetKey = "key",
                inputs = mutableListOf(),
                version = "1.1",
                language = "printscript",
            )

        `when`(assetService.get(anyString(), anyString())).thenReturn("println(1);")
        `when`(fileAdapter.withTempFile<Any>(anyString(), anyString(), any { _: File -> })).thenAnswer { invocation ->
            val block = invocation.getArgument<(File) -> Any>(2)
            block(File.createTempFile("test", ".ps"))
        }

        val response = runnerService.executeSnippet(req)
        assertNotNull(response)
        assertEquals(req.snippetId, response.snippetId)
    }

    @Test
    fun `should format snippet successfully`() {
        val req =
            FormatReqDTO(
                snippetId = UUID.randomUUID(),
                assetKey = "key",
                content = "println(1);",
                version = "1.1",
                language = "printscript",
                config =
                    OwnerConfigDto(
                        linting = LintingRulesDTO("camelCase", true, true),
                        formatting = FormattingRulesDTO(4, true, true, true, true, true, 1, true, true, true, 1, true),
                    ),
            )

        `when`(objectMapper.convertValue(Mockito.any(), eq(Map::class.java))).thenReturn(mapOf<String, Any>())
        `when`(
            fileAdapter.withTempFiles<Any>(
                anyString(),
                any(Object()),
                anyString(),
                any {
                        _: File,
                        _: File,
                    ->
                },
            ),
        ).thenAnswer { invocation ->
            val block = invocation.getArgument<(File, File) -> Any>(3)
            block(File.createTempFile("test", ".ps"), File.createTempFile("config", ".json"))
        }

        val response = runnerService.formatSnippet(req)
        assertNotNull(response)
        assertEquals(req.snippetId, response.snippetId)
    }

    @Test
    fun `should lint snippet successfully`() {
        val req =
            LintReqDTO(
                snippetId = UUID.randomUUID(),
                assetKey = "key",
                content = "println(1);",
                version = "1.1",
                language = "printscript",
                config =
                    OwnerConfigDto(
                        linting = LintingRulesDTO("camelCase", true, true),
                        formatting = FormattingRulesDTO(4, true, true, true, true, true, 1, true, true, true, 1, true),
                    ),
            )

        `when`(objectMapper.convertValue(Mockito.any(), eq(Map::class.java))).thenReturn(mapOf<String, Any>())
        `when`(
            fileAdapter.withTempFiles<Any>(
                anyString(),
                any(Object()),
                anyString(),
                any {
                        _: File,
                        _: File,
                    ->
                },
            ),
        ).thenAnswer { invocation ->
            val block = invocation.getArgument<(File, File) -> Any>(3)
            block(File.createTempFile("test", ".ps"), File.createTempFile("config", ".json"))
        }

        val response = runnerService.lintSnippet(req)
        assertNotNull(response)
        assertEquals(req.snippetId, response.snippetId)
    }

    @Test
    fun `should get snippet code successfully`() {
        val assetKey = "key"
        `when`(assetService.get("snippets", assetKey)).thenReturn("code")
        val result = runnerService.getSnippetCode(assetKey)
        assertEquals("code", result)
    }

    @Test
    fun `should delete snippet successfully`() {
        val assetKey = "key"
        runnerService.deleteSnippet(assetKey)
        Mockito.verify(assetService).delete("snippets", assetKey)
    }
}
