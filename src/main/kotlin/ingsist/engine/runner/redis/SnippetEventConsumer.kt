package ingsist.engine.runner.redis

import java.util.UUID

interface SnippetEventConsumer {
    fun consumeSnippet(snippetId: UUID)
}
