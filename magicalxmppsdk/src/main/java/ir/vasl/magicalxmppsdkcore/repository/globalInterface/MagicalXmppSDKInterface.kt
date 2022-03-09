package ir.vasl.magicalxmppsdkcore.repository.globalInterface

import ir.vasl.magicalxmppsdkcore.repository.enum.ConnectionStatus
import ir.vasl.magicalxmppsdkcore.repository.enum.NetworkStatus
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage

interface MagicalXmppSDKInterface {
    fun onNetworkStatusChanged(networkStatus: NetworkStatus)
    fun onConnectionStatusChanged(connectionStatus: ConnectionStatus)
    fun onNewIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage)
    fun onNewIncomingMessageHistory(magicalIncomingMessageHistoryList: MutableList<MagicalIncomingMessage>)
    fun onNewOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage)
}