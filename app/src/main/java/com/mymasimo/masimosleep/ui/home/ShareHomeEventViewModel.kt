package com.mymasimo.masimosleep.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

class ShareHomeEventViewModel: ViewModel() {
    val shareEvent = MutableSharedFlow<HomeViewModel.SessionConfiguration.Summary>()
}