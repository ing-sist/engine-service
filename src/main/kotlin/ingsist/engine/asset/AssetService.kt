package ingsist.engine.asset

import ingsist.engine.runner.utils.exception.ExternalServiceException
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class AssetService(private val assetRestClient: RestClient) : AssetServiceInterface {
    override fun upload(
        container: String,
        key: String,
        content: String,
    ): String {
        val response =
            assetRestClient.put()
                .uri("/v1/asset/{container}/{key}", container, key)
                .body(content)
                .retrieve()
                .toEntity(String::class.java)
        return when (response.statusCode.value()) {
            201 -> "Asset uploaded successfully in $container with key $key"
            200 -> "Asset updated successfully in $container with key $key"
            else -> throw ExternalServiceException("Asset upload failed with status code: ${response.statusCode}")
        }
    }

    override fun delete(
        container: String,
        key: String,
    ): String {
        val response =
            assetRestClient.delete()
                .uri("/v1/asset/{container}/{key}", container, key)
                .retrieve()
                .toEntity(String::class.java)
        return when (response.statusCode.value()) {
            201 -> "Asset deleted successfully in $container with key $key"
            else -> throw ExternalServiceException("Asset deleted failed with status code: ${response.statusCode}")
        }
    }

    override fun get(
        container: String,
        key: String,
    ): String {
        val response =
            assetRestClient.get()
                .uri("/v1/asset/{container}/{key}", container, key)
                .retrieve()
                .onStatus({ status -> status.isError }) { _, _ ->
                    throw ExternalServiceException("Asset not found in $container with key $key")
                }
                .toEntity(String::class.java)

        return response.body ?: throw ExternalServiceException("Asset content is empty")
    }
}
