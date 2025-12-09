package ingsist.engine.asset

import ingsist.engine.runner.utils.exception.ExternalServiceException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.RequestBodyUriSpec
import org.springframework.web.client.RestClient.RequestHeadersUriSpec
import org.springframework.web.client.RestClient.ResponseSpec

@ExtendWith(MockitoExtension::class)
class AssetServiceTest {
    @Mock
    private lateinit var restClient: RestClient

    @Mock
    private lateinit var requestBodyUriSpec: RequestBodyUriSpec

    @Mock
    private lateinit var requestHeadersUriSpec: RequestHeadersUriSpec<*>

    @Mock
    private lateinit var responseSpec: ResponseSpec

    private lateinit var assetService: AssetService

    @BeforeEach
    fun setUp() {
        assetService = AssetService(restClient)
    }

    @Test
    fun `should upload asset successfully`() {
        AssetServiceDSL()
            .givenRestClientReturns(HttpStatus.CREATED)
            .whenUpload("container", "key", "content")
            .thenResultShouldBe("Asset uploaded successfully in container with key key")
    }

    @Test
    fun `should update asset successfully`() {
        AssetServiceDSL()
            .givenRestClientReturns(HttpStatus.OK)
            .whenUpload("container", "key", "content")
            .thenResultShouldBe("Asset updated successfully in container with key key")
    }

    @Test
    fun `should fail to upload asset`() {
        AssetServiceDSL()
            .givenRestClientReturns(HttpStatus.INTERNAL_SERVER_ERROR)
            .whenUploadAndExpectError("container", "key", "content")
    }

    @Test
    fun `should delete asset successfully`() {
        AssetServiceDSL()
            .givenRestClientDeleteReturns(HttpStatus.OK)
            .whenDelete("container", "key")
            .thenResultShouldBe("Asset deleted successfully in container with key key")
    }

    @Test
    fun `should get asset successfully`() {
        AssetServiceDSL()
            .givenRestClientGetReturns("content")
            .whenGet("container", "key")
            .thenResultShouldBe("content")
    }

    inner class AssetServiceDSL {
        private var result: String? = null

        fun givenRestClientReturns(status: HttpStatus): AssetServiceDSL {
            `when`(restClient.put()).thenReturn(requestBodyUriSpec)
            `when`(requestBodyUriSpec.uri(any<String>(), any<String>(), any<String>()))
                .thenReturn(requestBodyUriSpec)
            `when`(requestBodyUriSpec.body(any<String>())).thenReturn(requestBodyUriSpec)
            `when`(requestBodyUriSpec.retrieve()).thenReturn(responseSpec)
            `when`(responseSpec.toEntity(String::class.java))
                .thenReturn(ResponseEntity.status(status).build())
            return this
        }

        fun givenRestClientDeleteReturns(status: HttpStatus): AssetServiceDSL {
            `when`(restClient.delete()).thenReturn(requestHeadersUriSpec)
            `when`(requestHeadersUriSpec.uri(any<String>(), any<String>(), any<String>()))
                .thenReturn(requestHeadersUriSpec)
            `when`(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec)
            `when`(responseSpec.toEntity(String::class.java))
                .thenReturn(ResponseEntity.status(status).build())
            return this
        }

        fun givenRestClientGetReturns(body: String): AssetServiceDSL {
            `when`(restClient.get()).thenReturn(requestHeadersUriSpec)
            `when`(requestHeadersUriSpec.uri(any<String>(), any<String>(), any<String>()))
                .thenReturn(requestHeadersUriSpec)
            `when`(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec)
            `when`(responseSpec.onStatus(any(), any())).thenReturn(responseSpec)
            `when`(responseSpec.toEntity(String::class.java))
                .thenReturn(ResponseEntity.ok(body))
            return this
        }

        fun whenUpload(
            container: String,
            key: String,
            content: String,
        ): AssetServiceDSL {
            result = assetService.upload(container, key, content)
            return this
        }

        fun whenUploadAndExpectError(
            container: String,
            key: String,
            content: String,
        ): AssetServiceDSL {
            assertThrows(ExternalServiceException::class.java) {
                assetService.upload(container, key, content)
            }
            return this
        }

        fun whenDelete(
            container: String,
            key: String,
        ): AssetServiceDSL {
            result = assetService.delete(container, key)
            return this
        }

        fun whenGet(
            container: String,
            key: String,
        ): AssetServiceDSL {
            result = assetService.get(container, key)
            return this
        }

        fun thenResultShouldBe(expected: String) {
            assertEquals(expected, result)
        }
    }
}
