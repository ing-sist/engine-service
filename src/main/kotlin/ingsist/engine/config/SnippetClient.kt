package ingsist.engine.config

import ingsist.engine.auth0.client.Auth0M2MClient
import ingsist.engine.runner.dto.OwnerConfigDto
import ingsist.engine.runner.dto.SnippetMetadataResponseDto
import ingsist.engine.runner.utils.exception.ExternalServiceException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.UUID
import kotlin.jvm.java

@Component
class SnippetClient(
    builder: RestClient.Builder,
    private val auth0M2MClient: Auth0M2MClient,
    @Value("\${external.snippet.url}") private val snippetUrl: String,
) {
    private val restClient = builder.baseUrl(snippetUrl).build()

    fun getSnippetMetadata(id: UUID): SnippetMetadataResponseDto {
        val token = auth0M2MClient.getAccessToken()
        return restClient.get()
            .uri("/snippets/{id}/metadata", id)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching snippet metadata for id: $id.")
            }
            .body(SnippetMetadataResponseDto::class.java)
            ?: throw ExternalServiceException("Empty response when fetching snippet metadata for id: $id")
    }

    fun getAssetKey(snippetId: UUID): String {
        val token = auth0M2MClient.getAccessToken()
        return restClient.get()
            .uri("/snippets/{id}/asset-key", snippetId)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching asset key for snippet id: $snippetId")
            }
            .body(String::class.java)
            ?: throw
            ExternalServiceException("Empty response when fetching asset key for snippet id: $snippetId")
    }

    fun getSnippetConfig(snippetId: UUID): OwnerConfigDto {
        val token = auth0M2MClient.getAccessToken()
        return restClient.get()
            .uri("/users/{snippetId}/getConfig", snippetId)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching snippet config for id: $snippetId")
            }
            .body(OwnerConfigDto::class.java)
            ?: throw ExternalServiceException("Empty response when fetching snippet config for id: $snippetId")
    }
}
