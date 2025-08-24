package id.co.awan.walle.utils

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

object JsonNodeUtils {

    fun <T : Any> validateField(
        jsonNode: JsonNode,
        jsonPath: String,
        clazz: KClass<T>,
        nullable: Boolean = false
    ): T? {

        val node = jsonNode.at(jsonPath)

        // Node is null
        if (node.isNull) {
            if (nullable) { // Skip kalo boleh null
                return null
            } else { // Exception kalo gaboleh
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "$jsonPath | Cannot be null or empty"
                )
            }
        }

        when (clazz) {
            String::class -> node.isTextual
            Int::class -> node.isInt
            Double::class -> node.isDouble
            Float::class -> node.isFloat
            Boolean::class -> node.isBoolean
            BigInteger::class -> node.isBigInteger
            BigDecimal::class -> node.isBigDecimal
            else -> false
        }.also {
            if (!it) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "$jsonPath | Type data must be ${clazz.simpleName}"
                )

            }
        }

        @Suppress("UNCHECKED_CAST")
        return when (clazz) {
            String::class -> node.asText()
                // String Null Check
                .apply {
                    if (this.isEmpty() || this.isBlank()) {
                        if (!nullable) { // Skip kalo boleh null
                            throw ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "$jsonPath | Cannot be null or empty"
                            )
                        }
                    }
                } as T

            Int::class -> node.asInt() as T
            Double::class -> node.asDouble() as T
            Float::class -> node.floatValue() as T
            Boolean::class -> node.asBoolean() as T
            BigInteger::class -> node.bigIntegerValue() as T
            BigDecimal::class -> node.decimalValue() as T
            else -> null
        }
    }


    data class JsonValidationPredicate(
        val jsonPath: String,
        val typeData: KClass<*>,
        val nullable: Boolean = false
    )

    fun validateFieldDeprecated(jsonNode: JsonNode, predicate: List<JsonValidationPredicate>) {

        val errorListMessage = mutableListOf<String>()

        loopPredicate@ for ((jsonPath, typeData, nullable) in predicate) {
            val node = jsonNode.at(jsonPath)
            if (nullable) { // Jika boleh null
                if (node.isNull) {
                    continue@loopPredicate
                }
            } else { // Jika tidak boleh null
                if (node.isNull) {
                    errorListMessage.add("$jsonPath | Cannot be null or empty")
                    continue@loopPredicate
                }
            }

            when (typeData) {
                String::class -> node.isTextual
                Int::class -> node.isInt
                Double::class -> node.isDouble
                Float::class -> node.isFloat
                Boolean::class -> node.isBoolean
                BigInteger::class -> node.isBigInteger
                BigDecimal::class -> node.isBigDecimal
                else -> false
            }.also {
                if (!it) {
                    errorListMessage.add("$jsonPath | Type data must be ${typeData.simpleName}")
                }
            }

        }

        if (errorListMessage.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, errorListMessage.joinToString(",")
            )
        }

    }


}