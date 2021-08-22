package com.mymasimo.masimosleep.util

import java.text.DecimalFormat

inline fun Double.format():String{
    val format = DecimalFormat("#.#")
    return format.format(this)
}