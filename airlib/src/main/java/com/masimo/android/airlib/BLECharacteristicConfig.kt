package com.masimo.android.airlib

import java.util.*

data class BLECharacteristicConfig(val notificationDescriptorCharacteristic: UUID,
                                   val serviceCharacteristic: UUID,
                                   val incomingCharacteristic: UUID,
                                   val outgoingCharacteristic: UUID)

fun createAIRConfig(): BLECharacteristicConfig = BLECharacteristicConfig(AIR_UPDATE_NOTIFICATION_DESCRIPTOR_UUID,
                                                                         AIR_SERVICE_UUID,
                                                                         AIR_INCOMING_UUID,
                                                                         AIR_OUTGOING_UUID)