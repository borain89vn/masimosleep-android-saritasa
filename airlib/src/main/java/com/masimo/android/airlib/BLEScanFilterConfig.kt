package com.masimo.android.airlib

import android.bluetooth.le.ScanFilter

class BLEScanFilterConfig {
    val scanFilters: MutableList<ScanFilter>
    val deviceNameFilterSet: MutableSet<String>
    val productTypeFilterSet: MutableSet<ProductType>
    val productVariantFilterSet: MutableSet<ProductVariant>
    val serialNumberFilterSet: MutableSet<SerialNumber>

    init {
        scanFilters = arrayListOf()
        deviceNameFilterSet = mutableSetOf()
        productTypeFilterSet = mutableSetOf()
        productVariantFilterSet = mutableSetOf()
        serialNumberFilterSet = mutableSetOf()
    }
}
