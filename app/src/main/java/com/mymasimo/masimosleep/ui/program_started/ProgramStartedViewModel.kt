package com.mymasimo.masimosleep.ui.program_started

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgramStartedViewModel @Inject constructor(
    programRepository: ProgramRepository,
) : ViewModel() {

    private val _goToDashboardEnabled = MutableLiveData(false)
    val goToDashboardEnabled: LiveData<Boolean>
        get() = _goToDashboardEnabled

    init {
        viewModelScope.launch {
            programRepository.createProgram()
            _goToDashboardEnabled.value = true
        }
    }
}