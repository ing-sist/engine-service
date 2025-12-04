package ingsist.engine.config

import ingsist.engine.runner.dto.OwnerConfigDto
import ingsist.engine.runner.dto.SnippetMetadataResponseDto
import ingsist.engine.runner.utils.exception.ExternalServiceException
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.UUID
import kotlin.jvm.java

@Component
class SnippetClient(
    builder: RestClient.Builder,
    @Value("\${external.snippet.url}") private val snippetUrl: String,
) {
    private val restClient = builder.baseUrl(snippetUrl).build()

    @PostConstruct
    fun logBaseUrl() {
        println(">>> SnippetClient baseUrl = $snippetUrl")
    }

    fun getSnippetMetadata(id: UUID): SnippetMetadataResponseDto {
        return restClient.get()
            .uri("/snippets/{id}/metadata", id)
            // aca va el header auth
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching snippet metadata for id: $id.")
            }
            .body(SnippetMetadataResponseDto::class.java)
            ?: throw ExternalServiceException("Empty response when fetching snippet metadata for id: $id")
    }

    fun getAssetKey(snippetId: UUID): String {
        return restClient.get()
            .uri("/snippets/{id}/asset-key", snippetId)
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching asset key for snippet id: $snippetId")
            }
            .body(String::class.java)
            ?: throw
            ExternalServiceException("Empty response when fetching asset key for snippet id: $snippetId")
    }

    fun getSnippetConfig(snippetId: UUID): OwnerConfigDto {
        return restClient.get()
            .uri("/user/{snippetId}/getConfig", snippetId)
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching snippet config for id: $snippetId")
            }
            .body(OwnerConfigDto::class.java)
            ?: throw ExternalServiceException("Empty response when fetching snippet config for id: $snippetId")
    }
}
