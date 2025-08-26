package id.co.awan.walle.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ErrorController {

    @ExceptionHandler(Throwable::class)
    fun handleException(ex: Exception): ResponseEntity<Any> {
        // Customize your error response here
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf(Pair("error", ex.message)))
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleRcException(ex: ResponseStatusException): ResponseEntity<Any> {
        // Customize your error response here
        return ResponseEntity
            .status(ex.statusCode)
            .body<Any>(
                mapOf(
                    Pair(
                        "error",
                        ex.reason ?: "General Error"
                    )
                )
            )
    }

}