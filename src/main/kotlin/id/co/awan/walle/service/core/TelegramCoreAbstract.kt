package id.co.awan.walle.service.core

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

abstract class TelegramCoreAbstract(
    private val restTemplate: RestTemplate
) {

    @Value("\${telegram.host}")
    private lateinit var telegramHost: String

    @Value("\${telegram.api-key}")
    private lateinit var apiKey: String

    protected fun get(
        telegramMethodName: String,
        queryParams: LinkedMultiValueMap<String, String>? = null
    ): ResponseEntity<JsonNode?> {
        return executeRest(
            HttpMethod.GET,
            telegramMethodName,
            queryParams
        )
    }

    protected fun post(
        telegramMethodName: String,
        queryParams: LinkedMultiValueMap<String, String>? = null,
        body: JsonNode? = null,
    ): ResponseEntity<JsonNode?> {
        return executeRest(
            HttpMethod.POST,
            telegramMethodName,
            queryParams,
            body
        )
    }

    protected fun executeRest(
        method: HttpMethod,
        telegramMethodName: String,
        queryParams: LinkedMultiValueMap<String, String>? = null,
        body: JsonNode? = null,
    ): ResponseEntity<JsonNode?> {

        val url = UriComponentsBuilder.fromUriString("$telegramHost/bot$apiKey/$telegramMethodName")
            .queryParams(queryParams)
            .build()
            .toUri()

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        return restTemplate.exchange(
            url,
            method,
            HttpEntity<JsonNode?>(body, headers),
            JsonNode::class.java
        )
    }


    protected fun parseResponseJsonNode(
        responseEntity: ResponseEntity<JsonNode?>
    ): JsonNode {

        val responseJson: JsonNode = responseEntity.getBody()
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|ResponseJson should not be null"
            )

        if (responseEntity.statusCode != HttpStatus.OK) {

            val errorMessage1 = responseJson.at("/error").asText("General Error")
            if (errorMessage1 != null) {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "01|$errorMessage1")
            }

            val errorCode = responseJson.at("/status/error_code").asInt()
            val errorMessage2 = responseJson.at("/status/error_message").asText("General Error")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "$errorCode|$errorMessage2")
        }

        return responseJson
    }

    // DTO

    data class User(
        val id: Integer,

        @field:JsonProperty("is_bot")
        val isBot: Boolean,

        @field:JsonProperty("first_name")
        val firstName: String,

        @field:JsonProperty("last_name")
        val lastName: String?,

        val username: String?,

        @field:JsonProperty("language_code")
        val languageCode: String?,

        @field:JsonProperty("is_premium")
        val isPremium: Boolean?,

        @field:JsonProperty("added_to_attachment_menu")
        val addedToAttachmentMenu: Boolean?,

        @field:JsonProperty("can_join_groups")
        val canJoinGroups: Boolean?,

        @field:JsonProperty("can_read_all_group_messages")
        val canReadAllGroupMessages: Boolean?,

        @field:JsonProperty("supports_inline_queries")
        val supportsInlineQueries: Boolean?,

        @field:JsonProperty("can_connect_to_business")
        val canConnectToBusiness: Boolean?,

        @field:JsonProperty("has_main_web_app")
        val hasMainWebApp: Boolean?,
    )

    data class MessageEntity(
        val type: String,
        val offset: Int,
        val length: Int,
        val url: String,
        val user: User,
        val language: String,

        @field:JsonProperty("custom_emoji_id")
        val customEmojiId: String,
    )

    data class LinkPreviewOptions(
        @field:JsonProperty("is_disabled")
        val isDisabled: Boolean?,

        val url: String?,

        @field:JsonProperty("prefer_small_media")
        val preferSmallMedia: Boolean?,

        @field:JsonProperty("prefer_large_media")
        val preferLargeMedia: Boolean?,

        @field:JsonProperty("show_above_text")
        val showAboveText: Boolean?,
    )

    data class SuggestedPostPrice(
        val currency: String,
        val amount: Integer
    )

    data class SuggestedPostParameters(
        val price: SuggestedPostPrice?,
        val send_date: Integer?,
    )

    data class ReplyParameters(
        @field:JsonProperty("message_id")
        val messageId: Integer,

        @field:JsonProperty("chat_id")
        val chatId: Integer,

        @field:JsonProperty("allow_sending_without_reply")
        val allowSendingWithoutReply: Boolean,

        val quote: String,

        @field:JsonProperty("quote_parse_mode")
        val quoteParseMode: String,

        @field:JsonProperty("quote_entities")
        val quoteEntities: List<MessageEntity>,

        @field:JsonProperty("quote_position")
        val quotePosition: Integer,

        @field:JsonProperty("checklist_task_id")
        val checklistTaskId: Integer,

        )

    data class SendMessage(
        @field:JsonProperty("business_connection_id")
        val businessConnectionId: String?,

        @field:JsonProperty("chat_id")
        val chatId: String,

        @field:JsonProperty("message_thread_id")
        val messageThreadId: Int?,

        @field:JsonProperty("direct_messages_topic_id")
        val directMessagesTopicId: Int?,

        val text: String,

        @field:JsonProperty("parse_mode")
        val parseMode: String?,

        val entities: MutableList<MessageEntity>,

        @field:JsonProperty("link_preview_options")
        val linkPreviewOptions: LinkPreviewOptions?,

        @field:JsonProperty("disable_notification")
        val disableNotification: Boolean?,

        @field:JsonProperty("protect_content")
        val protectContent: Boolean?,

        @field:JsonProperty("allow_paid_broadcast")
        val allowPaidBroadcast: Boolean?,

        @field:JsonProperty("message_effect_id")
        val messageEffectId: String?,

        @field:JsonProperty("suggested_post_parameters")
        val suggestedPostParameters: SuggestedPostParameters?,

        @field:JsonProperty("reply_parameters")
        val replyParameters: ReplyParameters?,

        @field:JsonProperty("reply_markup")
        val replyMarkup: Any?,

        )


}