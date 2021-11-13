package ir.vasl.magicalxmppsdkcore.repository.helper.smackBridge

import android.util.Log
import ir.vasl.magicalxmppsdkcore.repository.PublicValue.Companion.TAG
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MessagingBridgeInterface
import ir.vasl.magicalxmppsdkcore.repository.helper.IdGeneratorHelper
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.MessageBuilder
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate

class SmackMessagingBridge private constructor(
    connection: AbstractXMPPConnection,
    builder: Builder
) :
    IncomingChatMessageListener, OutgoingChatMessageListener {

    private lateinit var chatManager: ChatManager

    private var connection: AbstractXMPPConnection = connection
    private var domain = builder.domain
    private var messagingBridgeInterface: MessagingBridgeInterface? =
        builder.messagingBridgeInterface

    data class Builder(val connection: AbstractXMPPConnection) {
        var domain: String? = null
        var messagingBridgeInterface: MessagingBridgeInterface? = null
        fun setDomain(domain: String) = apply { this.domain = domain }
        fun setCallback(messagingBridgeInterface: MessagingBridgeInterface?) =
            apply { this.messagingBridgeInterface = messagingBridgeInterface }

        fun build() = SmackMessagingBridge(connection, this)
    }

    init {
        // -- Init chat manager based on new connection
        initChatManager()
    }

    private fun initChatManager() {
        if (this::chatManager.isInitialized.not()) {
            chatManager = ChatManager.getInstanceFor(connection)
            chatManager.addIncomingListener(this)
            chatManager.addOutgoingListener(this)
        }
    }

    override fun newIncomingMessage(
        from: EntityBareJid?,
        message: Message?,
        chat: Chat?
    ) {
        Log.i(TAG, "newIncomingMessage | Instance hashCode: ${hashCode()}")
        Log.i(TAG, "newIncomingMessage | message: $message")
        messagingBridgeInterface?.newIncomingMessage(
            MagicalIncomingMessage(
                id = IdGeneratorHelper.getRandomId(),
                message = message?.body ?: "No Message Found!",
                from = from.toString()
            )
        )
    }

    override fun newOutgoingMessage(
        to: EntityBareJid?,
        messageBuilder: MessageBuilder?,
        chat: Chat?
    ) {
        Log.i(TAG, "newOutgoingMessage | Instance hashCode: ${hashCode()}")
        Log.i(TAG, "newOutgoingMessage | message: ${messageBuilder?.body}")
        messagingBridgeInterface?.newOutgoingMessage(
            MagicalOutgoingMessage(
                id = IdGeneratorHelper.getRandomId(),
                message = messageBuilder?.body,
                to = to?.localpart?.asUnescapedString()
            )
        )
    }

    private fun generateMessage(message: String?, stanzaId: String?): Message {
        val newMessage = Message()
        newMessage.stanzaId = stanzaId
        newMessage.type = Message.Type.chat
        newMessage.body = message
        return newMessage
    }

    fun sendNewMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
        val message = generateMessage(magicalOutgoingMessage.message, magicalOutgoingMessage.to)
        val targetJid = magicalOutgoingMessage.to + "@" + domain
        val jid = JidCreate.from(targetJid)
        val chat: Chat = chatManager.chatWith(jid.asEntityBareJidIfPossible())
        chat.send(message)
    }

    fun disconnect() {
        Log.i(TAG, "destroy | Instance hashCode: ${hashCode()}")
        chatManager.removeIncomingListener(this)
        chatManager.removeOutgoingListener(this)
    }

}