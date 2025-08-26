package id.co.awan.walle.service.core

import jakarta.validation.Validator
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

abstract class ValidationCoreAbstract(
    private val validator: Validator
) {

    protected fun validate(any: Any) {

        val constraintViolations = validator.validate(any)

        if (constraintViolations.isNotEmpty()) {

            val errors = mutableListOf<String>()
            for (violation in constraintViolations) {
                errors.add(violation.message)
            }

            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, errors.joinToString(",")
            )
        }
    }
}