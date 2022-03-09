package ir.vasl.magicalxmppsdkcore.repository.globalInterface

import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage

interface MessagingHistoryInterface {
    fun newIncomingMessageHistory(messageHistoryList: MutableList<MagicalIncomingMessage>)
    fun newIncomingMessageHistoryError(errorMessage: String)
}