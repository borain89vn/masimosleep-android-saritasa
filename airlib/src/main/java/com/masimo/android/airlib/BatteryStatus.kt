package com.masimo.android.airlib

enum class BatteryStatus {
    NORMAL,
    BATTERY_LOW_WARNING,
    DEPLETED_BATTERY,
    BATTERY_SUFFOCATION_WARNING,
    SUFFOCATED_BATTERY;
}

fun batteryStatusFromInt(status: Int) = BatteryStatus.values()[status]