package com.github.kotlintelegrambot.dispatcher

import com.github.kotlintelegrambot.HandleUpdate
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandler
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardButton
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.TelegramFile.ByUrl
import com.github.kotlintelegrambot.entities.inlinequeryresults.InlineQueryResult
import com.github.kotlintelegrambot.entities.inlinequeryresults.InputMessageContent
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.network.fold
import okhttp3.logging.HttpLoggingInterceptor

fun main(args: Array<String>) {

    val bot = bot {

        token = "YOUR_API_KEY"
        timeout = 30
        logLevel = HttpLoggingInterceptor.Level.BODY

        dispatch {
            message(Filter.Sticker) { bot, update ->
                bot.sendMessage(update.message!!.chat.id, text = "You have received an awesome sticker \\o/")
            }

            message(Filter.Reply or Filter.Forward) { bot, update ->
                bot.sendMessage(update.message!!.chat.id, text = "someone is replying or forwarding messages ...")
            }

            command("start") { bot, update ->

                val result = bot.sendMessage(chatId = update.message!!.chat.id, text = "Bot started")

                result.fold({
                    // do something here with the response
                }, {
                    // do something with the error
                })
            }

            command("hello") { bot, update ->

                val result = bot.sendMessage(chatId = update.message!!.chat.id, text = "Hello, world!")

                result.fold({
                    // do something here with the response
                }, {
                    // do something with the error
                })
            }

            command("commandWithArgs") { bot, update, args ->
                val joinedArgs = args.joinToString()
                val response = if (!joinedArgs.isNullOrBlank()) joinedArgs else "There is no text apart from command!"
                bot.sendMessage(chatId = update.message!!.chat.id, text = response)
            }

            command("markdown") { bot, update ->
                val markdownText = "_Cool message_: *Markdown* is `beatiful` :P"
                bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    text = markdownText,
                    parseMode = MARKDOWN
                )
            }

            command("inlineButtons") { bot, update ->
                val chatId = update.message?.chat?.id ?: return@command

                val inlineKeyboardMarkup = InlineKeyboardMarkup(generateButtons())
                bot.sendMessage(chatId = chatId, text = "Hello, inline buttons!", replyMarkup = inlineKeyboardMarkup)
            }

            command("userButtons") { bot, update ->
                val chatId = update.message?.chat?.id ?: return@command

                val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true)
                bot.sendMessage(chatId = chatId, text = "Hello, users buttons!", replyMarkup = keyboardMarkup)
            }

            command("mediaGroup") { bot, update ->
                val chatId = update.message?.chat?.id ?: return@command
                val messageId = update.message?.messageId ?: return@command
                bot.sendMediaGroup(
                    chatId = chatId,
                    mediaGroup = MediaGroup.from(
                        InputMediaPhoto(
                            media = ByUrl("https://www.sngular.com/wp-content/uploads/2019/11/Kotlin-Blog-1400x411.png"),
                            caption = "I come from an url :P"
                        )
                    ),
                    replyToMessageId = messageId
                )
            }

            callbackQuery("testButton") { bot, update ->
                update.callbackQuery?.let {
                    val chatId = it.message?.chat?.id ?: return@callbackQuery
                    bot.sendMessage(chatId = chatId, text = it.data)
                }
            }

            callbackQuery(createAlertCallbackQueryHandler { bot, update ->
                update.callbackQuery?.let {
                    val chatId = it.message?.chat?.id ?: return@createAlertCallbackQueryHandler
                    bot.sendMessage(chatId = chatId, text = it.data)
                }
            })

            text("ping") { bot, update ->
                bot.sendMessage(chatId = update.message!!.chat.id, text = "Pong")
            }

            location { bot, update, location ->
                val chatId = update.message?.chat?.id ?: return@location
                bot.sendMessage(
                    chatId = chatId,
                    text = "Your location is (${location.latitude}, ${location.longitude})",
                    replyMarkup = ReplyKeyboardRemove()
                )
            }

            contact { bot, update, contact ->
                val chatId = update.message?.chat?.id ?: return@contact
                bot.sendMessage(
                    chatId = chatId,
                    text = "Hello, ${contact.firstName} ${contact.lastName}",
                    replyMarkup = ReplyKeyboardRemove()
                )
            }

            channel { bot, update ->
                // Handle channel update
            }

            inlineQuery { bot, inlineQuery ->
                val queryText = inlineQuery.query

                if (queryText.isBlank() or queryText.isEmpty()) return@inlineQuery

                val inlineResults = (0 until 5).map {
                    InlineQueryResult.Article(
                        id = it.toString(),
                        title = "$it. $queryText",
                        inputMessageContent = InputMessageContent.Text("$it. $queryText"),
                        description = "Add $it. before you word"
                    )
                }
                bot.answerInlineQuery(inlineQuery.id, inlineResults)
            }

            photos { bot, update, _ ->
                val chatId = update.message?.chat?.id ?: return@photos
                bot.sendMessage(
                    chatId = chatId,
                    text = "Wowww, awesome photos!!! :P"
                )
            }

            telegramError { _, telegramError ->
                println(telegramError.getErrorMessage())
            }
        }
    }

    bot.startPolling()
}

fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("Request location (not supported on desktop)", requestLocation = true)),
        listOf(KeyboardButton("Request contact", requestContact = true))
    )
}

fun createAlertCallbackQueryHandler(handler: HandleUpdate): CallbackQueryHandler {
    return CallbackQueryHandler(
        callbackData = "showAlert",
        callbackAnswerText = "HelloText",
        callbackAnswerShowAlert = true,
        handler = handler
    )
}

fun generateButtons(): List<List<InlineKeyboardButton>> {
    return listOf(
        listOf(InlineKeyboardButton(text = "Test Inline Button", callbackData = "testButton")),
        listOf(InlineKeyboardButton(text = "Show alert", callbackData = "showAlert"))
    )
}
