package ir.vasl.magicalxmppsdkcore.repository.helper.messagingBridge

import android.util.Log
import ir.vasl.magicalxmppsdkcore.repository.PublicValue
import ir.vasl.magicalxmppsdkcore.repository.PublicValue.Companion.DEFAULT_MESSAGE_COUNT
import ir.vasl.magicalxmppsdkcore.repository.PublicValue.Companion.TAG
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MessagingHistoryInterface
import ir.vasl.magicalxmppsdkcore.repository.helper.IdGeneratorHelper
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smackx.mam.MamManager
import org.jivesoftware.smackx.mam.MamManager.MamQueryArgs
import org.jxmpp.jid.impl.JidCreate

class SmackMessagingHistoryBridge private constructor(
    private var connection: AbstractXMPPConnection,
    builder: Builder
) {

    private var target = builder.target
    private var messageCount = builder.messageCount
    private var messagingHistoryInterface: MessagingHistoryInterface? = builder.messagingHistoryInterface

    data class Builder(val connection: AbstractXMPPConnection) {
        var target: String? = null
        var messageCount: Int = DEFAULT_MESSAGE_COUNT
        var messagingHistoryInterface: MessagingHistoryInterface? = null

        fun setTarget(target: String) = apply {
            this.target = target
        }

        fun setMessageCount(messageCount: Int) = apply {
            this.messageCount = messageCount
        }

        fun setCallback(messagingHistoryInterface: MessagingHistoryInterface?) = apply {
            this.messagingHistoryInterface = messagingHistoryInterface
        }

        fun build() = SmackMessagingHistoryBridge(connection, this)
    }

    init {
        getChatHistory()
    }

    private fun getChatHistory() {

        Log.i(TAG, "getChatHistory: start getting history")

        val jid = JidCreate.from(target + "@" + PublicValue.DEFAULT_DOMAIN)

        CoroutineScope(Dispatchers.IO).launch {

            val mamManager = MamManager.getInstanceFor(connection)

            mamManager.enableMamForAllMessages()

            Log.i(TAG, "getChatHistory: mamManager support -> : ${mamManager.isSupported}")

            try {

                val mamQueryArgs = MamQueryArgs.builder()
                    .limitResultsToJid(jid)
                    .setResultPageSize(messageCount)
                    .build()

                val mamQuery = mamManager.queryArchive(mamQueryArgs)
                Log.i(TAG, "getChatHistory: mamQuery -> ${mamQuery.messageCount}")
                val messageHistoryList: List<MagicalIncomingMessage> = mamQuery.messages.map {
                    MagicalIncomingMessage(
                        id = IdGeneratorHelper.getRandomId(),
                        message = it?.body ?: "No Message Found!",
                        from = it.from.toString()
                    )
                }

                // val archivePref = mamManager.retrieveArchivingPreferences()
                // Log.i(TAG, "getChatHistory: archivePref -> ${archivePref.toString()}")

                messagingHistoryInterface?.newIncomingMessageHistory(messageHistoryList)

            } catch (e: Exception) {
                Log.e(TAG, "getChatHistory: $e")
            }
        }
    }

}