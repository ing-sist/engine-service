package ingsist.engine.runner.service

import Diagnostic
import PrintScriptEngine
import Report
import com.fasterxml.jackson.databind.ObjectMapper
import ingsist.engine.asset.AssetService
import ingsist.engine.redis.producer.LintingConformanceProducer
import ingsist.engine.runner.dto.ConformanceStatus
import ingsist.engine.runner.dto.ExecuteReqDTO
import ingsist.engine.runner.dto.ExecuteResDTO
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.FormatResDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.LintResDTO
import ingsist.engine.runner.dto.LintingConformanceStatusDto
import ingsist.engine.runner.dto.SupportedLanguageDto
import ingsist.engine.runner.dto.ValidateReqDto
import ingsist.engine.runner.dto.ValidateResDto
import ingsist.engine.runner.utils.FileAdapter
import ingsist.engine.runner.utils.exception.ProcessException
import ingsist.engine.runner.utils.exception.ValidationException
import language.errors.InterpreterException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import progress.ProgressReporter
import java.io.IOException
import java.util.UUID

@Service
class RunnerServiceImpl(
    private val progressReporter: ProgressReporter,
    private val fileAdapter: FileAdapter,
    private val assetService: AssetService,
    private val objectMapper: ObjectMapper,
    private val lintingconformanceProducer: LintingConformanceProducer,
) : RunnerService {
    val log = LoggerFactory.getLogger(RunnerServiceImpl::class.java)

    private val supportedLanguages =
        listOf(
            SupportedLanguageDto("printscript", listOf("1.0", "1.1"), "ps"),
        )

    override fun getSupportedLanguages(): List<SupportedLanguageDto> {
        log.info("Engine service fetching supported languages")
        return supportedLanguages
    }

    override fun lintSnippet(req: LintReqDTO): LintResDTO {
        @Suppress("UNCHECKED_CAST")
        val configMap =
            objectMapper.convertValue(req.config, Map::class.java) as Map<String, Any>

        val response =
            fileAdapter.withTempFiles(
                req.content,
                configMap,
                getLanguageExtension(req.language),
            ) { codeFile, configFile ->
                val engine = createEngine(req.language, req.version)
                engine.setAnalyzerConfig(configFile.absolutePath)
                val report = engine.analyze(codeFile.absolutePath, progressReporter)

                mapReportToLintResponse(req.snippetId, report)
            }

        log.info("Publishing linting conformance status for snippetId: ${req.snippetId}")

        val lintingStatus =
            if
                (response.report.isEmpty()) {
                ConformanceStatus.COMPLIANT
            } else {
                ConformanceStatus.NON_COMPLIANT
            }
        lintingconformanceProducer.publishConformance(
            LintingConformanceStatusDto(
                req.snippetId,
                lintingStatus,
            ),
        )
        log.info("Published linting conformance status for snippetId: ${req.snippetId} with status: $lintingStatus")
        return response
    }

    override fun formatSnippet(req: FormatReqDTO): FormatResDTO {
        @Suppress("UNCHECKED_CAST")
        val configMap = objectMapper.convertValue(req.config, Map::class.java) as Map<String, Any>
        val response =
            fileAdapter.withTempFiles(
                req.content,
                configMap,
                getLanguageExtension(req.language),
            ) { codeFile, configFile ->
                val engine =
                    try {
                        log.info("Created engine for language: ${req.language}, version: ${req.version}")
                        createEngine(req.language, req.version)
                    } catch (e: IllegalArgumentException) {
                        log.error("Failed to create engine for language: ${req.language}, version: ${req.version}")
                        throw ValidationException("Version '${req.version}' is not a valid version for PrintScript.", e)
                    }
                engine.setFormatterConfig(configFile.absolutePath)

                var formattedContent: String
                val errors = mutableListOf<String>()

                try {
                    log.info("Formatting snippetId: ${req.snippetId}")
                    formattedContent = engine.format(codeFile.absolutePath, progressReporter)
                } catch (e: IllegalStateException) {
                    log.error("Formatting failed for snippetId: ${req.snippetId} with error: ${e.message}")
                    throw ProcessException("Error al formatear: ${e.message}", e)
                } catch (e: IOException) {
                    log.error("I/O error during formatting for snippetId: ${req.snippetId} with error: ${e.message}")
                    throw ProcessException("Error de I/O al formatear: ${e.message}", e)
                }

                FormatResDTO(req.snippetId, formattedContent, errors)
            }

        log.info("Uploading formatted snippetId: ${req.snippetId} to asset service")
        assetService.upload("snippets", req.assetKey, req.content)
        log.info("Uploaded formatted snippetId: ${req.snippetId} to asset service")
        return response
    }

    override fun executeSnippet(req: ExecuteReqDTO): ExecuteResDTO {
        val content = assetService.get("snippets", req.assetKey)

        val response =
            fileAdapter.withTempFile(content, getLanguageExtension(req.language)) { codeFile ->
                val engine =
                    try {
                        log.info("Created engine for language: ${req.language}, version: ${req.version}")
                        createEngine(req.language, req.version)
                    } catch (e: IllegalArgumentException) {
                        log.error("Failed to create engine for language: ${req.language}, version: ${req.version}")
                        throw ValidationException("Version '${req.version}' is not a valid version for PrintScript.", e)
                    }
                val outputs = mutableListOf<String>()
                val errors = mutableListOf<String>()

                try {
                    log.info("Executing snippetId: ${req.snippetId}")

                    val output = engine.execute(codeFile.absolutePath, progressReporter, req.inputs)

                    if (output.isNotEmpty()) {
                        outputs.addAll(output.lines())
                    }
                } catch (e: InterpreterException) {
                    log.error("Execution failed for snippetId: ${req.snippetId} with error: ${e.message}")
                    errors.add(e.message ?: "Error de ejecución desconocido")
                } catch (e: IOException) {
                    log.error("I/O error during execution for snippetId: ${req.snippetId} with error: ${e.message}")
                    throw ProcessException("Error al leer/escribir archivo de ejecución", e)
                }

                ExecuteResDTO(req.snippetId, outputs, errors)
            }

        log.info("Uploading executed snippetId: ${req.snippetId} to asset service")
        return response
    }

    override fun validateSnippet(req: ValidateReqDto): ValidateResDto {
        val response =
            fileAdapter.withTempFile(req.content, getLanguageExtension(req.language)) { codeFile ->
                val engine =
                    try {
                        log.info("Created engine for language: ${req.language}, version: ${req.version}")
                        createEngine(req.language, req.version)
                    } catch (e: IllegalArgumentException) {
                        log.error("Failed to create engine for language: ${req.language}, version: ${req.version}")
                        throw ValidationException("Version '${req.version}' is not a valid version for PrintScript.", e)
                    }

                try {
                    log.info("Validating syntax for snippetId: ${req.snippetId}")
                    engine.validateSyntax(codeFile.absolutePath, progressReporter)
                    ValidateResDto(req.snippetId, emptyList())
                } catch (e: IllegalStateException) {
                    log.error("Validation failed for snippetId: ${req.snippetId} with error: ${e.message}")
                    throw ValidationException(e.message ?: "Error de validación desconocido", e)
                } catch (e: IOException) {
                    log.error("I/O error during validation for snippetId: ${req.snippetId} with error: ${e.message}")
                    throw ProcessException("Error de I/O durante la validación", e)
                }
            }
        log.info("Uploading validated snippetId: ${req.snippetId} to asset service")
        assetService.upload("snippets", req.assetKey, req.content)
        log.info("Uploaded validated snippetId: ${req.snippetId} to asset service")
        return response
    }

    private fun createEngine(
        language: String,
        version: String,
    ): PrintScriptEngine {
        validateLanguageSupport(language, version)

        return when (language.lowercase()) {
            "printscript" -> PrintScriptEngine().apply { setVersion(version) }
            else -> throw ValidationException("Engine definition missing for language '$language'")
        }
    }

    private fun validateLanguageSupport(
        language: String,
        version: String,
    ) {
        val langConfig =
            supportedLanguages.find { it.name.equals(language, ignoreCase = true) }
                ?: throw ValidationException("Language '$language' is not supported by this engine.")

        if (!langConfig.version.contains(version)) {
            throw ValidationException("Version '$version' is not supported for language '$language'.")
        }
    }

    private fun getLanguageExtension(language: String): String {
        val langConfig =
            supportedLanguages.find { it.name.equals(language, ignoreCase = true) }
                ?: throw ValidationException("Language '$language' is not supported by this engine.")
        return ".${langConfig.extension}"
    }

    private fun mapReportToLintResponse(
        snippetId: UUID,
        report: Report,
    ): LintResDTO {
        val diagnosticsList = mutableListOf<Diagnostic>()
        report.forEach { diagnosticsList.add(it) }

        val reportStrings =
            diagnosticsList.map {
                "L${it.location.line}: ${it.message} (${it.type})"
            }
        return LintResDTO(snippetId, reportStrings)
    }

    override fun getSnippetCode(assetKey: String): String {
        return assetService.get("snippets", assetKey)
    }

    override fun deleteSnippet(assetKey: String) {
        assetService.delete("snippets", assetKey)
    }
}
