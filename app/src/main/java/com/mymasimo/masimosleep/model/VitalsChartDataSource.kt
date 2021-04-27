package com.mymasimo.masimosleep.model

import com.masimo.timelinechart.ViewStyle
import com.masimo.timelinechart.data.Coordinate
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import org.joda.time.LocalDateTime
import org.joda.time.Seconds
import java.math.RoundingMode
import kotlin.collections.ArrayList

class VitalsChartDataSource {

    private var allCoordinatesReversed: ArrayList<Coordinate> = ArrayList()
    private var coordinates: List<List<Coordinate>> = emptyList()

    var lowHighText: String = ""

    fun update(
        points: List<LineGraphViewData.LineGraphPoint>,
        readingType: ReadingType,
        viewStyle: ViewStyle
    ) {
        allCoordinatesReversed = ArrayList()

        var minVal: Double = Double.MAX_VALUE
        var maxVal: Double = Double.MIN_VALUE

        val (minReading, maxReading) = when (readingType) {
            ReadingType.SP02 -> Pair(50.0, 100.0)
            ReadingType.PR -> Pair(20.0, 160.0)
            ReadingType.RRP -> Pair(4.0, 40.0)
            ReadingType.DEFAULT -> Pair(0.0, 1.0)
        }
        val minMaxDiffReading = maxReading - minReading

        for (point in points.sortedByDescending { it.timestamp }) {
            if (point.value < minVal) {
                minVal = point.value
            }
            if (point.value > maxVal) {
                maxVal = point.value
            }

            val coordinate = Coordinate(
                LocalDateTime(point.timestamp),
                (point.value - minReading / minMaxDiffReading).toFloat()
            )
            allCoordinatesReversed.add(coordinate)
        }

        val lowRounded = minVal
            .toBigDecimal()
            .setScale(1, RoundingMode.UP)
            .toDouble()

        val highRounded = maxVal
            .toBigDecimal()
            .setScale(1, RoundingMode.UP)
            .toDouble()

        lowHighText = "$lowRounded - $highRounded"
        coordinates = prepareCoordinates(viewStyle)
    }

    /**
     * Prepares coordinates for the chart by excluding those that close to each other.
     */
    private fun prepareCoordinates(viewStyle: ViewStyle): List<List<Coordinate>> {
        if (allCoordinatesReversed.isEmpty()) {
            return emptyList()
        }

        val result: ArrayList<ArrayList<Coordinate>> = ArrayList()
        var section: ArrayList<Coordinate> = ArrayList()
        val shouldUseMixMaxAlgorithm = when (viewStyle) {
            ViewStyle.MINUTES -> false
            ViewStyle.HOURS, ViewStyle.DAYS -> true
        }
        val intervalMultiplier = if (shouldUseMixMaxAlgorithm) 2 else 1
        val stepInterval = when (viewStyle) {
            ViewStyle.MINUTES -> Seconds.seconds(intervalMultiplier * 60)
            ViewStyle.HOURS -> Seconds.seconds(intervalMultiplier * 3 * 60)
            ViewStyle.DAYS -> Seconds.seconds(intervalMultiplier * 16 * 60)
        }

        // Move timeline window from newest to oldest.
        var rest: List<Coordinate> = allCoordinatesReversed
        var stepStartDate =
            allCoordinatesReversed.first().dateTime.withMillisOfSecond(0).withSecondOfMinute(0)
        var stepEndDate = stepStartDate.minus(stepInterval)

        while (rest.isNotEmpty()) {
            // Split to get the current timeline window.
            val stepCoordinates = rest.takeWhile { it.dateTime.isAfter(stepEndDate) }
            rest = rest.dropWhile { it.dateTime.isAfter(stepEndDate) }
            if (stepCoordinates.isEmpty()) {
                if (section.isNotEmpty()) {
                    result.add(section)
                    section = ArrayList()
                }
            } else {
                if (shouldUseMixMaxAlgorithm && stepCoordinates.count() > 1) {
                    // Get min and max coordinates for this timeline window.
                    var newest = stepCoordinates.minByOrNull { it.value }
                    var oldest = stepCoordinates.maxByOrNull { it.value }
                    if (newest != null && oldest != null) {
                        if (oldest.dateTime.isAfter(newest.dateTime)) {
                            // Swap, so they are in order.
                            val temp = newest; newest = oldest; oldest = temp
                        }
                        section.add(Coordinate(stepStartDate, newest.value))
                        section.add(
                            Coordinate(
                                stepStartDate.minus(stepInterval.dividedBy(2)),
                                oldest.value
                            )
                        )
                    }
                } else {
                    section.add(stepCoordinates.first())
                }
            }

            // Calculated bounds for the next timeline window.
            stepStartDate = stepEndDate
            stepEndDate = stepStartDate.minus(stepInterval)
        }
        if (section.isNotEmpty()) {
            result.add(section)
        }
        return result.reversed()
    }
}
