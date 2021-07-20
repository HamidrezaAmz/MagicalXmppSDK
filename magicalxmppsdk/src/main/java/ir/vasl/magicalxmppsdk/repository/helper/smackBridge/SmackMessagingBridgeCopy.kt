package ir.vasl.magicalxmppsdk.repository.helper.smackBridge

import android.util.Log
import ir.vasl.magicalxmppsdk.repository.PublicValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.MessageBuilder
import org.jivesoftware.smackx.mam.MamManager
import org.jivesoftware.smackx.mam.MamManager.MamQueryArgs
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate

object SmackMessagingBridgeCopy : IncomingChatMessageListener, OutgoingChatMessageListener {

    private const val TAG = "SmackChatBridge"
    private var chatManager: ChatManager? = null

    private var smackChatBridgeIncomingChatMessageListener: IncomingChatMessageListener? = null
    private var smackChatBridgeOutgoingChatMessageListener: OutgoingChatMessageListener? = null

    override fun newIncomingMessage(
        from: EntityBareJid?,
        message: Message?,
        chat: Chat?
    ) {
        Log.i(TAG, "newIncomingMessage: ${chat.toString()}")
        smackChatBridgeIncomingChatMessageListener?.let {
            it.newIncomingMessage(
                from,
                message,
                chat
            )
        }
    }

    override fun newOutgoingMessage(
        to: EntityBareJid?,
        messageBuilder: MessageBuilder?,
        chat: Chat?
    ) {
        Log.i(TAG, "newOutgoingMessage: ${chat.toString()}")
        smackChatBridgeOutgoingChatMessageListener?.let {
            it.newOutgoingMessage(to, messageBuilder, chat)
        }
    }

    fun generateChatMessage(message: String): String {
        return message
    }

    fun generateChatMessage(message: String, stanzaId: String): Message {
        val newMessage = Message()
        newMessage.stanzaId = stanzaId
        newMessage.type = Message.Type.chat
        newMessage.body = message
        return newMessage
    }

    fun generateChatManager(connection: AbstractXMPPConnection): ChatManager? {
        if (chatManager == null) {
            chatManager = ChatManager.getInstanceFor(connection)
            chatManager?.addIncomingListener(this@SmackMessagingBridgeCopy)
            chatManager?.addOutgoingListener(this@SmackMessagingBridgeCopy)
        }

        return chatManager
    }

    fun getChatManager(): ChatManager? {
        return chatManager
    }

    fun getChatHistory(connection: AbstractXMPPConnection, target: String) {

        val jid = JidCreate.from(target + "@" + PublicValue.DEFAULT_DOMAIN)

        CoroutineScope(Dispatchers.IO).launch {

            var mamManager = MamManager.getInstanceFor(connection)

            mamManager.enableMamForAllMessages()

            Log.i(TAG, "mamManager support -> : ${mamManager.isSupported}")

            try {

                val mamQueryArgs = MamQueryArgs.builder()
                    .limitResultsToJid(jid)
                    .setResultPageSizeTo(10)
                    .queryLastPage()
                    .build()

                val mamQuery = mamManager.queryArchive(mamQueryArgs)

                Log.i(TAG, "getChatHistory: mamQuery -> ${mamQuery.messageCount}")
                // Log.i(TAG, "getChatHistory: mamQuery -> ${mamQuery.messages.toString()}")

                for (i in 0..mamQuery.messageCount) {
                    Log.i(TAG, "getChatHistory: message -> ${mamQuery.messages[i].body}")
                }

                // val archivePref = mamManager.retrieveArchivingPreferences()
                // Log.i(TAG, "getChatHistory: archivePref -> ${archivePref.toString()}")

            } catch (e: Exception) {
                Log.e(TAG, "getChatHistory: $e")
            }
        }
    }

    fun sendChatMessage(chatManager: ChatManager?, message: String, target: String) {
        chatManager?.let {
            val jid = JidCreate.entityBareFrom(target)
            val chat: Chat = it.chatWith(jid)
            chat.send(message)
        }
    }

    fun sendChatMessage(chatManager: ChatManager?, message: Message, target: String) {
        chatManager?.let {
            // val jid = JidCreate.entityBareFrom(target)
            val jid = JidCreate.from(target + "@" + PublicValue.DEFAULT_DOMAIN)
            val chat: Chat = it.chatWith(jid.asEntityBareJidIfPossible())
            chat.send(message)
        }
    }

    fun setIncomingChatMessageListener(incomingChatMessageListener: IncomingChatMessageListener) {
        this.smackChatBridgeIncomingChatMessageListener = incomingChatMessageListener
    }

    fun setOutgoingChatMessageListener(outgoingChatMessageListener: OutgoingChatMessageListener) {
        this.smackChatBridgeOutgoingChatMessageListener = outgoingChatMessageListener
    }

}