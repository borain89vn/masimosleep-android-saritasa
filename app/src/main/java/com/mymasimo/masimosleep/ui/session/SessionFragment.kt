package com.mymasimo.masimosleep.ui.session

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.repository.RawParameterReadingRepository
import com.mymasimo.masimosleep.data.repository.SessionTerminatedRepository
import com.mymasimo.masimosleep.databinding.FragmentSessionBinding
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import com.mymasimo.masimosleep.service.BLEConnectionState
import com.mymasimo.masimosleep.service.DeviceException
import com.mymasimo.masimosleep.service.DeviceExceptionHandler
import com.mymasimo.masimosleep.service.RawParameterReadingCsvExport
import com.mymasimo.masimosleep.ui.dialogs.SessionTerminatedFragmentArgs
import com.mymasimo.masimosleep.ui.session.no_data_yet.SessionNoDataFragment
import com.mymasimo.masimosleep.ui.session.session_events.SessionEventsFragment
import com.mymasimo.masimosleep.ui.session.session_sleep_quality.SessionSleepQualityFragment
import com.mymasimo.masimosleep.ui.session.session_time_in_bed.SessionTimeInBedFragment
import com.mymasimo.masimosleep.ui.session.sleep_quality_trend.SessionSleepQualityTrendFragment
import com.mymasimo.masimosleep.ui.session.view_vitals.SessionViewVitalsFragment
import com.mymasimo.masimosleep.ui.waking.survey.SurveyFragmentArgs
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject
import android.content.Intent

class SessionFragment : Fragment(R.layout.fragment_session) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var disposables: CompositeDisposable
    @Inject
    lateinit var deviceExceptionHandler: DeviceExceptionHandler
    @Inject
    lateinit var bleConnectionState: BLEConnectionState
    @Inject
    lateinit var sessionTerminatedRepository: SessionTerminatedRepository
    @Inject
    lateinit var rawParameterReadingRepository: RawParameterReadingRepository

    private val vm: SessionViewModel by viewModels { vmFactory }

    private val args: SessionFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentSessionBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.titleTextView.text = getString(R.string.night_label, args.nightNumber, NUM_OF_NIGHTS)

        viewBinding.addNoteButton.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.action_sessionFragment_to_addNoteFragment)
        }

        viewBinding.exportCsvBtn.setOnClickListener {
            exportMeasurements()
        }

        viewBinding.endSessionBtn.setOnClickListener {
            vm.onEndSessionClick()
        }

        vm.scoreAvailable.observe(viewLifecycleOwner) { scoreAvailable ->
            if (scoreAvailable) {
                showDataConfiguration()
            } else {
                showNoDataConfiguration()
            }
        }

    }

    override fun onResume() {
        super.onResume()

        vm.sessionInProgress
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { isSessionInProgress ->
                if (!isSessionInProgress) {
                    //Session ended or cancelled not by user
                    requireView().findNavController().navigate(R.id.homeFragment)
                }
            }
            .addTo(disposables)

        vm.sessionEnded
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .flatMap { (sessionId, currentNight) ->
                Timber.d("Session Ended, checking cause.")
                sessionTerminatedRepository.findLatestTerminatedModelNotHandled() // TODO MC: 3/15/21 simplify chain
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .doOnError {
                        Timber.d("Session Ended")
                        requireView().findNavController().navigate(
                            R.id.surveyFragment,
                            SurveyFragmentArgs(sessionId, currentNight).toBundle()
                        )
                    }.toObservable()
            }
            .subscribe({ sessionTerminatedEntity ->
                Timber.d("Session Ended with ${sessionTerminatedEntity.cause}")
                sessionTerminatedEntity.cause?.let {
                    if (it == SessionTerminatedCause.NONE && sessionTerminatedEntity.sessionId != null && sessionTerminatedEntity.night != null) {
                        Timber.d("Session Ended by user")
                        requireView().findNavController().navigate(
                            R.id.surveyFragment,
                            SurveyFragmentArgs(sessionTerminatedEntity.sessionId, sessionTerminatedEntity.night).toBundle()
                        )
                    } else {
                        requireView().findNavController().navigate(R.id.sessionTerminatedFragment, SessionTerminatedFragmentArgs(sessionTerminatedEntity).toBundle())
                    }
                }
            }, { it.printStackTrace() })
            .addTo(disposables)

        vm.sessionCanceled
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                // Navigate to Home fragment.
                requireView().findNavController().navigate(R.id.homeFragment)
            }
            .addTo(disposables)

        vm.showEndSessionConfirmation
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                requireView().findNavController().navigate(R.id.endSessionDialogFragment)
            }
            .addTo(disposables)

        vm.showCancelSessionConfirmation
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                requireView().findNavController().navigate(R.id.cancelSessionDialogFragment)
            }
            .addTo(disposables)

        vm.showSensorDialog.subscribe { deviceException ->
            val destinationId = when (deviceException) {
                DeviceException.SENSOR_OFF_PATIENT -> R.id.sensorDisconnectedDialogFragment
                DeviceException.DEFECTIVE_SENSOR -> R.id.defectiveDialogFragment
                DeviceException.LOW_BATTERY -> R.id.batteryLowDialogFragment
                else -> throw IllegalStateException()
            }
            requireView().findNavController().navigate(destinationId, null, NavOptions.Builder().setLaunchSingleTop(true).build())
        }.addTo(disposables)

        vm.showBleDialog.subscribe {
            requireView().findNavController().navigate(R.id.chipDisconnectedDialogFragment, null, NavOptions.Builder().setLaunchSingleTop(true).build())
        }.addTo(disposables)

        vm.checkBLEState()
        vm.checkSessionInProgressState()
    }

    override fun onPause() {
        disposables.clear()
        super.onPause()
    }

    /**
     * Export raw sensor reding data for the current session into CSV file.
     */
    private fun exportMeasurements() {
        val endAt = System.currentTimeMillis()

        rawParameterReadingRepository
            .getRawReadingCsvData(args.sessionStart, endAt)
            .subscribe { data ->
                if (data.isNullOrEmpty()) {
                    Toast.makeText(context!!, R.string.export_no_data, 5000).show()
                } else {
                    val resultUri = RawParameterReadingCsvExport.exportToDownloads(context!!, args.sessionStart, endAt, data)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = RawParameterReadingCsvExport.CSV_MIME_TYPE
                        putExtra(Intent.EXTRA_STREAM, resultUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Open export result"))
                }
            }
    }

    private fun showNoDataConfiguration() {
        removeAllFragments()

        addFragment(SessionNoDataFragment.newInstance(), NO_DATA_FRAGMENT_TAG)
        addFragment(SessionTimeInBedFragment.newInstance(args.sessionStart), TIME_IN_BED_FRAGMENT_TAG)
        addFragment(createViewVitalsFragment(), VIEW_VITALS_FRAGMENT_TAG)
    }

    private fun showDataConfiguration() {
        removeAllFragments()

        addFragment(SessionSleepQualityFragment.newInstance(args.sessionStart), SLEEP_QUALITY_FRAGMENT_TAG)
        addFragment(SessionTimeInBedFragment.newInstance(args.sessionStart), TIME_IN_BED_FRAGMENT_TAG)
        addFragment(SessionSleepQualityTrendFragment.newInstance(args.sessionStart), SLEEP_QUALITY_TREND_FRAGMENT_TAG)
        addFragment(SessionEventsFragment.newInstance(args.sessionStart), EVENTS_FRAGMENT_TAG)
        addFragment(createViewVitalsFragment(), VIEW_VITALS_FRAGMENT_TAG)
    }

    private fun createViewVitalsFragment(): SessionViewVitalsFragment {
        return SessionViewVitalsFragment.newInstance().apply {
            setOnClickListener {
                requireView().findNavController().navigate(
                    SessionFragmentDirections.actionSessionFragmentToSessionVitalsFragment(
                        args.sessionStart
                    )
                )
            }
        }
    }

    private fun removeAllFragments() {
        ALL_FRAGMENT_TAGS.forEach { tag ->
            childFragmentManager.findFragmentByTag(tag)?.let { fragment ->
                childFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        childFragmentManager.beginTransaction()
            .add(R.id.session_layout, fragment, tag)
            .commitAllowingStateLoss()
    }

    private fun NavController.popIfCurrentDestination(destinationId: Int) {
        val currentDestination = currentDestination ?: throw IllegalStateException()
        if (currentDestination.id == destinationId) {
            popBackStack()
        }
    }

    companion object {
        private const val NO_DATA_FRAGMENT_TAG = "NO_DATA"
        private const val TIME_IN_BED_FRAGMENT_TAG = "TIME_IN_BED"
        private const val VIEW_VITALS_FRAGMENT_TAG = "VIEW_VITALS"
        private const val SLEEP_QUALITY_FRAGMENT_TAG = "SLEEP_QUALITY"
        private const val SLEEP_QUALITY_TREND_FRAGMENT_TAG = "SLEEP_QUALITY_TREND"
        private const val EVENTS_FRAGMENT_TAG = "SLEEP_EVENTS"

        private val ALL_FRAGMENT_TAGS = listOf(
            NO_DATA_FRAGMENT_TAG,
            TIME_IN_BED_FRAGMENT_TAG,
            VIEW_VITALS_FRAGMENT_TAG,
            SLEEP_QUALITY_FRAGMENT_TAG,
            SLEEP_QUALITY_TREND_FRAGMENT_TAG,
            EVENTS_FRAGMENT_TAG,
        )
    }
}