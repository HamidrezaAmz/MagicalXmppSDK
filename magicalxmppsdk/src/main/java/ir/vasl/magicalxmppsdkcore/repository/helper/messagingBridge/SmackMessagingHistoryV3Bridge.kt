package ir.vasl.magicalxmppsdkcore.repository.helper.messagingBridge

import android.util.Log
import ir.vasl.magicalxmppsdkcore.repository.PublicValue
import ir.vasl.magicalxmppsdkcore.repository.PublicValue.Companion.TAG
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MessagingHistoryInterface
import ir.vasl.magicalxmppsdkcore.repository.helper.IdGeneratorHelper
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smackx.mam.MamManager
import org.jxmpp.jid.Jid
import org.jxmpp.jid.impl.JidCreate

class SmackMessagingHistoryV3Bridge private constructor(
    private var connection: AbstractXMPPConnection,
    builder: Builder
) {

    private var domain = builder.domain
    private var messageCount = builder.messageCount
    private var messagingHistoryInterface: MessagingHistoryInterface? = builder.messagingHistoryInterface
    private var mamManager: MamManager = MamManager.getInstanceFor(connection)
    private var currTarget: String = ""

    private lateinit var mamQueryArgs: MamManager.MamQueryArgs
    private lateinit var mamQuery: MamManager.MamQuery

    data class Builder(val connection: AbstractXMPPConnection) {
        var domain: String? = PublicValue.DEFAULT_DOMAIN
        var messageCount: Int = PublicValue.DEFAULT_MESSAGE_COUNT
        var messagingHistoryInterface: MessagingHistoryInterface? = null

        fun setDomain(domain: String) = apply {
            this.domain = domain
        }

        fun setMessageCount(messageCount: Int) = apply {
            this.messageCount = messageCount
        }

        fun setCallback(messagingHistoryInterface: MessagingHistoryInterface?) = apply {
            this.messagingHistoryInterface = messagingHistoryInterface
        }

        fun build() = SmackMessagingHistoryV3Bridge(connection, this)
    }

    private fun getJid(target: String): Jid? {
        return JidCreate.from("$target@$domain")
    }

    private fun getChatHistoryNextPage() {
        Log.i(TAG, "getChatHistoryNextPage: currTarget: $currTarget")
        try {
            mamQuery.pageNext(messageCount)
            val messageHistoryList: List<MagicalIncomingMessage> = mamQuery.messages.map {
                MagicalIncomingMessage(
                    id = IdGeneratorHelper.getRandomId(),
                    message = it?.body ?: "No Message Found!",
                    from = it.from.toString()
                )
            }
            messagingHistoryInterface?.newIncomingMessageHistory(messageHistoryList as MutableList<MagicalIncomingMessage>)
        } catch (e: Exception) {
            messagingHistoryInterface?.newIncomingMessageHistoryError(e.message.toString())
        }
    }

    private fun getChatHistoryPreviousPage() {
        Log.i(TAG, "getChatHistoryPreviousPage: currTarget: $currTarget")
        try {
            if (mamQuery.isComplete.not()) {

                mamQuery.pagePrevious(messageCount)
                val messageHistoryList: List<MagicalIncomingMessage> = mamQuery.messages.map {
                    MagicalIncomingMessage(
                        id = IdGeneratorHelper.getRandomId(),
                        message = it?.body ?: "No Message Found!",
                        from = it.from.toString()
                    )
                }
                messagingHistoryInterface?.newIncomingMessageHistory(messageHistoryList as MutableList<MagicalIncomingMessage>)
            } else {
                Log.i(TAG, "getChatHistoryNextPage: All messages has been returned!")
            }

        } catch (e: Exception) {
            messagingHistoryInterface?.newIncomingMessageHistoryError(e.message.toString())
        }
    }

    fun getMessageHistory(target: String) {
        if (::mamQueryArgs.isInitialized.not())
            getMessageHistoryLastPage(target)
        else if (currTarget != target)
            getMessageHistoryLastPage(target) // new chat!
        else
            getChatHistoryPreviousPage()
    }

    fun getMessageFuture(target: String) {
        if (::mamQueryArgs.isInitialized.not())
            getMessageHistoryLastPage(target)
        else if (currTarget != target)
            getMessageHistoryLastPage(target) // new chat!
        else
            getChatHistoryNextPage()
    }

    fun getMessageHistoryLastPage(target: String) {
        try {
            currTarget = target
            Log.i(TAG, "getMessageHistoryLastPage: currTarget: $currTarget")

            mamQueryArgs = MamManager.MamQueryArgs.builder()
                .limitResultsToJid(getJid(target))
                .queryLastPage()
                .setResultPageSize(messageCount)
                .build()
            mamQuery = mamManager.queryArchive(mamQueryArgs)

            val messages = mamQuery.messages
            val extensions = mamQuery.mamResultExtensions

            val zippedList = messages.zip(extensions)
            val messageHistoryList: List<MagicalIncomingMessage> = zippedList.map { pair ->
                MagicalIncomingMessage(
                    id = pair.second.id ?: IdGeneratorHelper.getRandomId(),
                    message = pair.first?.body ?: "No Message Found!",
                    from = pair.first.from.toString()
                )
            }

            messagingHistoryInterface?.newIncomingMessageHistory(messageHistoryList as MutableList<MagicalIncomingMessage>)
        } catch (e: Exception) {
            messagingHistoryInterface?.newIncomingMessageHistoryError(e.message.toString())
        }
    }

    fun getMessageAfterId(target: String, Uid: String) {
        try {
            currTarget = target
            Log.i(TAG, "getMessageAfterId: currTarget: $currTarget")

            mamQueryArgs = MamManager.MamQueryArgs.builder()
                .limitResultsToJid(getJid(target))
                .afterUid(Uid)
                .setResultPageSize(messageCount)
                .build()
            mamQuery = mamManager.queryArchive(mamQueryArgs)

            val messages = mamQuery.messages
            val extensions = mamQuery.mamResultExtensions

            val zippedList = messages.zip(extensions)
            val messageHistoryList: List<MagicalIncomingMessage> = zippedList.map { pair ->
                MagicalIncomingMessage(
                    id = pair.second.id ?: IdGeneratorHelper.getRandomId(),
                    message = pair.first?.body ?: "No Message Found!",
                    from = pair.first.from.toString()
                )
            }
            messagingHistoryInterface?.newIncomingMessageHistory(messageHistoryList as MutableList<MagicalIncomingMessage>)
        } catch (e: Exception) {
            messagingHistoryInterface?.newIncomingMessageHistoryError(e.message.toString())
        }
    }

    fun getMessageBeforeId(target: String, Uid: String) {
        try {
            currTarget = target
            Log.i(TAG, "getMessageAfterId: currTarget: $currTarget")

            mamQueryArgs = MamManager.MamQueryArgs.builder()
                .limitResultsToJid(getJid(target))
                .beforeUid(Uid)
                .setResultPageSize(messageCount)
                .build()
            mamQuery = mamManager.queryArchive(mamQueryArgs)

            val messages = mamQuery.messages
            val extensions = mamQuery.mamResultExtensions

            val zippedList = messages.zip(extensions)
            val messageHistoryList: List<MagicalIncomingMessage> = zippedList.map { pair ->
                MagicalIncomingMessage(
                    id = pair.second.id ?: IdGeneratorHelper.getRandomId(),
                    message = pair.first?.body ?: "No Message Found!",
                    from = pair.first.from.toString()
                )
            }
            messagingHistoryInterface?.newIncomingMessageHistory(messageHistoryList as MutableList<MagicalIncomingMessage>)
        } catch (e: Exception) {
            messagingHistoryInterface?.newIncomingMessageHistoryError(e.message.toString())
        }
    }

    fun disconnect() {
        currTarget = ""
        messagingHistoryInterface = null
    }

}