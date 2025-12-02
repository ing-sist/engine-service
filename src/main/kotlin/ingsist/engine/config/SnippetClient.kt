package ingsist.engine.config

import ingsist.engine.runner.dto.OwnerConfigDTO
import ingsist.engine.runner.utils.exception.ExternalServiceException
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

    fun getSnippetVersion(snippetId: UUID): String {
        return restClient.get()
            .uri("/{id}/latest", snippetId)
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching snippet version for id: $snippetId")
            }
            .body(String::class.java)
            ?: throw ExternalServiceException("Empty response when fetching snippet version for id: $snippetId")
    }

    fun getAssetKey(snippetId: UUID): String {
        return restClient.get()
            .uri("/{id}/asset-key", snippetId)
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching asset key for snippet id: $snippetId")
            }
            .body(String::class.java)
            ?: throw ExternalServiceException("Empty response when fetching asset key for snippet id: $snippetId")
    }

    fun getSnippetConfig(snippetId: UUID): OwnerConfigDTO {
        return restClient.get()
            .uri("/userConfig", snippetId)
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExternalServiceException("Error when fetching snippet config for id: $snippetId")
            }
            .body(OwnerConfigDTO::class.java)
            ?: throw ExternalServiceException("Empty response when fetching snippet config for id: $snippetId")
    }
}
