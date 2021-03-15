package com.mymasimo.masimosleep.ui.night_report.report_events.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.room.entity.SleepEventType
import com.mymasimo.masimosleep.databinding.EventRowBinding
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

val dateFormat = SimpleDateFormat("hh:mm a")

class EventDetailsAdapter(
    private val events: MutableList<EventDetailsViewModel.EventDetailViewData.EventSummary>
) : RecyclerView.Adapter<EventViewHolder>() {

    override fun getItemCount(): Int = events.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val bindings = EventRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(bindings)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bindData(events[position])
    }

    fun setEvents(events: List<EventDetailsViewModel.EventDetailViewData.EventSummary>) {
        this.events.clear()
        this.events.addAll(events)
        notifyDataSetChanged()
    }
}

class EventViewHolder(
    private val bindings: EventRowBinding
) : RecyclerView.ViewHolder(bindings.root) {

    fun bindData(event: EventDetailsViewModel.EventDetailViewData.EventSummary) {
        bindings.numberLabel.text = event.index.toString()

        val formattedTime = dateFormat.format(event.startTime)

        bindings.timeLabel.text = formattedTime

        bindings.durationLabel.text = String.format(
            "%dm %ds",
            TimeUnit.MILLISECONDS.toMinutes(event.duration),
            TimeUnit.MILLISECONDS.toSeconds(event.duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(event.duration))
        )

        var severityDotID: Int = R.drawable.minor_event_dot
        var severityStringID: Int = R.string.minor_event_name
        if (event.severity == SleepEventType.SEVERE) {
            severityDotID = R.drawable.major_event_dot
            severityStringID = R.string.major_event_name
        }

        bindings.severityDot.background = ResourcesCompat.getDrawable(bindings.root.resources, severityDotID, null)
        bindings.severityLabel.text = bindings.root.resources.getString(severityStringID)

        var bgColorID = R.color.white
        if (adapterPosition % 2 == 0) {
            bgColorID = R.color.lightGray
        }

        bindings.root.background = ResourcesCompat.getDrawable(bindings.root.resources, bgColorID, null)
    }
}