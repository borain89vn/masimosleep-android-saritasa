package com.mymasimo.masimosleep.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.repository.SessionTerminatedRepository
import com.mymasimo.masimosleep.databinding.FragmentSessionBinding
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import com.mymasimo.masimosleep.service.BLEConnectionState
import com.mymasimo.masimosleep.service.DeviceException
import com.mymasimo.masimosleep.service.DeviceExceptionHandler
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

class SessionFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    @Inject lateinit var schedulerProvider: SchedulerProvider
    @Inject lateinit var disposables: CompositeDisposable
    @Inject lateinit var deviceExceptionHandler: DeviceExceptionHandler
    @Inject lateinit var bleConnectionState: BLEConnectionState
    @Inject lateinit var sessionTerminatedRepository: SessionTerminatedRepository

    private val vm: SessionViewModel by viewModels { vmFactory }

    private lateinit var binding: FragmentSessionBinding
    private val args: SessionFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.titleTextView.text = "Night ${args.nightNumber} of ${NUM_OF_NIGHTS}"

        binding.addNoteButton.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.action_sessionFragment_to_addNoteFragment)
        }

        binding.endSessionBtn.setOnClickListener {
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
                sessionTerminatedRepository.findLatestTerminatedModelNotHandled()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .doOnError {
                        Timber.d("Session Ended")
                        requireView().findNavController().navigate(
                                R.id.surveyFragment,
                                SurveyFragmentArgs(sessionId, currentNight).toBundle())
                    }.toObservable()
            }
            .subscribe({ sessionTerminatedEntity ->
                           Timber.d("Session Ended with ${sessionTerminatedEntity.cause}")
                           sessionTerminatedEntity.cause?.let {
                               if (it == SessionTerminatedCause.NONE && sessionTerminatedEntity.sessionId != null && sessionTerminatedEntity.night != null) {
                                   Timber.d("Session Ended by user")
                                   requireView().findNavController().navigate(
                                           R.id.surveyFragment,
                                           SurveyFragmentArgs(sessionTerminatedEntity.sessionId, sessionTerminatedEntity.night).toBundle())
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
                DeviceException.DEFECTIVE_SENSOR   -> R.id.defectiveDialogFragment
                DeviceException.LOW_BATTERY        -> R.id.batteryLowDialogFragment
                else                               -> throw IllegalStateException()
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
                EVENTS_FRAGMENT_TAG
        )
    }
}