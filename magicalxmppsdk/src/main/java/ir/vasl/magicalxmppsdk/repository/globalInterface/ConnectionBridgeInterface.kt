package ir.vasl.magicalxmppsdk.repository.globalInterface

import ir.vasl.magicalxmppsdk.repository.enum.ConnectionStatus

interface ConnectionBridgeInterface {
    fun onConnectionStatusChanged(connectionStatus: ConnectionStatus)
}