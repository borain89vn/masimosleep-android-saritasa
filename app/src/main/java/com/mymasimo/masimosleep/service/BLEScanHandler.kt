package com.mymasimo.masimosleep.service

import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.masimo.android.airlib.MASIMO_BLUETOOTH_SIG_MANUFACTURER_ID
import com.masimo.android.airlib.ProductType
import com.masimo.android.airlib.ProductVariant
import com.masimo.android.airlib.ScanRecordParser
import com.mymasimo.masimosleep.BuildConfig.APPLICATION_ID
import timber.log.Timber
import java.util.*
import kotlin.math.roundToLong

private const val MSG_EXPIRE_MATCH = 100

private const val DEFAULT_MATCH_TTL_MS = 15000L
private const val BEACON_PERIODIC_INTERVAL_MS = 1.25f
private const val BEACON_PERIODIC_INTERVAL_THRESHOLD_MS = 1000

const val ACTION_BLE_SCAN_RESULT = "$APPLICATION_ID.action.BLE_SCAN_RESULT"
const val ACTION_BLE_SCAN_FAILED = "$APPLICATION_ID.action.BLE_SCAN_FAILED"

const val EXTRA_SCAN_RESULT = "scan_result"
const val EXTRA_ERROR_CODE = "error"
const val EXTRA_MATCH_LOST = "match_lost"

class BLEScanHandler(
    looper: Looper,
    private val cast: LocalBroadcastManager
) : Handler(looper) {

    private val encounteredNotSupported = mutableSetOf<String>()
    private val keyMap = mutableMapOf<String, String>()
    private val resultMap = mutableMapOf<String, ScanResult>()

    fun reset() = synchronized(resultMap) {
        removeMessages(MSG_EXPIRE_MATCH)
        resultMap.clear()
        keyMap.clear()
        encounteredNotSupported.clear()
    }

    fun timedOut() {
        cast.sendBroadcast(Intent(ACTION_BLE_SCAN_TIMEOUT))
        reset()
    }

    fun onMatchFound(result: ScanResult, callbackType: Int) = synchronized(resultMap) {
        val address = result.device?.address?.trim() ?: return@synchronized

        // we've seen this device and know it's not supported
        if (encounteredNotSupported.contains(address)) return@synchronized

        // We've already seen this device and know it's supported
        if (!(keyMap.containsKey(address) || result.isSupported())) {
            encounteredNotSupported.add(address)
            return@synchronized
        }

        /*
         The key that is used a few lines below in removeMessages is reference checked, not content
         checked. So even though the string content is identical, the actual messages are never
         removed because this address string isn't the same address string as the original entry.
         By putting it in a map, we preserve the same instance across the various content-equal
         instances of each device's address.
         */
        val key = keyMap.getOrPut(address) { address.toUpperCase(Locale.ROOT) }

        val delay = result.getTTL()

        removeMessages(MSG_EXPIRE_MATCH, key)

        if (callbackType == ScanSettings.CALLBACK_TYPE_MATCH_LOST) {
            Timber.d("Result '${result.device?.address}' key: $key. TTL: ${delay}ms (interval: ${result.periodicAdvertisingInterval()})")
            onMatchLost(address)
            return@synchronized
        }

        if (resultMap.put(address, result) == null) {
            Timber.d("Broadcasting for '$address'")
            cast.notifyScanResult(result)
        }

        sendMessageDelayed(obtainMessage(MSG_EXPIRE_MATCH, key), delay)
    }

    private fun onMatchLost(address: String) = synchronized(resultMap) {
        Timber.d("Lost '$address'")

        encounteredNotSupported.remove(address)

        keyMap.remove(address)?.let {
            removeMessages(MSG_EXPIRE_MATCH, it)
        }

        resultMap.remove(address)?.let {
            cast.notifyScanResult(it, true)
        }
    }

    override fun handleMessage(msg: Message) {
        if (msg.what != MSG_EXPIRE_MATCH) super.handleMessage(msg)
        else {
            val address = msg.obj as String

            Timber.d("Haven't seen '$address' in a while!")
            onMatchLost(address)
        }
    }

    fun scanFailed(errorCode: Int) {
        cast.notifyScanFailed(errorCode)
    }
}

private fun ScanResult.getTTL(): Long {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        DEFAULT_MATCH_TTL_MS
    } else if (periodicAdvertisingInterval == ScanResult.PERIODIC_INTERVAL_NOT_PRESENT) {
        DEFAULT_MATCH_TTL_MS
    } else {
        ((periodicAdvertisingInterval * BEACON_PERIODIC_INTERVAL_MS)
                + BEACON_PERIODIC_INTERVAL_THRESHOLD_MS).roundToLong()
    }
}

private fun ScanResult.isSupported(): Boolean {
    val record = scanRecord ?: return false

    record.manufacturerSpecificData ?: return false

    val id = MASIMO_BLUETOOTH_SIG_MANUFACTURER_ID

    record.getManufacturerSpecificData(id)?.let {
        return ScanRecordParser(it, setOf(ProductType.AIR_SPO2), setOf(ProductVariant.AIR_SPO2_P05), null).apply {
            parse()
        }.foundSupportedDevice
    } ?: return false
}

private fun LocalBroadcastManager.notifyScanResult(result: ScanResult, lost: Boolean = false): Boolean {
    return sendBroadcast(Intent(
        ACTION_BLE_SCAN_RESULT
    ).apply {
        putExtra(EXTRA_SCAN_RESULT, result)
        putExtra(EXTRA_MATCH_LOST, lost)
    })
}

private fun LocalBroadcastManager.notifyScanFailed(code: Int): Boolean {
    return sendBroadcast(Intent(
        ACTION_BLE_SCAN_FAILED
    ).apply {
        putExtra(EXTRA_ERROR_CODE, code)
    })
}


private fun ScanResult.periodicAdvertisingInterval(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        periodicAdvertisingInterval
    } else {
        ScanResult.PERIODIC_INTERVAL_NOT_PRESENT
    }
}