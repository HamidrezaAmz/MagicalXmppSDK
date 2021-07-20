package ir.vasl.magicalxmppsdk.repository.`interface`

import ir.vasl.magicalxmppsdk.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdk.repository.model.MagicalOutgoingMessage

interface MessagingBridgeInterface {
    fun newIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage)
    fun newOutgoingMessage(magicalOutgoingMessage:MagicalOutgoingMessage)
}