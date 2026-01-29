package com.example.sms2line.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.sms2line.worker.EmailQueueManager

class NetworkMonitor(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var isRegistered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available, triggering email queue retry")
            EmailQueueManager.scheduleImmediateRetry(context)
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Network lost")
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val validated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            if (hasInternet && validated) {
                Log.d(TAG, "Network validated, triggering email queue retry")
                EmailQueueManager.scheduleImmediateRetry(context)
            }
        }
    }

    fun startMonitoring() {
        if (isRegistered) return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
            isRegistered = true
            Log.d(TAG, "Network monitoring started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    fun stopMonitoring() {
        if (!isRegistered) return

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isRegistered = false
            Log.d(TAG, "Network monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister network callback", e)
        }
    }

    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    companion object {
        private const val TAG = "NetworkMonitor"
    }
}
