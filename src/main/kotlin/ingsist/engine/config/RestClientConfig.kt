package ingsist.engine.config

import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    companion object {
        private const val CORRELATION_ID_KEY = "correlation-id"
        private const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    }

    @Bean
    fun assetRestClient(
        @Value("\${external.asset.url}") url: String,
    ): RestClient {
        return RestClient.builder()
            .requestInterceptor { request, body, execution ->
                MDC.get(CORRELATION_ID_KEY)?.let { corrId ->
                    request.headers.add(CORRELATION_ID_HEADER, corrId)
                }
                execution.execute(request, body)
            }
            .baseUrl(url)
            .build()
    }
}
