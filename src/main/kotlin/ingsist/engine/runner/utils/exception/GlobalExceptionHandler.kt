package ingsist.engine.runner.utils.exception

import ingsist.engine.runner.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

class ExternalServiceException(message: String) : RuntimeException(message)

/**
 * Manejador de excepciones global.
 * Atrapa excepciones y las convierte en respuestas HTTP JSON.
 *
 */
@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message)
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ExecutionException::class)
    fun handleExecutionException(ex: ExecutionException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.message)
        return ResponseEntity(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY)
    }

    @ExceptionHandler(ProcessException::class)
    fun handleProcessException(ex: ProcessException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.message)
        return ResponseEntity(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        // Logueamos el error real para debugging
        logger.error("Unhandled internal server error", ex)

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "Error interno del servidor: ${ex.javaClass.simpleName}",
            )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
