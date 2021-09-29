package com.mymasimo.masimosleep.dagger

import com.mymasimo.masimosleep.MasimoSleepApp
import com.mymasimo.masimosleep.alarm.MasimoBootReceiver
import com.mymasimo.masimosleep.dagger.modules.*
import com.mymasimo.masimosleep.service.MasimoSleepCommunicationService
import com.mymasimo.masimosleep.ui.dashboard.sleeping.nightsession.NightSessionFragment
import com.mymasimo.masimosleep.ui.dashboard.sleeping.program.SleepingProgramFragment
import com.mymasimo.masimosleep.ui.dialogs.*
import com.mymasimo.masimosleep.ui.home.HomeFragment
import com.mymasimo.masimosleep.ui.home.StartButtonFragment
import com.mymasimo.masimosleep.ui.home.night_picker.NightPickerFragment
import com.mymasimo.masimosleep.ui.night_report.NightReportFragment
import com.mymasimo.masimosleep.ui.night_report.notes.ReportNotesFragment
import com.mymasimo.masimosleep.ui.night_report.notes.addnote.ReportAddNoteFragment
import com.mymasimo.masimosleep.ui.night_report.recommendations.RecommendationsFragment
import com.mymasimo.masimosleep.ui.night_report.report_bed_time.ReportTimeInBedFragment
import com.mymasimo.masimosleep.ui.night_report.report_events.ReportEventsFragment
import com.mymasimo.masimosleep.ui.night_report.report_events.details.EventDetailsFragment
import com.mymasimo.masimosleep.ui.night_report.report_measurements.ReportMeasurementsFragment
import com.mymasimo.masimosleep.ui.night_report.report_sleep_quality.ReportSleepQualityFragment
import com.mymasimo.masimosleep.ui.night_report.report_sleep_trend.ReportSleepTrendFragment
import com.mymasimo.masimosleep.ui.night_report.report_vitals.ReportVitalsFragment
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph.ReportLineGraphFragment
import com.mymasimo.masimosleep.ui.night_report.sleep_pattern.SleepPatternFragment
import com.mymasimo.masimosleep.ui.pairing.pair.SelectDeviceFragment
import com.mymasimo.masimosleep.ui.pairing.scan.ScanFragment
import com.mymasimo.masimosleep.ui.profile.screens.ProfileBedtimeFragment
import com.mymasimo.masimosleep.ui.profile.screens.ProfileReminderFragment
import com.mymasimo.masimosleep.ui.program_completed.ProgramCompletedFragment
import com.mymasimo.masimosleep.ui.program_history.ProgramHistoryFragment
import com.mymasimo.masimosleep.ui.program_report.ProgramReportFragment
import com.mymasimo.masimosleep.ui.program_report.avg_sleep_quality.AverageSleepQualityFragment
import com.mymasimo.masimosleep.ui.program_report.events.ProgramEventsFragment
import com.mymasimo.masimosleep.ui.program_report.nightly_scores.NightlyScoresFragment
import com.mymasimo.masimosleep.ui.program_report.outcome.ProgramOutcomeFragment
import com.mymasimo.masimosleep.ui.program_report.recommendations.ProgramRecommendationsFragment
import com.mymasimo.masimosleep.ui.program_started.ProgramStartedFragment
import com.mymasimo.masimosleep.ui.remove_chip.RemoveChipFragment
import com.mymasimo.masimosleep.ui.session.SessionFragment
import com.mymasimo.masimosleep.ui.session.addnote.AddNoteFragment
import com.mymasimo.masimosleep.ui.session.export_measurements.SessionExportMeasurementsFragment
import com.mymasimo.masimosleep.ui.session.session_event_detail.SessionEventDetailsFragment
import com.mymasimo.masimosleep.ui.session.session_events.SessionEventsFragment
import com.mymasimo.masimosleep.ui.session.session_measurements.SessionMeasurementsFragment
import com.mymasimo.masimosleep.ui.session.session_sleep_quality.SessionSleepQualityFragment
import com.mymasimo.masimosleep.ui.session.session_time_in_bed.SessionTimeInBedFragment
import com.mymasimo.masimosleep.ui.session.sleep_quality_trend.SessionSleepQualityTrendFragment
import com.mymasimo.masimosleep.ui.session.vitals.SessionVitalsFragment
import com.mymasimo.masimosleep.ui.session.vitals.live.linegraph.LiveLineGraphFragment
import com.mymasimo.masimosleep.ui.settings.device.SettingsDeviceFragment
import com.mymasimo.masimosleep.ui.settings.sensor.SettingsSensorFragment
import com.mymasimo.masimosleep.ui.waking.survey.SurveyFragment
import com.mymasimo.masimosleep.ui.welcome.SplashFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ContextModule::class,
        RoomModule::class,
        RxJavaUtilsModule::class,
        ViewModelModule::class,
        CoroutineDispatchersModule::class,
    ]
)
interface SingletonComponent {
    fun inject(target: SplashFragment)
    fun inject(target: MasimoBootReceiver)
    fun inject(target: MasimoSleepApp)
    fun inject(target: SelectDeviceFragment)
    fun inject(target: MasimoSleepCommunicationService)
    fun inject(target: SleepingProgramFragment)
    fun inject(target: NightSessionFragment)
    fun inject(target: ScanFragment)
    fun inject(target: SessionSleepQualityFragment)
    fun inject(target: SessionFragment)
    fun inject(target: SessionTimeInBedFragment)
    fun inject(target: LiveLineGraphFragment)
    fun inject(target: HomeFragment)
    fun inject(target: AddNoteFragment)
    fun inject(target: SessionSleepQualityTrendFragment)
    fun inject(target: SessionEventsFragment)
    fun inject(target: StartButtonFragment)
    fun inject(target: NightPickerFragment)
    fun inject(target: ProgramStartedFragment)
    fun inject(target: ReportSleepQualityFragment)
    fun inject(target: ReportTimeInBedFragment)
    fun inject(target: ReportSleepTrendFragment)
    fun inject(target: ReportEventsFragment)
    fun inject(target: EventDetailsFragment)
    fun inject(target: ReportNotesFragment)
    fun inject(target: ReportAddNoteFragment)
    fun inject(target: ReportLineGraphFragment)
    fun inject(target: RecommendationsFragment)
    fun inject(target: SleepPatternFragment)
    fun inject(target: BatteryLowDialogFragment)
    fun inject(target: SensorDisconnectedDialogFragment)
    fun inject(target: CancelSessionDialogFragment)
    fun inject(target: EndSessionDialogFragment)
    fun inject(target: ChipDisconnectedDialogFragment)
    fun inject(target: SetupDeviceDialogFragment)
    fun inject(target: DefectiveDialogFragment)
    fun inject(target: ProgramHistoryFragment)
    fun inject(target: ProgramCompletedFragment)
    fun inject(target: AverageSleepQualityFragment)
    fun inject(target: NightlyScoresFragment)
    fun inject(target: ProgramEventsFragment)
    fun inject(target: ProgramRecommendationsFragment)
    fun inject(target: EndProgramDialogFragment)
    fun inject(target: ProgramReportFragment)
    fun inject(target: RemoveChipFragment)
    fun inject(target: SettingsSensorFragment)
    fun inject(target: ProfileBedtimeFragment)
    fun inject(target: ProfileReminderFragment)
    fun inject(target: SurveyFragment)
    fun inject(target: ProgramOutcomeFragment)
    fun inject(target: ConfirmReplaceSensorDialogFragment)
    fun inject(target: SettingsDeviceFragment)
    fun inject(target: SessionTerminatedFragment)
    fun inject(target: SelfDismissDialogFragment)
    fun inject(target: SessionExportMeasurementsFragment)
    fun inject(target: NightReportFragment)
    fun inject(target: ReportMeasurementsFragment)
    fun inject(target: SessionMeasurementsFragment)
    fun inject(target: SessionEventDetailsFragment)
    fun inject(target: ReportVitalsFragment)
    fun inject(target: SessionVitalsFragment)
}
