package ingsist.engine.redis

import ingsist.engine.runner.dto.StreamReqDto

interface StreamService {
    fun formatAndSaveSnippet(snippet: StreamReqDto)

    fun lintAndSaveSnippet(snippet: StreamReqDto)
}
