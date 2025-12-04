package ingsist.engine.redis

import ingsist.engine.asset.AssetService
import ingsist.engine.config.SnippetClient
import ingsist.engine.runner.dto.FormatReqDTO
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
        val snippetMetadata = snippetClient.getSnippetMetadata(snippetId)
        val assetKey = snippetClient.getAssetKey(snippetId)
        val version = snippetMetadata.version
        val content = assetService.get("snippets", assetKey)
        val config = snippetClient.getSnippetConfig(snippetId)
        runnerService.formatSnippet(
            FormatReqDTO(
                snippetId,
                assetKey,
                content,
                version,
                config,
            ),
        )
    }
}
