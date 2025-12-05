import ingsist.engine.runner.dto.OwnerConfigDto

data class LintingStreamReqDto(
    val assetKey: String,
    val version: String,
    val config: OwnerConfigDto,
)
