package ingsist.engine.runner.controller

import ingsist.engine.runner.dto.ExecuteReqDTO
import ingsist.engine.runner.dto.ExecuteResDTO
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.FormatResDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.LintResDTO
import ingsist.engine.runner.dto.ValidateReqDto
import ingsist.engine.runner.dto.ValidateResDto
import ingsist.engine.runner.service.RunnerService
import jakarta.validation.Valid
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
    @PostMapping("/lint")
    fun lint(
        @Valid @RequestBody req: LintReqDTO,
    ): ResponseEntity<LintResDTO> {
        return ResponseEntity.ok(
            runnerService.lintSnippet(req),
        )
    }

    @PostMapping("/format")
    fun format(
        @Valid @RequestBody req: FormatReqDTO,
    ): FormatResDTO {
        return runnerService.formatSnippet(req)
    }

    @PostMapping("/validate")
    fun validate(
        @Valid @RequestBody req: ValidateReqDto,
    ): ResponseEntity<ValidateResDto> {
        return ResponseEntity.ok(
            runnerService.validateSnippet(req),
        )
    }

    @PostMapping("/execute")
    fun execute(
        @Valid @RequestBody req: ExecuteReqDTO,
    ): ResponseEntity<ExecuteResDTO> {
        return ResponseEntity.ok(
            runnerService.executeSnippet(req),
        )
    }

    @GetMapping("/code/{assetKey}")
    fun getSnippetCode(
        @PathVariable assetKey: String,
    ): ResponseEntity<String> {
        val code = runnerService.getSnippetCode(assetKey)
        return ResponseEntity.ok(code)
    }

    @DeleteMapping("/code/{assetKey}")
    fun deleteSnippet(
        @PathVariable assetKey: String,
    ): ResponseEntity<Void> {
        runnerService.deleteSnippet(assetKey)
        return ResponseEntity.noContent().build()
    }
}
