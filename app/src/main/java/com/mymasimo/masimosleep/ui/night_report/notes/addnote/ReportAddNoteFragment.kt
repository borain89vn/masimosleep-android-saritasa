package com.mymasimo.masimosleep.ui.night_report.notes.addnote

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentReportAddNoteBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReportAddNoteFragment : Fragment(R.layout.fragment_report_add_note) {

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportAddNoteViewModel by viewModels { vmFactory }

    private val args: ReportAddNoteFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentReportAddNoteBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()

        vm.onNoteSaved
            .subscribe { dismiss() }
            .addTo(disposables)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun loadViewContent() {
        viewBinding.cancelButton.setOnClickListener {
            dismiss()
        }

        viewBinding.addButton.setOnClickListener {
            addNote()
        }

        val dateFormatter = SimpleDateFormat("MMM d, hh:mm aa")
        val dateString = dateFormatter.format(Date(Calendar.getInstance().timeInMillis))

        viewBinding.dateText.text = dateString

        viewBinding.noteText.addTextChangedListener(object : TextWatcher {

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
        var charsRemaining = TOTAL_CHARS - viewBinding.noteText.text.count()
        if (charsRemaining < 0) {
            charsRemaining = 0
        }

        viewBinding.charsRemainingText.text = getString(R.string.hint_characters_left, charsRemaining, TOTAL_CHARS)
    }

    private fun addNote() {
        viewBinding.addButton.isEnabled = false
        viewBinding.noteText.isEnabled = false

        vm.onAddButtonClick(args.sessionId, viewBinding.noteText.text.toString())
    }

    private fun dismiss() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigateUp()
    }

    companion object {
        private const val TOTAL_CHARS: Int = 45
    }
}