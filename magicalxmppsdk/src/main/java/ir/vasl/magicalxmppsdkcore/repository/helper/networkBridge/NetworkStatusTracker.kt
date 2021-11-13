package ir.vasl.magicalxmppsdkcore.repository.helper.networkBridge

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import ir.vasl.magicalxmppsdkcore.repository.enum.NetworkStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkStatusTracker(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @ExperimentalCoroutinesApi
    val networkStatus: Flow<NetworkStatus> = callbackFlow<NetworkStatus> {
        offer(NetworkStatus.UNAVAILABE)
        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onUnavailable() {
                offer(NetworkStatus.UNAVAILABE)
            }

            override fun onAvailable(network: Network) {
                offer(NetworkStatus.AVAILABLE)
            }

            override fun onLost(network: Network) {
                offer(NetworkStatus.UNAVAILABE)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkStatusCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkStatusCallback)
        }
    }
}