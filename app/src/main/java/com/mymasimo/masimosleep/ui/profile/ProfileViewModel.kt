package com.mymasimo.masimosleep.ui.profile


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import timber.log.Timber
import java.util.*


class ProfileViewModel : ViewModel() {

    val profileName: MutableLiveData<String> = MutableLiveData<String>()

    val gender: MutableLiveData<String> = MutableLiveData<String>()

    var birthdate: MutableLiveData<Calendar> = MutableLiveData<Calendar>()

    var hasCondition: Boolean = false

    var conditionList: MutableLiveData<ArrayList<String>> = MutableLiveData<ArrayList<String>>()

    var timeHour: MutableLiveData<Int> = MutableLiveData<Int>()
    var timeMinute: MutableLiveData<Int> = MutableLiveData<Int>()

    var reminderDuration: MutableLiveData<Int> = MutableLiveData<Int>()

    fun printValues() {

        MasimoSleepPreferences.name = profileName.value


        Timber.d("Name: " + profileName.value)
        Timber.d("Gender: " + gender.value)
        birthdate.value?.let {bd ->
            val month = bd.get(Calendar.MONTH) + 1
            val day = bd.get(Calendar.DAY_OF_MONTH)
            val year = bd.get(Calendar.YEAR)
            Timber.d("Birthdate: $month/$day/$year")
        }


        Timber.d("Conditions: " + conditionList.value)

        val hour = timeHour.value
        val minute = timeMinute.value
        Timber.d("Bedtime: $hour:$minute")

        val reminder = reminderDuration.value
        Timber.d("Reminder duration: $reminder")
    }
}