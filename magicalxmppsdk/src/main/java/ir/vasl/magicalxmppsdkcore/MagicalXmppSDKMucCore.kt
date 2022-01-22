package ir.vasl.magicalxmppsdkcore

import android.content.Context
import android.util.Log
import ir.vasl.magicalxmppsdkcore.repository.PublicValue
import ir.vasl.magicalxmppsdkcore.repository.PublicValue.Companion.TAG
import ir.vasl.magicalxmppsdkcore.repository.enum.ConnectionStatus
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.ConnectionBridgeInterface
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MagicalXmppSDKInterface
import ir.vasl.magicalxmppsdkcore.repository.helper.connectionBridge.SmackConnectionBridge
import ir.vasl.magicalxmppsdkcore.repository.helper.multiUserBridge.SmackMucMessagingBridge
import ir.vasl.magicalxmppsdkcore.repository.helper.networkBridge.NetworkStatusTracker
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MagicalXmppSDKMucCore private constructor(context: Context, builder: Builder) {

    private var username: String = builder.username ?: PublicValue.DEFAULT_USERNAME
    private var password: String = builder.password ?: PublicValue.DEFAULT_PASSWORD
    private var domain: String = builder.domain ?: PublicValue.DEFAULT_DOMAIN
    private var host: String = builder.host ?: PublicValue.DEFAULT_HOST
    private var port: Int = builder.port ?: PublicValue.DEFAULT_PORT
    private var room: String = builder.room ?: PublicValue.DEFAULT_ROOM
    private var magicalXmppSDKInterface: MagicalXmppSDKInterface? = builder.magicalXmppSDKInterface
    private var jobParent: Job = Job()
    private var jobNetworkTracker: Job = Job()
    private var jobXmppConnection: Job = Job()
    private val scope: CoroutineScope = CoroutineScope(jobParent + Dispatchers.IO)
    private val networkStatusTracker: NetworkStatusTracker = NetworkStatusTracker(context)

    private lateinit var smackConnectionBridgeInstance: SmackConnectionBridge

    // private lateinit var smackMessagingBridgeInstance: SmackMessagingBridge
    private lateinit var smackMucMessagingBridgeInstance: SmackMucMessagingBridge

    data class Builder(val context: Context) {

        var username: String? = null
        var password: String? = null
        var domain: String? = null
        var host: String? = null
        var port: Int? = null
        var room: String? = null
        var magicalXmppSDKInterface: MagicalXmppSDKInterface? = null

        fun setUsername(username: String?) = apply {
            this.username = username
        }

        fun setPassword(password: String?) = apply {
            this.password = password
        }

        fun setDomain(domain: String?) = apply {
            this.domain = domain
        }

        fun setHost(host: String?) = apply {
            this.host = host
        }

        fun setPort(port: Int?) = apply {
            this.port = port
        }

        fun setRoom(room: String?) = apply {
            this.room = room
        }

        fun setCallback(magicalXmppSDKInterface: MagicalXmppSDKInterface) = apply {
            this.magicalXmppSDKInterface = magicalXmppSDKInterface
        }

        fun build() = MagicalXmppSDKMucCore(context, this)
    }

    init {

        // -- RUN COROUTINE NETWORK TRACKER
        runNetworkTracker()

        // -- RUN AND INIT XMPP CONNECTION BRIDGE
        runConnectionBridge()

    }

    private fun runNetworkTracker() {
        jobNetworkTracker = scope.launch {
            networkStatusTracker.networkStatus.collect {
                withContext(Dispatchers.Main) {
                    magicalXmppSDKInterface?.onNetworkStatusChanged(it)
                }
            }
        }
    }

    private fun runConnectionBridge() {

        // TODO: 7/20/21 Prevent from initializing new instance ( this should be singleton )
        if (this::smackConnectionBridgeInstance.isInitialized)
            return

        Log.i(TAG, "runConnectionBridge | Instance hashCode: ${hashCode()}")

        jobXmppConnection = scope.launch {
            smackConnectionBridgeInstance =
                SmackConnectionBridge.Builder(username, password, domain)
                    .setHost(host)
                    .setPort(port)
                    .setCallback(object : ConnectionBridgeInterface {
                        override fun onConnectionStatusChanged(connectionStatus: ConnectionStatus) {
                            scope.launch {
                                withContext(Dispatchers.Main) {
                                    magicalXmppSDKInterface?.onConnectionStatusChanged(
                                        connectionStatus
                                    )
                                }
                            }
                            runMultiUserMessagingBridge(connectionStatus)
                        }
                    })
                    .build()
        }
    }

    private fun runMultiUserMessagingBridge(connectionStatus: ConnectionStatus) {

        // TODO: 7/20/21 Prevent from initializing new instance ( this should be singleton )
        if (this::smackMucMessagingBridgeInstance.isInitialized)
            return

        if (connectionStatus == ConnectionStatus.AUTHENTICATED) {
            Log.i(TAG, "runMultiUserMessagingBridge | Instance hashCode: ${hashCode()}")

            val connection = smackConnectionBridgeInstance.getConnectionInstance()
            smackMucMessagingBridgeInstance = SmackMucMessagingBridge.Builder(connection)
                .setDomain(domain)
                .setRoom(room)
                .setCallback(null)
                .build()
        }
    }

    fun sendNewMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
        if (::smackMucMessagingBridgeInstance.isInitialized)
            smackMucMessagingBridgeInstance.sendNewMessage(magicalOutgoingMessage)
    }

    fun getChatHistory(room: String) {
        if (::smackMucMessagingBridgeInstance.isInitialized)
            smackMucMessagingBridgeInstance.getChatHistory(room)
    }

    fun getConnectionStatus(): ConnectionStatus {
        return if (::smackConnectionBridgeInstance.isInitialized)
            return smackConnectionBridgeInstance.getConnectionStatus()
        else
            ConnectionStatus.UNKNOWN
    }

    fun disconnect() {
        if (::smackConnectionBridgeInstance.isInitialized)
            smackConnectionBridgeInstance.disconnect()

        if (::smackMucMessagingBridgeInstance.isInitialized)
            smackMucMessagingBridgeInstance.disconnect()

        jobParent.cancel()
        jobNetworkTracker.cancel()
        jobXmppConnection.cancel()
    }

}