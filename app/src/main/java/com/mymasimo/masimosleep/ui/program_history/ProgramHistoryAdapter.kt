package com.mymasimo.masimosleep.ui.program_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.databinding.ProgramRowBinding
import com.mymasimo.masimosleep.ui.program_history.util.Program
import java.text.SimpleDateFormat
import java.util.*

class ProgramHistoryAdapter(
        private val programs: MutableList<Program>,
        private val onProgramClickListener: (Program) -> Unit
) : RecyclerView.Adapter<ProgramViewHolder>() {

    override fun getItemCount(): Int = programs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        val bindings = ProgramRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgramViewHolder(bindings, onProgramClickListener)
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        holder.bindData(programs[position])
    }

    fun setPrograms(programs: List<Program>) {
        this.programs.clear()
        this.programs.addAll(programs)
        notifyDataSetChanged()
    }

}

class ProgramViewHolder(
        private val bindings: ProgramRowBinding,
        private val onProgramClickListener: (Program) -> Unit
) : RecyclerView.ViewHolder(bindings.root) {

    fun bindData(program: Program) {
        bindings.root.setOnClickListener {
            onProgramClickListener(program)
        }

        with(bindings.root.resources) {
            bindings.subTitleText.text = if (program.sessionCount <= 0) getString(R.string.no_nights_recorded) else getString(R.string.num_of_nights_recorded, program.sessionCount,
                                                                                                                              getQuantityText(R.plurals.nights_recorded,
                                                                                                                                              program.sessionCount))
        }

        val score = program.score

        val scoreRounded: Int = (score * 100).toInt()

        bindings.lblScoreText.text = scoreRounded.toString()

        var faceImageID: Int = R.drawable.face_red
        when {
            scoreRounded < bindings.root.resources.getInteger(R.integer.red_upper)     -> {
                faceImageID = R.drawable.face_red
            }
            scoreRounded <= bindings.root.resources.getInteger(R.integer.yellow_upper) -> {
                faceImageID = R.drawable.face_yellow
            }
            scoreRounded > bindings.root.resources.getInteger(R.integer.yellow_upper)  -> {
                faceImageID = R.drawable.face_green
            }
        }

        when (program) {
            is Program.Current -> {
                with(bindings.root.resources) {
                    bindings.titleText.text = getString(R.string.current)

                    //Display score at ${NUM_OF_NIGHTS - 1}th night
                    if (program.sessionCount < NUM_OF_NIGHTS - 1) {
                        faceImageID = R.drawable.face_yellow
                        bindings.lblScoreText.text = getString(R.string.double_dash)
                    }
                }
            }
            is Program.Past    -> {
                val startFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
                val startTimeCalendar = Calendar.getInstance().apply {
                    timeInMillis = program.startAt
                }

                val startString = startFormatter.format(startTimeCalendar.time)

                val endFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val endTimeCalendar = Calendar.getInstance().apply {
                    timeInMillis = program.endAt
                }

                val endString = endFormatter.format(endTimeCalendar.time)

                bindings.titleText.text = "$startString - $endString"
            }
        }

        bindings.faceImage.setImageDrawable(bindings.root.resources.getDrawable(faceImageID, null))
    }
}