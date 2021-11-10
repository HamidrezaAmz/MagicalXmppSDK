package ir.vasl.magicalxmppsdk.repository.globalInterface

import ir.vasl.magicalxmppsdk.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdk.repository.model.MagicalOutgoingMessage

interface MessagingBridgeInterface {
    fun newIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage)
    fun newOutgoingMessage(magicalOutgoingMessage:MagicalOutgoingMessage)
}