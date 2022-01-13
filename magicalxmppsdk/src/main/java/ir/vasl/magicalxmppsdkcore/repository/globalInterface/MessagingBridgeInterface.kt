package ir.vasl.magicalxmppsdkcore.repository.globalInterface

import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage

interface MessagingBridgeInterface {
    fun newIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage)
    fun newIncomingMessageHistory(magicalIncomingMessageHistoryList: List<MagicalIncomingMessage>)
    fun newOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage)
}