package ingsist.engine.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.MDC

@ExtendWith(MockitoExtension::class)
class CorrelationIdFilterTest {
    @Mock
    lateinit var request: HttpServletRequest

    @Mock
    lateinit var response: HttpServletResponse

    @Mock
    lateinit var filterChain: FilterChain

    @InjectMocks
    lateinit var correlationIdFilter: CorrelationIdFilter

    @Test
    fun `should use existing correlation id from header`() {
        val existingId = "existing-id"
        `when`(request.getHeader("X-Correlation-Id")).thenReturn(existingId)

        var capturedMdc: String? = null
        `when`(filterChain.doFilter(request, response)).thenAnswer {
            capturedMdc = MDC.get("correlation-id")
            null
        }

        correlationIdFilter.doFilter(request, response, filterChain)

        verify(response).setHeader("X-Correlation-Id", existingId)
        verify(filterChain).doFilter(request, response)
        assertEquals(existingId, capturedMdc)
    }

    @Test
    fun `should generate new correlation id when header is missing`() {
        `when`(request.getHeader("X-Correlation-Id")).thenReturn(null)

        var capturedMdc: String? = null
        `when`(filterChain.doFilter(request, response)).thenAnswer {
            capturedMdc = MDC.get("correlation-id")
            null
        }

        correlationIdFilter.doFilter(request, response, filterChain)

        verify(response).setHeader(
            org.mockito.ArgumentMatchers
                .eq("X-Correlation-Id"),
            org.mockito.ArgumentMatchers.anyString(),
        )
        verify(filterChain).doFilter(request, response)
        assertNotNull(capturedMdc)
    }

    @Test
    fun `should generate new correlation id when header is blank`() {
        `when`(request.getHeader("X-Correlation-Id")).thenReturn("   ")

        var capturedMdc: String? = null
        `when`(filterChain.doFilter(request, response)).thenAnswer {
            capturedMdc = MDC.get("correlation-id")
            null
        }

        correlationIdFilter.doFilter(request, response, filterChain)

        verify(response).setHeader(
            org.mockito.ArgumentMatchers
                .eq("X-Correlation-Id"),
            org.mockito.ArgumentMatchers.anyString(),
        )
        verify(filterChain).doFilter(request, response)
        assertNotNull(capturedMdc)
    }
}
