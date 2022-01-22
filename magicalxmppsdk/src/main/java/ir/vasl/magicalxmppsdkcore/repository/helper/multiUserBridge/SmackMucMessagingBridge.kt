package ir.vasl.magicalxmppsdkcore.repository.helper.multiUserBridge

import android.util.Log
import ir.vasl.magicalxmppsdkcore.repository.PublicValue.Companion.TAG
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MultiUserBridgeInterface
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart

class SmackMucMessagingBridge private constructor(
    connection: AbstractXMPPConnection,
    builder: SmackMucMessagingBridge.Builder
) : MessageListener {

    private var connection: AbstractXMPPConnection = connection
    private var domain = builder.domain
    private var room = builder.room
    private var multiUserBridgeInterface: MultiUserBridgeInterface? = builder.multiUserBridgeInterface

    private lateinit var multiUserChatManager: MultiUserChatManager
    private lateinit var multiUserChat: MultiUserChat

    data class Builder(val connection: AbstractXMPPConnection) {

        var domain: String? = null
        var room: String? = null
        var multiUserBridgeInterface: MultiUserBridgeInterface? = null

        fun setDomain(domain: String) = apply {
            this.domain = domain
        }

        fun setRoom(room: String) = apply {
            this.room = room
        }

        fun setCallback(multiUserBridgeInterface: MultiUserBridgeInterface?) = apply {
            this.multiUserBridgeInterface = multiUserBridgeInterface
        }

        fun build() = SmackMucMessagingBridge(connection, this)
    }

    init {
        // init Multi User Chat Manager
        initMultiUserChatManager()
    }

    private fun initMultiUserChatManager() {
        // Get the MultiUserChatManager
        multiUserChatManager = MultiUserChatManager.getInstanceFor(connection)
        initMultiUserChat()
    }

    private fun initMultiUserChat() {

        if (::multiUserChatManager.isInitialized.not())
            return

        // Create a MultiUserChat using an XMPPConnection for a room
        multiUserChat = multiUserChatManager.getMultiUserChat(getMucJid())
        multiUserChat.addMessageListener(this)

        joinRoom()
    }

    fun joinRoom() {

        if (::multiUserChat.isInitialized.not())
            return

        // User joins the new room
        // The room service will decide the amount of history to send
        if (multiUserChat.isJoined.not())
            multiUserChat.join(getNickName())
    }

    fun leaveRoom() {
        if (::multiUserChat.isInitialized.not())
            return

        // User leave the new room
        multiUserChat.leave()
    }

    fun sendNewMessage(message: String) {
        if (::multiUserChat.isInitialized.not())
            return

        // User send new message
        multiUserChat.sendMessage(message)
    }

    fun sendNewMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
        if (::multiUserChat.isInitialized.not())
            return

        // User send new message
        multiUserChat.sendMessage(magicalOutgoingMessage.message)
    }

    fun getChatHistory(room: String) {
        // todo: will be added
    }

    fun disconnect() {
        multiUserChat.removeMessageListener(this)
    }

    private fun getMucJid(): EntityBareJid {
        // Create the XMPP address (JID) of the MUC.
        val jid = room + "@" + "conference." + domain
        return JidCreate.bareFrom(jid) as EntityBareJid
    }

    private fun getNickName(): Resourcepart? {
        // Create the nickname.
        return Resourcepart.from("MY NICKNAME ;)")
    }

    override fun processMessage(message: Message?) {
        Log.i(TAG, "processMessage: $message")
    }
}