package id.co.awan.walle.utils

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import java.security.SecureRandom


class LogUtils {
    companion object {

        private val log = LoggerFactory.getLogger(this::class.java)
        private val secureRandom = SecureRandom()

        val generateLogToken = {
            "REQ_${System.currentTimeMillis()}_${secureRandom.nextInt(999999)}"
        }

        fun logHttpRequest(
            clazz: Class<*>,
            methodName: String,
            request: JsonNode
        ): String {

            val reqToken = generateLogToken()
            log.info(
                "[HTTP-REQUEST {} - {}:{}] : {}",
                reqToken,
                clazz.getSimpleName(),
                "$methodName()",
                request.toPrettyString()
            )

            return "$reqToken|$methodName"
        }

        fun logHttpResponse(
            reqTokenJoin: String,
            clazz: Class<*>,
            response: JsonNode
        ) {
            val reqTokenTupple = reqTokenJoin.split("|");
            log.info(
                "[HTTP-RESPONSE {} - {}:{}] : {}",
                reqTokenTupple[0],
                clazz.getSimpleName(),
                "${reqTokenTupple[1]}()",
                response.toPrettyString()
            )
        }

        fun logHttpResponseWithoutToken(
            methodName: String,
            clazz: Class<*>,
            response: JsonNode
        ) {
            log.info(
                "[HTTP-RESPONSE - {}:{}] : {}",
                clazz.getSimpleName(),
                "$methodName()",
                response.toPrettyString()
            )
        }

    }
}