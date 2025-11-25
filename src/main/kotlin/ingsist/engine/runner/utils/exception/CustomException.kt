package ingsist.engine.runner.utils.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Excepción para errores de validación de sintaxis (Parser).
 * Se convierte en un HTTP 400 (Bad Request).
 *
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class ValidationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Excepción para errores de ejecución (Interpreter).
 * Se convierte en un HTTP 422 (Unprocessable Entity).
 *
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class ExecutionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Excepción para errores de proceso (Linting, Formatting) o I/O.
 * Se convierte en un HTTP 422 (Unprocessable Entity).
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class ProcessException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
