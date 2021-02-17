package com.masimo.android.airlib

private const val QUEUE_SIZE = 100

internal class WaveformQueue {
    private val aQueue =
        java.lang.reflect.Array.newInstance(java.lang.Float.TYPE, *intArrayOf(QUEUE_SIZE, 2)) as Array<FloatArray>
    var count = 0
    private var deQueueIndex = 0
    private var enQueueIndex = 0
    fun Enqueue(pleth: FloatArray, sigIq: FloatArray) {
        for (i in pleth.indices) {
            Enqueue(pleth[i] * -1, sigIq[i])
        }
    }

    fun Enqueue(pleth: Float, sigIq: Float) {
        enQueueIndex = enQueueIndex % QUEUE_SIZE
        aQueue[enQueueIndex][0] = pleth
        aQueue[enQueueIndex][1] = sigIq
        enQueueIndex++
        count++
    }

    fun Dequeue(): FloatArray {
        val result = FloatArray(2)
        val i = count
        count = i - 1
        if (i > 0) {
            result[0] = aQueue[deQueueIndex][0]
            result[1] = aQueue[deQueueIndex][1]
            deQueueIndex = (deQueueIndex + 1) % QUEUE_SIZE
        }
        return result
    }
}