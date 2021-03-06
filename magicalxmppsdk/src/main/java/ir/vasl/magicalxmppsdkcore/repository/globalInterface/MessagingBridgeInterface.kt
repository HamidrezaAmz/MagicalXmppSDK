package ir.vasl.magicalxmppsdkcore.repository.globalInterface

import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage

interface MessagingBridgeInterface {
    fun newIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage)
    fun newOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage)
    fun messagingBridgeError(errorMessage: String) {}
}