package com.mymasimo.masimosleep.ui.session.addnote

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_add_note.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class AddNoteFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: AddNoteViewModel by viewModels { vmFactory }


    companion object {
        private const val TOTAL_CHARS: Int = 45
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_add_note, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.onNoteSaved
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                noteSaved()
            }
            .addTo(disposables)

        loadViewContent()
    }

    private fun loadViewContent() {
        cancel_button.setOnClickListener {
            dismiss()
        }

        add_button.setOnClickListener {
            addNote()
        }

        val dateFormatter = SimpleDateFormat("MMM d, hh:mm aa")
        val dateString = dateFormatter.format(Date(Calendar.getInstance().timeInMillis))

        date_text.text = dateString

        note_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateRemainingCharsLabel()
            }

        })

        updateRemainingCharsLabel()
    }

    private fun updateRemainingCharsLabel() {
        var charsRemaining = TOTAL_CHARS - note_text.text.count()
        if (charsRemaining < 0) {
            charsRemaining = 0
        }

        chars_remaining_text.text = getString(R.string.hint_characters_left, charsRemaining, TOTAL_CHARS)
    }

    private fun addNote() {
        add_button.isEnabled = false
        note_text.isEnabled = false
        vm.onAddButtonClick(note_text.text.toString())
    }

    private fun noteSaved() {
        dismiss()
    }

    private fun dismiss() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigateUp()
    }
}