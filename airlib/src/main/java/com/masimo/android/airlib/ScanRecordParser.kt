package com.masimo.android.airlib

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScanRecordParser(data: ByteArray, private val mTargetProductTypeSet: Set<ProductType>, private val mTargetProductVariantSet: Set<ProductVariant>,
                       private val mTargetSerialNumberSet: Set<SerialNumber>?) {
    private var position = 0
    private val bufferArray: ByteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

    var foundSupportedDevice = false
    private set

    lateinit var productType: ProductType
        private set
    lateinit var productVariant: ProductVariant
        private set

    private var mStructureVersion: StructureVersion? = null

    /**
     * Returns the serial number found in the scan.
     */
    var serialNumber: SerialNumber? = null
        private set
    var firmwareVersion = ""
        private set

    /**
     * Utility method that parses scanRecord byte array returned in onLeScan callback.
     * Ref: https://agile.masimo.com/ : ADS-2556C
     * Ref: Bluetooth.org
     * SafetyNetPPGHub Example
     * Ad type ref : https://www.bluetooth.com/specifications/assigned-numbers/generic-access-profile/
     * adLength adType companyId structureVersion productType(HUB) HubVariant fwVersion 10-Byte ASCII Serial
     * 12       ff     4302      02               06               00         0100      37314246424143393545
     */
    fun parse() {
        mStructureVersion = structureVersionFromByte(nextByte)
        when (mStructureVersion) {
            StructureVersion.V1 -> parseV1()
            StructureVersion.V2 -> {
                parseV2()
                return
            }
            else                -> return
        }
    }

    private fun parseV2() {
        productType = productTypeFromByte(nextByte).also {
            if (!mTargetProductTypeSet.contains(it)) return
        }

        // product type was assigned above
        productVariant = productType.variantFromByte(nextByte).also {
            if (!mTargetProductVariantSet.contains(it)) return
        }

        if (productType !== ProductType.AIR_SPO2 || productVariant !== ProductVariant.AIR_SPO2_P05) {
            return
        }

        firmwareVersion = Integer.toHexString(-nextShort and 0xffff)
        val macAddressByteArray = ByteArray(6)
        for (i in 0..5) {
            macAddressByteArray[i] = nextByte
        }
        val macAddress = getMacStringFromBytes(macAddressByteArray)
        serialNumber = SerialNumber(macAddress)

        mTargetSerialNumberSet?.let {
            if (it.isEmpty() || it.contains(serialNumber!!))
                foundSupportedDevice = true
        } ?: run {
            foundSupportedDevice = true
        }
    }

    private fun parseV1() {
        productType = productTypeFromByte(nextByte).also {
            if (!mTargetProductTypeSet.contains(it)) return
        }

        productVariant = productType.variantFromByte(nextByte)

        serialNumber = SerialNumber(nextInt.toLong() and 0x00000000FFFFFFFFL)

        mTargetSerialNumberSet?.let {
            if (it.isEmpty() || it.contains(serialNumber!!))
                foundSupportedDevice = true
        } ?: run {
            foundSupportedDevice = true
        }
    }

    @Suppress("unused") // may be needed later
    private val nextLong: Long
        get() {
            val result = bufferArray.getLong(position)
            position += 8
            return result
        }

    private val nextInt: Int
        get() {
            val result = bufferArray.getInt(position)
            position += 4
            return result
        }

    private val nextShort: Short
        get() {
            val result = bufferArray.getShort(position)
            position += 2
            return result
        }

    private val nextByte: Byte
        get() = bufferArray[position++]

    private fun getMacStringFromBytes(macAddressByteArray: ByteArray): String {
        val sb = StringBuilder(18)
        for (b in macAddressByteArray) {
            if (sb.isNotEmpty()) sb.append(':')
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

}