package ingsist.engine.runner.service

import Diagnostic
import PrintScriptEngine
import Report
import com.fasterxml.jackson.databind.ObjectMapper
import ingsist.engine.asset.AssetService
import ingsist.engine.redis.producer.LintingComplianceProducer
import ingsist.engine.runner.dto.ComplianceStatus
import ingsist.engine.runner.dto.ExecuteReqDTO
import ingsist.engine.runner.dto.ExecuteResDTO
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.FormatResDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.LintResDTO
import ingsist.engine.runner.dto.LintingComplianceStatusDto
import ingsist.engine.runner.dto.ValidateReqDto
import ingsist.engine.runner.dto.ValidateResDto
import ingsist.engine.runner.utils.FileAdapter
import ingsist.engine.runner.utils.exception.ProcessException
import ingsist.engine.runner.utils.exception.ValidationException
import language.errors.InterpreterException
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
    private val lintingComplianceProducer: LintingComplianceProducer,
) : RunnerService {
    override fun lintSnippet(req: LintReqDTO): LintResDTO {
        @Suppress("UNCHECKED_CAST")
        val configMap =
            objectMapper.convertValue(req.config, Map::class.java) as Map<String, Any>

        val response =
            fileAdapter.withTempFiles(
                req.content,
                configMap,
            ) { codeFile, configFile ->
                val engine = createEngine(req.version)
                engine.setAnalyzerConfig(configFile.absolutePath)
                val report = engine.analyze(codeFile.absolutePath, progressReporter)

                mapReportToLintResponse(req.snippetId, report)
            }

        val lintingStatus =
            if
                (response.report.isEmpty()) {
                ComplianceStatus.COMPLIANT
            } else {
                ComplianceStatus.NON_COMPLIANT
            }
        lintingComplianceProducer.publishCompliance(
            LintingComplianceStatusDto(
                req.snippetId,
                lintingStatus,
            ),
        )
        return response
    }

    override fun formatSnippet(req: FormatReqDTO): FormatResDTO {
        @Suppress("UNCHECKED_CAST")
        val configMap = objectMapper.convertValue(req.config, Map::class.java) as Map<String, Any>
        val response =
            fileAdapter.withTempFiles(req.content, configMap) { codeFile, configFile ->
                val engine =
                    try {
                        createEngine(req.version)
                    } catch (e: IllegalArgumentException) {
                        throw ValidationException("Version '${req.version}' is not a valid version for PrintScript.", e)
                    }
                engine.setFormatterConfig(configFile.absolutePath)

                var formattedContent: String
                val errors = mutableListOf<String>()

                try {
                    formattedContent = engine.format(codeFile.absolutePath, progressReporter)
                } catch (e: IllegalStateException) {
                    throw ProcessException("Error al formatear: ${e.message}", e)
                } catch (e: IOException) {
                    throw ProcessException("Error de I/O al formatear: ${e.message}", e)
                }

                FormatResDTO(req.snippetId, formattedContent, errors)
            }

        assetService.upload("snippets", req.assetKey, req.content)
        return response
    }

    override fun executeSnippet(req: ExecuteReqDTO): ExecuteResDTO {
        val response =
            fileAdapter.withTempFile(req.content, ".ps") { codeFile ->
                val engine =
                    try {
                        createEngine(req.version)
                    } catch (e: IllegalArgumentException) {
                        throw ValidationException("Version '${req.version}' is not a valid version for PrintScript.", e)
                    }
                val outputs = mutableListOf<String>()
                val errors = mutableListOf<String>()

                try {
                    val output = engine.execute(codeFile.absolutePath, progressReporter)
                    if (output.isNotEmpty()) {
                        outputs.addAll(output.lines())
                    }
                } catch (e: InterpreterException) {
                    errors.add(e.message ?: "Error de ejecución desconocido")
                } catch (e: IOException) {
                    throw ProcessException("Error al leer/escribir archivo de ejecución", e)
                }

                ExecuteResDTO(req.snippetId, outputs, errors)
            }

        return response
    }

    override fun validateSnippet(req: ValidateReqDto): ValidateResDto {
        val response =
            fileAdapter.withTempFile(req.content, ".ps") { codeFile ->
                val engine =
                    try {
                        createEngine(req.version)
                    } catch (e: IllegalArgumentException) {
                        throw ValidationException("Version '${req.version}' is not a valid version for PrintScript.", e)
                    }

                try {
                    engine.validateSyntax(codeFile.absolutePath, progressReporter)
                    ValidateResDto(req.snippetId, emptyList())
                } catch (e: IllegalStateException) {
                    // 'validateSyntax' lanza 'error()'
                    throw ValidationException(e.message ?: "Error de validación desconocido", e)
                } catch (e: IOException) {
                    throw ProcessException("Error de I/O durante la validación", e)
                }
            }
        assetService.upload("snippets", req.assetKey, req.content)
        return response
    }

    private fun createEngine(version: String): PrintScriptEngine {
        return PrintScriptEngine().apply {
            setVersion(version)
        }
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
        // Obtenemos el archivo del bucket/storage
        return assetService.get("snippets", assetKey)
    }
}
