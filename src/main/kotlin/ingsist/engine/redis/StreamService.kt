package ingsist.engine.redis

import java.util.UUID

interface StreamService {
    fun formatAndSaveSnippet(snippetId: UUID)
}
