package ingsist.engine.redis

import SnippetInfoDto
import ingsist.engine.asset.AssetService
import ingsist.engine.config.SnippetClient
import ingsist.engine.runner.dto.FormatReqDTO
import ingsist.engine.runner.dto.LintReqDTO
import ingsist.engine.runner.service.RunnerService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RedisService(
    private val assetService: AssetService,
    private val runnerService: RunnerService,
    private val snippetClient: SnippetClient,
) : StreamService {
    override fun formatAndSaveSnippet(snippetId: UUID) {
        val snippetInfo = getSnippetInfo(snippetId)
        val content = assetService.get("snippets", snippetInfo.assetKey)
        runnerService.formatSnippet(
            FormatReqDTO(
                snippetId,
                snippetInfo.assetKey,
                content,
                snippetInfo.version,
                snippetInfo.config,
            ),
        )
    }

    override fun lintAndSaveSnippet(snippetId: UUID) {
        val snippetInfo = getSnippetInfo(snippetId)
        val content = assetService.get("snippets", snippetInfo.assetKey)
        runnerService.lintSnippet(
            LintReqDTO(
                snippetId,
                snippetInfo.assetKey,
                content,
                snippetInfo.version,
                snippetInfo.config,
            ),
        )
    }

    private fun getSnippetInfo(snippetId: UUID): SnippetInfoDto {
        val snippetMetadata = snippetClient.getSnippetMetadata(snippetId)
        val assetKey = snippetClient.getAssetKey(snippetId)
        val version = snippetMetadata.version
        val config = snippetClient.getSnippetConfig(snippetId)
        return SnippetInfoDto(
            assetKey,
            version,
            config,
        )
    }
}
