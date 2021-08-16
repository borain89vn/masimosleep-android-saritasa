package com.mymasimo.masimosleep.ui.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.repository.SessionTerminatedRepository
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.databinding.FragmentHomeBinding
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import com.mymasimo.masimosleep.ui.dialogs.SessionTerminatedFragmentArgs
import com.mymasimo.masimosleep.ui.night_report.NightReportFragment
import com.mymasimo.masimosleep.ui.session.SessionFragmentArgs
import com.mymasimo.masimosleep.util.navigateSafe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class HomeFragment : Fragment(R.layout.fragment_home) {

    @Inject
    lateinit var sleepSessionScoreManager: SleepSessionScoreManager

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var sessionTerminatedRepository: SessionTerminatedRepository

    private val vm: HomeViewModel by activityViewModels { vmFactory }
    private val args: HomeFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentHomeBinding::bind)

    private var defaultSessionIdToOpen = -1L

    private var isViewFullAnalysisClick = false
    private var selectedNight = 1

    private val sessionConfigurationObserver = Observer<HomeViewModel.SessionConfiguration> { configuration ->
        //Reset when night is selected so we don't automatically navigate to last ended session/night
        defaultSessionIdToOpen = -1L

        when (configuration) {
            HomeViewModel.SessionConfiguration.Today -> {
                if (!sleepSessionScoreManager.isSessionInProgress)
                    showTodayConfiguration()
            }
            is HomeViewModel.SessionConfiguration.Summary -> {
                if (!sleepSessionScoreManager.isSessionInProgress)
                    showSummaryConfiguration(configuration)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        defaultSessionIdToOpen = args.defaultSessionId
        receiveViewSummaryClickEvent()
    }

    override fun onPause() {
        super.onPause()
        vm.sessionConfiguration.removeObserver(sessionConfigurationObserver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.setDefaultSessionIdToOpen(defaultSessionIdToOpen)

        vm.goToDeviceSetupFlow.subscribe {
            findNavController().navigate(R.id.deviceOnboardingViewPagerFragment)
        }.addTo(disposables)

        vm.showSetupDeviceDialog.subscribe {
            findNavController().navigate(R.id.action_homeFragment_to_setUpDeviceDialogFragment)
        }.addTo(disposables)

        vm.onViewCreated()

        drawCircleBG()

        viewBinding.settingsButton.setOnClickListener {
            requireView().findNavController().navigateSafe(R.id.action_homeFragment_to_settingsFragment)
        }

        viewBinding.calendarButton.setOnClickListener {
            requireView().findNavController().navigateSafe(R.id.action_homeFragment_to_programHistoryFragment)
        }

        vm.homeTitle.observe(viewLifecycleOwner){
            viewBinding.titleTextView.text = it
        }
    }

    override fun onResume() {
        super.onResume()
        sessionTerminatedRepository.findLatestTerminatedModelNotHandled()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    if (it.cause == SessionTerminatedCause.NONE) {
                        Timber.d("Session Ended by user")
                    } else {
                        requireView().findNavController().navigate(R.id.sessionTerminatedFragment, SessionTerminatedFragmentArgs(it).toBundle())
                    }
                },
                {
                    Timber.d("No Session terminated")
                }).addTo(disposables)

        vm.sessionConfiguration.observe(viewLifecycleOwner, sessionConfigurationObserver)

        vm.sessionEnded
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { (sessionId, currentNight) -> }
            .addTo(disposables)

        vm.sessionCanceled
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
            }
            .addTo(disposables)

        vm.sessionInProgress
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ sessionEntity ->
                requireView().findNavController().navigate(
                    R.id.sessionFragment,
                    SessionFragmentArgs(sessionEntity.startAt, sessionEntity.nightNumber).toBundle(),
                    NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setLaunchSingleTop(true)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .setPopUpTo(R.id.homeFragment, true)
                        .build()
                )
            }, {
                it.printStackTrace()
            })
            .addTo(disposables)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun drawCircleBG() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val density = resources.displayMetrics.density

        val bitmap: Bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = Color.parseColor("#f7fafb")
        paint.isAntiAlias = true
        paint.isDither = true

        val nightPickerHeight = (164 * density)
        val yOffset: Float = nightPickerHeight

        val centerX = (screenWidth / 2).toFloat()
        val centerY = (screenHeight / 2).toFloat() + yOffset
        val radius = ((screenHeight) / 2).toFloat()

        canvas.drawCircle(centerX, centerY, radius, paint)

        viewBinding.circleBg.background = BitmapDrawable(resources, bitmap)
    }

    private fun showTodayConfiguration() {
        hideViewFullMode(-1)
        removeAllFragments()
        addFragment(StartButtonFragment.newInstance(), START_SESSION_FRAGMENT_TAG)
    }

    private fun showSummaryConfiguration(configuration: HomeViewModel.SessionConfiguration.Summary) {
        if (isViewFullMode(configuration)) return
        removeAllFragments()
        addFragment(
            NightSummaryFragment.newInstance(
                configuration.sessionId,
                configuration.nightNumber
            ),
            SESSION_SUMMARY_FRAGMENT_TAG
        )
    }

    private fun receiveViewSummaryClickEvent(){
       parentFragmentManager?.setFragmentResultListener(KEY_REQUEST_VIEW_SUMMARY, this) { _, result->
           isViewFullAnalysisClick = true
           val sessionId = result.getLong(KEY_SESSION_ID)
           val nightNumber = result.getInt(KEY_NIGHT_NUMBER)
           showNightReport(sessionId, nightNumber)
        }
    }
    private  fun showNightReport(sessionId: Long, nightNumber: Int){
        removeAllFragments()
        addFragment(
            NightReportFragment.newInstance(
                sessionId,
                nightNumber
            ),
            NIGHT_REPORT_FRAGMENT_TAG
        )
    }
    private fun isViewFullMode(configuration: HomeViewModel.SessionConfiguration.Summary): Boolean {
        val sessionId = configuration.sessionId
        val nightNumber = configuration.nightNumber

        if (isViewFullAnalysisClick && selectedNight == nightNumber) {
            showNightReport(sessionId,nightNumber)
            return true
        }
        hideViewFullMode(nightNumber)
        return false
    }
    private fun hideViewFullMode(nightNumber: Int){
        isViewFullAnalysisClick = false
        selectedNight = nightNumber
    }


    private fun removeAllFragments() {
        ALL_FRAGMENT_TAGS.forEach { tag ->
            parentFragmentManager.findFragmentByTag(tag)?.let { fragment ->
                parentFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        parentFragmentManager.beginTransaction()
            .add(R.id.home_linear_layout, fragment, tag)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val START_SESSION_FRAGMENT_TAG = "START_SESSION"
        private const val SESSION_SUMMARY_FRAGMENT_TAG = "SESSION_SUMMARY"
        private const val NIGHT_REPORT_FRAGMENT_TAG = "SESSION_SUMMARY"
        private const val KEY_SESSION_ID = "SESSION_ID"
        private const val KEY_NIGHT_NUMBER = "NIGHT_NUMBER"
        private const val KEY_REQUEST_VIEW_SUMMARY = "REQUEST_VIEW_SUMMARY"

        private val ALL_FRAGMENT_TAGS = listOf(
            START_SESSION_FRAGMENT_TAG,
            SESSION_SUMMARY_FRAGMENT_TAG
        )
    }
}