package ir.vasl.magicalxmppsdkcore

import android.content.Context
import android.util.Log
import ir.vasl.magicalxmppsdkcore.repository.PublicValue
import ir.vasl.magicalxmppsdkcore.repository.PublicValue.Companion.TAG
import ir.vasl.magicalxmppsdkcore.repository.enum.ConnectionStatus
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.ConnectionBridgeInterface
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MagicalXmppSDKInterface
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MessagingBridgeInterface
import ir.vasl.magicalxmppsdkcore.repository.helper.networkBridge.NetworkStatusTracker
import ir.vasl.magicalxmppsdkcore.repository.helper.smackBridge.SmackConnectionBridge
import ir.vasl.magicalxmppsdkcore.repository.helper.smackBridge.SmackMessagingBridge
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
class MagicalXmppSDKCore private constructor(context: Context, builder: Builder) {

    private var username: String = builder.username ?: PublicValue.DEFAULT_USERNAME
    private var password: String = builder.password ?: PublicValue.DEFAULT_PASSWORD
    private var domain: String = builder.domain ?: PublicValue.DEFAULT_DOMAIN
    private var host: String = builder.host ?: PublicValue.DEFAULT_HOST
    private var port: Int = builder.port ?: PublicValue.DEFAULT_PORT
    private var magicalXmppSDKInterface: MagicalXmppSDKInterface? = builder.magicalXmppSDKInterface
    private var jobParent: Job = Job()
    private var jobNetworkTracker: Job = Job()
    private var jobXmppConnection: Job = Job()
    private val scope: CoroutineScope = CoroutineScope(jobParent + Dispatchers.IO)
    private val networkStatusTracker: NetworkStatusTracker = NetworkStatusTracker(context)

    private lateinit var smackConnectionBridgeInstance: SmackConnectionBridge
    private lateinit var smackMessagingBridgeInstance: SmackMessagingBridge

    data class Builder(val context: Context) {

        var username: String? = null
        var password: String? = null
        var domain: String? = null
        var host: String? = null
        var port: Int? = null
        var magicalXmppSDKInterface: MagicalXmppSDKInterface? = null

        fun setUsername(username: String?) = apply { this.username = username }
        fun setPassword(password: String?) = apply { this.password = password }
        fun setDomain(domain: String?) = apply { this.domain = domain }
        fun setHost(host: String?) = apply { this.host = host }
        fun setPort(port: Int?) = apply { this.port = port }
        fun setCallback(magicalXmppSDKInterface: MagicalXmppSDKInterface) = apply { this.magicalXmppSDKInterface = magicalXmppSDKInterface }

        fun build() = MagicalXmppSDKCore(context, this)
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
                            runMessagingBridge(connectionStatus)
                        }
                    })
                    .build()
        }
    }

    private fun runMessagingBridge(connectionStatus: ConnectionStatus) {

        // TODO: 7/20/21 Prevent from initializing new instance ( this should be singleton )
        if (this::smackMessagingBridgeInstance.isInitialized)
            return

        if (connectionStatus == ConnectionStatus.AUTHENTICATED) {
            Log.i(TAG, "runMessagingBridge | Instance hashCode: ${hashCode()}")

            val connection = smackConnectionBridgeInstance.getConnectionInstance()
            smackMessagingBridgeInstance = SmackMessagingBridge.Builder(connection)
                .setDomain(domain)
                .setCallback(object : MessagingBridgeInterface {
                    override fun newIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage) {
                        scope.launch {
                            withContext(Dispatchers.Main) {
                                magicalXmppSDKInterface?.onNewIncomingMessage(magicalIncomingMessage)
                            }
                        }
                    }

                    override fun newOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
                        scope.launch {
                            withContext(Dispatchers.Main) {
                                magicalXmppSDKInterface?.onNewOutgoingMessage(magicalOutgoingMessage)
                            }
                        }
                    }
                })
                .build()
        }
    }

    fun sendNewMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
        smackMessagingBridgeInstance.sendNewMessage(magicalOutgoingMessage)
    }

    fun getConnectionStatus(): ConnectionStatus {
        return smackConnectionBridgeInstance.getConnectionStatus()
    }

    fun disconnect() {
        if (::smackConnectionBridgeInstance.isInitialized)
            smackConnectionBridgeInstance.disconnect()
        if (::smackMessagingBridgeInstance.isInitialized)
            smackMessagingBridgeInstance.disconnect()
        jobParent.cancel()
        jobNetworkTracker.cancel()
        jobXmppConnection.cancel()
    }

}