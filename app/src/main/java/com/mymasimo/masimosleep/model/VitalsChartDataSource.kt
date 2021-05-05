package com.mymasimo.masimosleep.model

import android.content.Context
import androidx.core.content.ContextCompat
import com.masimo.timelinechart.TimelineChartView
import com.masimo.timelinechart.ViewStyle
import com.masimo.timelinechart.data.AxisValue
import com.masimo.timelinechart.data.Coordinate
import com.masimo.timelinechart.data.Marker
import com.masimo.timelinechart.data.Zone
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import org.joda.time.LocalDateTime
import org.joda.time.Seconds
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

/**
 * Common data source for vitals timeline charts.
 */
class VitalsChartDataSource(
    private val isLive: Boolean,
    private val context: Context,
    private val readingType: ReadingType
) : TimelineChartView.DataSource {

    private var allCoordinatesReversed: ArrayList<Coordinate> = ArrayList()

    // Caching of the coordinates.
    private val coordinates: EnumMap<ViewStyle, List<List<Coordinate>>> =
        EnumMap(ViewStyle::class.java)

    private var axisValues: List<AxisValue> = emptyList()
    private var zones: List<Zone> = emptyList()

    private var chartViewStyle: ViewStyle = ViewStyle.MINUTES

    var lowHighText: String = ""

    init {
        axisValues = prepareAxisValues(readingType)
        zones = prepareZones(readingType)
    }

    fun switchViewStyle(viewStyle: ViewStyle) {
        chartViewStyle = viewStyle
    }

    fun update(points: List<LineGraphViewData.LineGraphPoint>) {
        allCoordinatesReversed = ArrayList()
        coordinates.clear()

        var minVal: Double = Double.MAX_VALUE
        var maxVal: Double = Double.MIN_VALUE

        val (minReading, maxReading) = minMax(readingType)
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
                ((point.value - minReading) / minMaxDiffReading).toFloat()
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
    }

    /**
     * Prepares axis values for the chart.
     */
    private fun prepareAxisValues(readingType: ReadingType): List<AxisValue> {
        val values = axisNumbers(readingType)
        return values.map {
            AxisValue(
                it.toString(),
                translated(it.toFloat(), values.first().toFloat(), values.last().toFloat())
            )
        }
    }

    private fun axisNumbers(readingType: ReadingType): List<Int> {
        return when (readingType) {
            ReadingType.SP02 -> listOf(50, 60, 70, 80, 90, 100)
            ReadingType.PR -> listOf(20, 40, 60, 80, 100, 120, 140, 160)
            ReadingType.RRP -> listOf(4, 13, 22, 31, 40)
            ReadingType.DEFAULT -> listOf(0, 1)
        }
    }

    private fun minMax(readingType: ReadingType): Pair<Float, Float> {
        val values = axisNumbers(readingType)
        return Pair(values.first().toFloat(), values.last().toFloat())
    }

    private fun prepareZones(readingType: ReadingType): List<Zone> {
        val colorValues = when (readingType) {
            ReadingType.SP02 -> listOf(
                Pair(R.color.low, 90),
                Pair(R.color.optimal_low, 97),
                Pair(R.color.optimal, 100)
            )

            ReadingType.PR -> listOf(
                Pair(R.color.low, 40),
                Pair(R.color.optimal_low, 70),
                Pair(R.color.optimal, 100),
                Pair(R.color.optimal_high, 140),
                Pair(R.color.high, 160)
            )

            ReadingType.RRP -> listOf(
                Pair(R.color.low, 2),
                Pair(R.color.optimal_low, 12),
                Pair(R.color.optimal, 18),
                Pair(R.color.optimal_high, 24),
                Pair(R.color.high, 40)
            )
            ReadingType.DEFAULT -> listOf(Pair(R.color.optimal, 1))
        }
        val (min, max) = minMax(readingType)
        return colorValues.map {
            Zone(
                ContextCompat.getColor(context, it.first),
                translated(it.second.toFloat(), min, max)
            )
        }
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
        val stepInterval = viewStyle.preferredPointTimeInterval().multipliedBy(intervalMultiplier)

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

    /**
     * Returns grouped coordinates for the chart.
     */
    private fun coordinatesForChartViewStyle(viewStyle: ViewStyle): List<List<Coordinate>> {
        return coordinates.getOrPut(viewStyle, { prepareCoordinates(viewStyle) })
    }

    override fun timelineChartViewLowerBoundDate(view: TimelineChartView): LocalDateTime {
        val coordinateDate =
            coordinatesForChartViewStyle(chartViewStyle).flatten()
                .minByOrNull { it.dateTime }?.dateTime
        return coordinateDate ?: LocalDateTime.now()
    }

    override fun timelineChartViewUpperBoundDate(view: TimelineChartView): LocalDateTime {
        return if (isLive) {
            LocalDateTime.now()
        } else {
            val coordinateDate =
                coordinatesForChartViewStyle(chartViewStyle).flatten()
                    .maxByOrNull { it.dateTime }?.dateTime
            coordinateDate ?: LocalDateTime.now()
        }
    }

    override fun timelineChartViewAxisValues(view: TimelineChartView): List<AxisValue> {
        return axisValues
    }

    override fun timelineChartViewZones(view: TimelineChartView): List<Zone> {
        return zones
    }

    override fun timelineChartViewCoordinateSections(
        view: TimelineChartView,
        dateRange: Pair<LocalDateTime, LocalDateTime>
    ): List<List<Coordinate>> {
        return coordinatesForChartViewStyle(chartViewStyle)
    }

    override fun timelineChartViewMarkers(
        view: TimelineChartView,
        dateRange: Pair<LocalDateTime, LocalDateTime>
    ): List<Marker> {
        return emptyList()
    }

    override fun timelineChartViewPrefetchInterval(view: TimelineChartView): Seconds {
        for (section in coordinatesForChartViewStyle(chartViewStyle)) {
            if (section.count() > 1) {
                return Seconds.secondsBetween(section[1].dateTime, section[0].dateTime)
            }
        }
        return Seconds.seconds(0)
    }
}

private fun translated(value: Float, min: Float, max: Float): Float {
    return (value - min) / (max - min)
}
