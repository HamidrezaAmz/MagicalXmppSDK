package ir.vasl.magicalxmppsdk.repository.helper.smackBridge

import android.util.Log
import ir.vasl.magicalxmppsdk.repository.PublicValue
import ir.vasl.magicalxmppsdk.repository.PublicValue.Companion.TAG
import ir.vasl.magicalxmppsdk.repository.`interface`.ConnectionBridgeInterface
import ir.vasl.magicalxmppsdk.repository.enum.ConnectionStatus
import kotlinx.coroutines.*
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smack.util.TLSUtils

class SmackConnectionBridge private constructor(builder: Builder) : ConnectionListener {

    private lateinit var connection: AbstractXMPPConnection

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var username: String = builder.username
    private var password: String = builder.password
    private var domain: String = builder.domain
    private var host: String = builder.host ?: PublicValue.DEFAULT_HOST
    private var port: Int = builder.port ?: PublicValue.DEFAULT_PORT

    private var connectionBridgeInterface: ConnectionBridgeInterface? =
        builder.connectionBridgeInterface

    private var RETRY_INTERVAL: Long = 2000
    private var DELAY_INTERVAL: Long = 2000
    private var INTERVAL_MAX: Int = 10000

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.i(TAG, "CoroutineExceptionHandler | Instance hashCode: ${hashCode()}")
        Log.i(TAG, "CoroutineExceptionHandler: $throwable")

        connectionBridgeInterface?.onConnectionStatusChanged(ConnectionStatus.FAILED)
        reConnect()
    }

    data class Builder(
        var username: String,
        var password: String,
        var domain: String
    ) {
        var host: String? = null
        var port: Int? = null
        var connectionBridgeInterface: ConnectionBridgeInterface? = null
        fun setHost(host: String) = apply { this.host = host }
        fun setPort(port: Int) = apply { this.port = port }
        fun setCallback(connectionBridgeInterface: ConnectionBridgeInterface) =
            apply { this.connectionBridgeInterface = connectionBridgeInterface }

        fun build() = SmackConnectionBridge(this)
    }

    init {
        val connectionConfig = generateConnectionConfig(username, password, domain, host, port)
        generateConnectionInstance(connectionConfig)
        connect()
    }

    override fun connecting(connection: XMPPConnection?) {
        super.connecting(connection)
        Log.i(TAG, "connecting | Instance hashCode: ${hashCode()}")

        connectionBridgeInterface?.onConnectionStatusChanged(ConnectionStatus.CONNECTING)
    }

    override fun connected(connection: XMPPConnection?) {
        super.connected(connection)
        Log.i(TAG, "connected | Instance hashCode: ${hashCode()}")

        connectionBridgeInterface?.onConnectionStatusChanged(ConnectionStatus.CONNECTED)
    }

    override fun authenticated(connection: XMPPConnection?, resumed: Boolean) {
        super.authenticated(connection, resumed)
        Log.i(TAG, "authenticated | Instance hashCode: ${hashCode()}")

        connectionBridgeInterface?.onConnectionStatusChanged(ConnectionStatus.AUTHENTICATED)
        DELAY_INTERVAL = RETRY_INTERVAL // -- Refresh reConnect interval
    }

    override fun connectionClosed() {
        super.connectionClosed()
        Log.i(TAG, "connectionClosed | Instance hashCode: ${hashCode()}")

        connectionBridgeInterface?.onConnectionStatusChanged(ConnectionStatus.DISCONNECTED)
    }

    override fun connectionClosedOnError(e: java.lang.Exception?) {
        super.connectionClosedOnError(e)
        Log.i(TAG, "connectionClosedOnError | Instance hashCode: ${hashCode()}")
        Log.i(TAG, "connectionClosedOnError | Exception: $e")

        connectionBridgeInterface?.onConnectionStatusChanged(ConnectionStatus.FAILED)
        reConnect()
    }

    private fun generateConnectionConfig(
        username: String,
        password: String,
        domain: String,
        host: String,
        port: Int
    ): XMPPTCPConnectionConfiguration {

        return XMPPTCPConnectionConfiguration.builder()
            .setUsernameAndPassword(username, password)
            .setXmppDomain(domain)
            .setHost(host)
            .setPort(port)
            .enableDefaultDebugger()
            .setCompressionEnabled(false)
            .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
            .setCustomX509TrustManager(TLSUtils.AcceptAllTrustManager())
            .build()
    }

    private fun generateConnectionInstance(config: XMPPTCPConnectionConfiguration): AbstractXMPPConnection {
        config.let { xmppConfig ->
            connection = XMPPTCPConnection(xmppConfig)
            connection.addConnectionListener(this)
            return connection
        }
    }

    private fun connect() {
        scope.launch(exceptionHandler) {
            connection.connect().login()
        }
    }

    private fun reConnect() {

        Log.i(TAG, "reConnect | Instance hashCode: ${hashCode()}")
        Log.i(TAG, "reConnect: after $DELAY_INTERVAL sec")

        scope.launch {
            delay(DELAY_INTERVAL)
            connect()

            if (DELAY_INTERVAL < INTERVAL_MAX)
                DELAY_INTERVAL += RETRY_INTERVAL
        }
    }

    fun getConnectionInstance(): AbstractXMPPConnection {
        return connection
    }

    fun getConnectionStatus(): ConnectionStatus {
        return if (this::connection.isInitialized.not())
            ConnectionStatus.DISCONNECTED
        else if (connection.isAuthenticated.not())
            ConnectionStatus.DISCONNECTED
        else if (connection.isConnected && connection.isAuthenticated)
            ConnectionStatus.CONNECTED
        else
            ConnectionStatus.UNKNOWN
    }

    fun disconnect() {
        Log.i(TAG, "disconnect | Instance hashCode: ${hashCode()}")

        scope.launch {
            connection.disconnect()
        }
    }

}