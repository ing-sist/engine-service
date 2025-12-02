package ingsist.engine.runner.service

import ingsist.engine.runner.dto.ExecuteReqDTO
import ingsist.engine.runner.dto.ExecuteResDTO
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.FormatResDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.LintResDTO
import ingsist.engine.runner.dto.ValidateReqDto
import ingsist.engine.runner.dto.ValidateResDto
import java.util.UUID

interface RunnerService {
    fun lintSnippet(req: LintReqDTO): LintResDTO

    fun formatSnippet(req: FormatReqDTO): FormatResDTO

    fun executeSnippet(req: ExecuteReqDTO): ExecuteResDTO

    fun validateSnippet(req: ValidateReqDto): ValidateResDto

    fun formatAndSaveSnippet(snippetId: UUID)
}
