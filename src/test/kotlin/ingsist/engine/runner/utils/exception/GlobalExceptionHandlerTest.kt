package ingsist.engine.runner.utils.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleValidationException should return BAD_REQUEST`() {
        val ex = ValidationException("Validation error")
        val response = handler.handleValidationException(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.body?.status)
        assertEquals("Validation error", response.body?.message)
    }

    @Test
    fun `handleExecutionException should return UNPROCESSABLE_ENTITY`() {
        val ex = ExecutionException("Execution error")
        val response = handler.handleExecutionException(ex)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.body?.status)
        assertEquals("Execution error", response.body?.message)
    }

    @Test
    fun `handleProcessException should return UNPROCESSABLE_ENTITY`() {
        val ex = ProcessException("Process error")
        val response = handler.handleProcessException(ex)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.body?.status)
        assertEquals("Process error", response.body?.message)
    }

    @Test
    fun `handleGenericException should return INTERNAL_SERVER_ERROR`() {
        val ex = RuntimeException("Generic error")
        val response = handler.handleGenericException(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.body?.status)
        assertEquals("Error interno del servidor: RuntimeException", response.body?.message)
    }
}
