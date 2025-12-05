package ingsist.engine.auth0.client

import ingsist.engine.auth0.dto.TokenResponseDto
import ingsist.engine.runner.utils.exception.ExternalServiceException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Instant

@Component
class Auth0M2MClient(
    builder: RestClient.Builder,
    @Value("\${auth0.m2m.client-id}") private val clientId: String,
    @Value("\${auth0.m2m.client-secret}") private val clientSecret: String,
    @Value("\${auth0.m2m.audience}") private val audience: String,
    @Value("\${auth0.domain}") private val domain: String,
    @Value("\${auth0.m2m.token-url}") private val tokenUrl: String,
) {
    private val restClient =
        builder
            .baseUrl("https://$domain")
            .build()

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var expiresAt: Instant = Instant.EPOCH

    fun getAccessToken(): String {
        // para evitar pedirlo en cada request
        if (prevTokenIsValid() && cachedToken != null) {
            return cachedToken!!
        }

        val response =
            restClient.post()
                .uri(tokenUrl)
                .body(
                    mapOf(
                        "client_id" to clientId,
                        "client_secret" to clientSecret,
                        "audience" to audience,
                        "grant_type" to "client_credentials",
                    ),
                )
                .retrieve()
                .body(TokenResponseDto::class.java)
                ?: throw ExternalServiceException("Empty response when getting M2M token from Auth0")

        return response.accessToken
    }

    private fun prevTokenIsValid(): Boolean {
        val now = Instant.now().plusSeconds(30)
        val token = cachedToken
        return token != null && now.isBefore(expiresAt)
    }
}
