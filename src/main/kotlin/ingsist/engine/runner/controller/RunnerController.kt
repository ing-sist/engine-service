package ingsist.engine.runner.controller

import ingsist.engine.runner.dto.ExecuteReqDTO
import ingsist.engine.runner.dto.ExecuteResDTO
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.FormatResDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.LintResDTO
import ingsist.engine.runner.dto.SupportedLanguageDto
import ingsist.engine.runner.dto.ValidateReqDto
import ingsist.engine.runner.dto.ValidateResDto
import ingsist.engine.runner.service.RunnerService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/engine")
@Validated
class RunnerController(
    private val runnerService: RunnerService,
) {
    val log = LoggerFactory.getLogger(RunnerController::class.java)

    @PostMapping("/lint")
    fun lint(
        @Valid @RequestBody req: LintReqDTO,
    ): ResponseEntity<LintResDTO> {
        log.info("Engine Service received lint request for language: ${req.language}, version: ${req.version}")
        val response = runnerService.lintSnippet(req)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/format")
    fun format(
        @Valid @RequestBody req: FormatReqDTO,
    ): FormatResDTO {
        log.info("Engine Service receivedformat request for language: ${req.language}, version: ${req.version}")
        return runnerService.formatSnippet(req)
    }

    @PostMapping("/validate")
    fun validate(
        @Valid @RequestBody req: ValidateReqDto,
    ): ResponseEntity<ValidateResDto> {
        log.info("Engine Service received validation request for snippet in lan: ${req.language}, ver: ${req.version}")
        return ResponseEntity.ok(
            runnerService.validateSnippet(req),
        )
    }

    @PostMapping("/execute")
    fun execute(
        @Valid @RequestBody req: ExecuteReqDTO,
    ): ResponseEntity<ExecuteResDTO> {
        log.info("Engine Service received execute request for language: ${req.language}, version: ${req.version}")
        return ResponseEntity.ok(
            runnerService.executeSnippet(req),
        )
    }

    @GetMapping("/code/{assetKey}")
    fun getSnippetCode(
        @PathVariable assetKey: String,
    ): ResponseEntity<String> {
        log.info("Engine Service received request to get code for assetKey: $assetKey")
        val code = runnerService.getSnippetCode(assetKey)
        log.info("Code for assetKey: $assetKey retrieved successfully")
        return ResponseEntity.ok(code)
    }

    @GetMapping("/languages")
    fun getSupportedLanguages(): ResponseEntity<List<SupportedLanguageDto>> {
        log.info("Engine Service received request to get supported languages")
        val response = runnerService.getSupportedLanguages()
        log.info("Supported languages retrieved successfully")
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/code/{assetKey}")
    fun deleteSnippet(
        @PathVariable assetKey: String,
    ): ResponseEntity<Void> {
        log.info("Engine Service received request to delete snippet with assetKey: $assetKey")
        runnerService.deleteSnippet(assetKey)
        log.info("Snippet with assetKey: $assetKey deleted successfully")
        return ResponseEntity.noContent().build()
    }
}
