package ir.vasl.magicalxmppsdk.repository.`interface`

import ir.vasl.magicalxmppsdk.repository.enum.ConnectionStatus
import ir.vasl.magicalxmppsdk.repository.enum.NetworkStatus
import ir.vasl.magicalxmppsdk.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdk.repository.model.MagicalOutgoingMessage

interface MagicalXmppSDKInterface {
    fun onNetworkStatusChanged(networkStatus: NetworkStatus)
    fun onConnectionStatusChanged(connectionStatus: ConnectionStatus)
    fun onNewIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage)
    fun onNewOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage)
}