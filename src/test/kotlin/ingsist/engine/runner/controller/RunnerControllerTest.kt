package ingsist.engine.runner.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ingsist.engine.asset.AssetService
import ingsist.engine.redis.consumer.FormattingSnippetConsumer
import ingsist.engine.redis.consumer.LintingSnippetConsumer
import ingsist.engine.redis.producer.LintingConformanceProducer
import ingsist.engine.runner.dto.ExecuteReqDTO
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.FormattingRulesDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.LintingRulesDTO
import ingsist.engine.runner.dto.OwnerConfigDto
import ingsist.engine.runner.dto.ValidateReqDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class RunnerControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var assetService: AssetService

    @MockBean
    lateinit var lintingConformanceProducer: LintingConformanceProducer

    @MockBean
    private lateinit var jwtDecoder: JwtDecoder

    @MockBean
    lateinit var redisTemplate: RedisTemplate<String, String>

    @MockBean
    lateinit var formattingSnippetConsumer: FormattingSnippetConsumer

    @MockBean
    lateinit var lintingSnippetConsumer: LintingSnippetConsumer

    @BeforeEach
    fun setUp() {
        `when`(jwtDecoder.decode(anyString())).thenReturn(
            Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("scope", "read")
                .audience(listOf("https://api.ingsis.com"))
                .issuer("https://ingsis.auth0.com/")
                .build(),
        )

        // Mock AssetService to return some code when requested
        `when`(assetService.get(anyString(), anyString())).thenReturn("println(1);")
        `when`(assetService.upload(anyString(), anyString(), anyString())).thenReturn("uploaded")
    }

    @Test
    fun `should lint code successfully`() {
        RunnerControllerDSL()
            .givenLintRequest(
                snippetId = UUID.randomUUID(),
                assetKey = "test-key",
                version = "1.1",
                language = "printscript",
            )
            .whenLintIsCalled()
            .thenStatusShouldBe(200)
    }

    @Test
    fun `should format code successfully`() {
        RunnerControllerDSL()
            .givenFormatRequest(
                snippetId = UUID.randomUUID(),
                assetKey = "test-key",
                version = "1.1",
                language = "printscript",
            )
            .whenFormatIsCalled()
            .thenStatusShouldBe(200)
    }

    @Test
    fun `should execute code successfully`() {
        RunnerControllerDSL()
            .givenExecuteRequest(
                snippetId = UUID.randomUUID(),
                assetKey = "test-key",
                version = "1.1",
                language = "printscript",
            )
            .whenExecuteIsCalled()
            .thenStatusShouldBe(200)
    }

    @Test
    fun `should validate code successfully`() {
        RunnerControllerDSL()
            .givenValidateRequest(
                snippetId = UUID.randomUUID(),
                assetKey = "test-key",
                version = "1.1",
                language = "printscript",
                content = "println(1);",
            )
            .whenValidateIsCalled()
            .thenStatusShouldBe(200)
    }

    inner class RunnerControllerDSL {
        private var lintReq: LintReqDTO? = null
        private var formatReq: FormatReqDTO? = null
        private var executeReq: ExecuteReqDTO? = null
        private var validateReq: ValidateReqDto? = null
        private var resultActions: org.springframework.test.web.servlet.ResultActions? = null

        fun givenLintRequest(
            snippetId: UUID,
            assetKey: String,
            version: String,
            language: String,
        ): RunnerControllerDSL {
            lintReq =
                LintReqDTO(
                    snippetId = snippetId,
                    assetKey = assetKey,
                    content = "println(1);",
                    version = version,
                    language = language,
                    config =
                        OwnerConfigDto(
                            linting =
                                LintingRulesDTO(
                                    identifierNamingType = "camelCase",
                                    printlnSimpleArg = true,
                                    readInputSimpleArg = true,
                                ),
                            formatting =
                                FormattingRulesDTO(
                                    indentation = 4,
                                    spaceBeforeColon = true,
                                    spaceAfterColon = true,
                                    spaceAroundAssignment = true,
                                    spaceAroundOperators = true,
                                    maxSpaceBetweenTokens = true,
                                    lineBreakBeforePrintln = 1,
                                    lineBreakAfterSemiColon = true,
                                    inlineBraceIfStatement = true,
                                    belowLineBraceIfStatement = true,
                                    braceLineBreak = 1,
                                    keywordSpacingAfter = true,
                                ),
                        ),
                )
            return this
        }

        fun givenFormatRequest(
            snippetId: UUID,
            assetKey: String,
            version: String,
            language: String,
        ): RunnerControllerDSL {
            formatReq =
                FormatReqDTO(
                    snippetId = snippetId,
                    assetKey = assetKey,
                    content = "println(1);",
                    version = version,
                    language = language,
                    config =
                        OwnerConfigDto(
                            linting =
                                LintingRulesDTO(
                                    identifierNamingType = "camelCase",
                                    printlnSimpleArg = true,
                                    readInputSimpleArg = true,
                                ),
                            formatting =
                                FormattingRulesDTO(
                                    indentation = 4,
                                    spaceBeforeColon = true,
                                    spaceAfterColon = true,
                                    spaceAroundAssignment = true,
                                    spaceAroundOperators = true,
                                    maxSpaceBetweenTokens = true,
                                    lineBreakBeforePrintln = 1,
                                    lineBreakAfterSemiColon = true,
                                    inlineBraceIfStatement = true,
                                    belowLineBraceIfStatement = true,
                                    braceLineBreak = 1,
                                    keywordSpacingAfter = true,
                                ),
                        ),
                )
            return this
        }

        fun givenExecuteRequest(
            snippetId: UUID,
            assetKey: String,
            version: String,
            language: String,
        ): RunnerControllerDSL {
            executeReq =
                ExecuteReqDTO(
                    snippetId = snippetId,
                    assetKey = assetKey,
                    inputs = mutableListOf(),
                    version = version,
                    language = language,
                )
            return this
        }

        fun givenValidateRequest(
            snippetId: UUID,
            assetKey: String,
            version: String,
            language: String,
            content: String,
        ): RunnerControllerDSL {
            validateReq =
                ValidateReqDto(
                    snippetId = snippetId,
                    assetKey = assetKey,
                    version = version,
                    language = language,
                    content = content,
                )
            return this
        }

        fun whenLintIsCalled(): RunnerControllerDSL {
            resultActions =
                mockMvc.perform(
                    post("/engine/lint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(objectMapper.writeValueAsString(lintReq)),
                )
            return this
        }

        fun whenFormatIsCalled(): RunnerControllerDSL {
            resultActions =
                mockMvc.perform(
                    post("/engine/format")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(objectMapper.writeValueAsString(formatReq)),
                )
            return this
        }

        fun whenExecuteIsCalled(): RunnerControllerDSL {
            resultActions =
                mockMvc.perform(
                    post("/engine/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(objectMapper.writeValueAsString(executeReq)),
                )
            return this
        }

        fun whenValidateIsCalled(): RunnerControllerDSL {
            resultActions =
                mockMvc.perform(
                    post("/engine/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(objectMapper.writeValueAsString(validateReq)),
                )
            return this
        }

        fun thenStatusShouldBe(status: Int): RunnerControllerDSL {
            resultActions?.andExpect(status().`is`(status))
            return this
        }
    }
}
