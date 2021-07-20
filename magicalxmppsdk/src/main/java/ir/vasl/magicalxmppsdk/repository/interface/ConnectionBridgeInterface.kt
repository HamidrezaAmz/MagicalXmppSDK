package ir.vasl.magicalxmppsdk.repository.`interface`

import ir.vasl.magicalxmppsdk.repository.enum.ConnectionStatus

interface ConnectionBridgeInterface {
    fun onConnectionStatusChanged(connectionStatus: ConnectionStatus)
}