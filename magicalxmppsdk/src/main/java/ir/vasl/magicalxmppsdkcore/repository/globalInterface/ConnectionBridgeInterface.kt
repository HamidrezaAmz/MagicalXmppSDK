package ir.vasl.magicalxmppsdkcore.repository.globalInterface

import ir.vasl.magicalxmppsdkcore.repository.enum.ConnectionStatus

interface ConnectionBridgeInterface {
    fun onConnectionStatusChanged(connectionStatus: ConnectionStatus)
}