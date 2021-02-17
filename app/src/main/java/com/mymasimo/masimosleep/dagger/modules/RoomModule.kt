package com.mymasimo.masimosleep.dagger.modules

import android.content.Context
import com.mymasimo.masimosleep.data.room.ModulesDatabase
import com.mymasimo.masimosleep.data.room.dao.*
import dagger.Module
import dagger.Provides

@Module
class RoomModule {
    @Provides
    fun provideParameterReadingEntityDao(context: Context): ParameterReadingEntityDao {
        return ModulesDatabase.getInstance(context).parameterReadingEntityDao()
    }

    @Provides
    fun provideScoreEntityDao(context: Context): ScoreEntityDao {
        return ModulesDatabase.getInstance(context).scoreEntityDao()
    }

    @Provides
    fun provideSessionEntityDao(context: Context): SessionEntityDao {
        return ModulesDatabase.getInstance(context).sessionEntityDao()
    }

    @Provides
    fun provideSessionNoteEntityDao(context: Context): SessionNoteEntityDao {
        return ModulesDatabase.getInstance(context).sessionNoteEntityDao()
    }

    @Provides
    fun provideSleepEventEntityDao(context: Context): SleepEventEntityDao {
        return ModulesDatabase.getInstance(context).sleepEventEntityDao()
    }

    @Provides
    fun provideSurveyQuestionEntityDao(context: Context): SurveyQuestionEntityDao {
        return ModulesDatabase.getInstance(context).surveyQuestionEntityDao()
    }

    @Provides
    fun provideProgramEntityDao(context: Context): ProgramEntityDao {
        return ModulesDatabase.getInstance(context).programEntityDao()
    }

    @Provides
    fun provideSessionTerminatedEntityDao(context: Context): SessionTerminatedEntityDao {
        return ModulesDatabase.getInstance(context).sessionTerminatedEntityDao()
    }
}
