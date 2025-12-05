package ingsist.engine.redis

import java.util.UUID

interface StreamService {
    fun formatAndSaveSnippet(snippetId: UUID)

    fun lintAndSaveSnippet(snippetId: UUID)
}
