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
import androidx.fragment.app.setFragmentResultListener
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
    private var nightReportScrollPosition = 0
    private var selectedNight = -1

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
                    showNightReport(configuration)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        defaultSessionIdToOpen = args.defaultSessionId
        receiveScrollPositionEvent()
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
        removeAllFragments()
        addFragment(StartButtonFragment.newInstance(), START_SESSION_FRAGMENT_TAG)
    }

    private fun showNightReport(configuration: HomeViewModel.SessionConfiguration.Summary) {
        removeAllFragments()
        if (selectedNight != configuration.nightNumber) {
            nightReportScrollPosition = 0
            selectedNight = configuration.nightNumber
        }
        addFragment(
            NightReportFragment.newInstance(
                configuration.sessionId,
                configuration.nightNumber,
                nightReportScrollPosition
            ),
            NIGHT_REPORT_FRAGMENT_TAG
        )
    }

    private fun receiveScrollPositionEvent() {
        parentFragment?.setFragmentResultListener(KEY_SCROLL_POSITION_REQUEST) { _, result ->
            nightReportScrollPosition = result.getInt(KEY_SCROLL_POSITION)
        }
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
        private const val KEY_SCROLL_POSITION_REQUEST = "SCROLL_REQUEST"
        private const val KEY_SCROLL_POSITION = "SCROLL_POSITION"

        private val ALL_FRAGMENT_TAGS = listOf(
            START_SESSION_FRAGMENT_TAG,
            SESSION_SUMMARY_FRAGMENT_TAG
        )
    }
}