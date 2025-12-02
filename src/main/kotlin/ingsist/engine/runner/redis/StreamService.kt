package ingsist.engine.runner.redis

import java.util.UUID

interface StreamService {
    fun formatAndSaveSnippet(snippetId: UUID)
}
