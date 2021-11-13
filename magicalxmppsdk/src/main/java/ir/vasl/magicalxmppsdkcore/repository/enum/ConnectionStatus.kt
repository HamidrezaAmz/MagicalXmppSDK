package ir.vasl.magicalxmppsdkcore.repository.enum

enum class ConnectionStatus(val key: Int, val value: String) {
    CONNECTING(0, "Connecting"),
    CONNECTED(1, "Connected"),
    AUTHENTICATED(2, "Authenticated"),
    DISCONNECTED(3, "Disconnected"),
    FAILED(4, "Failed"),
    RETRYING(5, "Retrying"),
    UNKNOWN(6, "Unknown")
}