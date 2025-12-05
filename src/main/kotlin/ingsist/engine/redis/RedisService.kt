package ingsist.engine.redis

import ingsist.engine.asset.AssetService
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.dto.StreamReqDto
import ingsist.engine.runner.service.RunnerService
import org.springframework.stereotype.Service

@Service
class RedisService(
    private val assetService: AssetService,
    private val runnerService: RunnerService,
) : StreamService {
    override fun formatAndSaveSnippet(snippet: StreamReqDto) {
        val content = assetService.get("snippets", snippet.assetKey)
        runnerService.formatSnippet(
            FormatReqDTO(
                snippet.id,
                snippet.assetKey,
                content,
                snippet.version,
                snippet.config,
            ),
        )
    }

    override fun lintAndSaveSnippet(snippet: StreamReqDto) {
        val content = assetService.get("snippets", snippet.assetKey)
        runnerService.lintSnippet(
            LintReqDTO(
                snippet.id,
                snippet.assetKey,
                content,
                snippet.version,
                snippet.config,
            ),
        )
    }
}
