package com.mymasimo.masimosleep.dagger.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mymasimo.masimosleep.base.viewmodel.ViewModelFactory
import com.mymasimo.masimosleep.base.viewmodel.ViewModelKey
import com.mymasimo.masimosleep.ui.dashboard.sleeping.SleepSessionViewModel
import com.mymasimo.masimosleep.ui.dialogs.SelfDismissDialogFragmentViewModel
import com.mymasimo.masimosleep.ui.home.HomeViewModel
import com.mymasimo.masimosleep.ui.night_report.notes.ReportNotesViewModel
import com.mymasimo.masimosleep.ui.night_report.notes.addnote.ReportAddNoteViewModel
import com.mymasimo.masimosleep.ui.night_report.recommendations.RecommendationsViewModel
import com.mymasimo.masimosleep.ui.night_report.report_bed_time.ReportTimeInBedViewModel
import com.mymasimo.masimosleep.ui.night_report.report_events.ReportEventsViewModel
import com.mymasimo.masimosleep.ui.night_report.report_events.details.EventDetailsViewModel
import com.mymasimo.masimosleep.ui.night_report.report_measurements.ReportMeasurementsViewModel
import com.mymasimo.masimosleep.ui.night_report.report_sleep_quality.ReportSleepQualityViewModel
import com.mymasimo.masimosleep.ui.night_report.report_sleep_trend.ReportSleepTrendViewModel
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph.ReportLineGraphViewModel
import com.mymasimo.masimosleep.ui.night_report.sleep_pattern.SleepPatternViewModel
import com.mymasimo.masimosleep.ui.pairing.PairingViewModel
import com.mymasimo.masimosleep.ui.program_completed.ProgramCompletedViewModel
import com.mymasimo.masimosleep.ui.program_history.ProgramHistoryViewModel
import com.mymasimo.masimosleep.ui.program_report.ProgramReportViewModel
import com.mymasimo.masimosleep.ui.program_report.avg_sleep_quality.AverageSleepQualityViewModel
import com.mymasimo.masimosleep.ui.program_report.events.ProgramEventsViewModel
import com.mymasimo.masimosleep.ui.program_report.nightly_scores.NightlyScoresViewModel
import com.mymasimo.masimosleep.ui.program_report.outcome.ProgramOutcomeViewModel
import com.mymasimo.masimosleep.ui.program_report.recommendations.ProgramRecommendationsViewModel
import com.mymasimo.masimosleep.ui.program_started.ProgramStartedViewModel
import com.mymasimo.masimosleep.ui.remove_chip.RemoveChipViewModel
import com.mymasimo.masimosleep.ui.session.SessionViewModel
import com.mymasimo.masimosleep.ui.session.addnote.AddNoteViewModel
import com.mymasimo.masimosleep.ui.session.session_events.SessionSleepEventsViewModel
import com.mymasimo.masimosleep.ui.session.session_measurements.SessionMeasurementsViewModel
import com.mymasimo.masimosleep.ui.session.session_sleep_quality.SessionSleepQualityViewModel
import com.mymasimo.masimosleep.ui.session.sleep_quality_trend.SleepQualityTrendViewModel
import com.mymasimo.masimosleep.ui.session.vitals.live.linegraph.LineGraphViewModel
import com.mymasimo.masimosleep.ui.settings.device.SettingsDeviceViewModel
import com.mymasimo.masimosleep.ui.waking.survey.SurveyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(PairingViewModel::class)
    internal abstract fun bindPairingViewModel(vm: PairingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SleepSessionViewModel::class)
    internal abstract fun bindSleepSessionViewModel(vm: SleepSessionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SessionViewModel::class)
    internal abstract fun bindSessionViewModel(vm: SessionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SessionSleepQualityViewModel::class)
    internal abstract fun bindSessionSleepQualityViewModel(vm: SessionSleepQualityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SessionSleepEventsViewModel::class)
    internal abstract fun bindSessionSleepEventsViewModel(vm: SessionSleepEventsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineGraphViewModel::class)
    internal abstract fun bindLineGraphViewModel(vm: LineGraphViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddNoteViewModel::class)
    internal abstract fun bindAddNoteViewModel(vm: AddNoteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SleepQualityTrendViewModel::class)
    internal abstract fun bindSleepQualityTrendViewModel(vm: SleepQualityTrendViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    internal abstract fun bindHomeViewModel(vm: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProgramStartedViewModel::class)
    internal abstract fun bindProgramStartedViewModel(vm: ProgramStartedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SurveyViewModel::class)
    internal abstract fun bindSurveyViewModel(vm: SurveyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportSleepQualityViewModel::class)
    internal abstract fun bindReportSleepQualityViewModel(vm: ReportSleepQualityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportTimeInBedViewModel::class)
    internal abstract fun bindReportTimeInBedViewModel(vm: ReportTimeInBedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportSleepTrendViewModel::class)
    internal abstract fun bindReportSleepTrendViewModel(vm: ReportSleepTrendViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportEventsViewModel::class)
    internal abstract fun bindReportEventsViewModel(vm: ReportEventsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EventDetailsViewModel::class)
    internal abstract fun bindEventDetailsViewModel(vm: EventDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportNotesViewModel::class)
    internal abstract fun bindReportNotesViewModel(vm: ReportNotesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportAddNoteViewModel::class)
    internal abstract fun bindReportAddNoteViewModel(vm: ReportAddNoteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportLineGraphViewModel::class)
    internal abstract fun bindReportLineGraphViewModel(vm: ReportLineGraphViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecommendationsViewModel::class)
    internal abstract fun bindRecommendationsViewModel(vm: RecommendationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SleepPatternViewModel::class)
    internal abstract fun bindSleepPatternViewModel(vm: SleepPatternViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProgramHistoryViewModel::class)
    internal abstract fun bindProgramHistoryViewModel(vm: ProgramHistoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProgramCompletedViewModel::class)
    internal abstract fun bindProgramCompletedViewModel(vm: ProgramCompletedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AverageSleepQualityViewModel::class)
    internal abstract fun bindAverageSleepQualityViewModel(vm: AverageSleepQualityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NightlyScoresViewModel::class)
    internal abstract fun bindNightlyScoresViewModel(vm: NightlyScoresViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProgramEventsViewModel::class)
    internal abstract fun bindProgramEventsViewModel(vm: ProgramEventsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProgramRecommendationsViewModel::class)
    internal abstract fun bindProgramRecommendationsViewModel(vm: ProgramRecommendationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProgramReportViewModel::class)
    internal abstract fun bindProgramReportViewModel(vm: ProgramReportViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RemoveChipViewModel::class)
    internal abstract fun bindRemoveChipViewModel(vm: RemoveChipViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProgramOutcomeViewModel::class)
    internal abstract fun bindProgramOutcomeViewModel(vm: ProgramOutcomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsDeviceViewModel::class)
    internal abstract fun bindSettingsDeviceViewModel(vm: SettingsDeviceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelfDismissDialogFragmentViewModel::class)
    internal abstract fun bindSelfDismissDialogFragmentViewModel(vm: SelfDismissDialogFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportMeasurementsViewModel::class)
    internal abstract fun bindReportMeasurementsViewModel(vm: ReportMeasurementsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SessionMeasurementsViewModel::class)
    internal abstract fun bindSessionMeasurementsViewModel(vm: SessionMeasurementsViewModel): ViewModel
}
