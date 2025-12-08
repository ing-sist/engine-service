package ingsist.engine.redis.consumer

import ingsist.engine.runner.dto.StreamReqDto

interface ConsumerStreamService {
    fun formatAndSaveSnippet(snippet: StreamReqDto)

    fun lintAndSaveSnippet(snippet: StreamReqDto)
}
