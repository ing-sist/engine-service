import ingsist.engine.runner.dto.OwnerConfigDto

data class SnippetInfoDto(
    val assetKey: String,
    val version: String,
    val config: OwnerConfigDto,
)
