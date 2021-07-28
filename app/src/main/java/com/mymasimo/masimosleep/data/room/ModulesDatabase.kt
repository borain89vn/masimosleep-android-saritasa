package com.mymasimo.masimosleep.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mymasimo.masimosleep.data.room.dao.*
import com.mymasimo.masimosleep.data.room.entity.*
import com.mymasimo.masimosleep.data.room.typeConverter.RoomConverters
import timber.log.Timber

/**
 * The Modules database that contains module data and its readings.
 */
@Database(
    version = 10,
    entities = [
        Module::class,
        ParameterReadingEntity::class,
        ProgramEntity::class,
        SessionEntity::class,
        SessionNoteEntity::class,
        ScoreEntity::class,
        SleepEventEntity::class,
        SurveyQuestionEntity::class,
        SessionTerminatedEntity::class,
        RawParameterReadingEntity::class,
    ]
)
@TypeConverters(RoomConverters::class)
abstract class ModulesDatabase : RoomDatabase() {
    abstract fun moduleDao(): ModuleDao
    abstract fun parameterReadingEntityDao(): ParameterReadingEntityDao
    abstract fun rawParameterReadingEntityDao(): RawParameterReadingEntityDao
    abstract fun scoreEntityDao(): ScoreEntityDao
    abstract fun sessionEntityDao(): SessionEntityDao
    abstract fun sessionNoteEntityDao(): SessionNoteEntityDao
    abstract fun sleepEventEntityDao(): SleepEventEntityDao
    abstract fun surveyQuestionEntityDao(): SurveyQuestionEntityDao
    abstract fun programEntityDao(): ProgramEntityDao
    abstract fun sessionTerminatedEntityDao(): SessionTerminatedEntityDao

    companion object {
        private const val DB_FILE_NAME = "zeek.masimo.sleep.db"

        @Volatile
        private var INSTANCE: ModulesDatabase? = null

        fun getInstance(context: Context): ModulesDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): ModulesDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ModulesDatabase::class.java,
                DB_FILE_NAME
            )
                .addMigrations(
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Timber.d("Database created")
                        addTriggerToDeleteModulesOfTheSameType(db)
                    }
                })
                .build()
        }

        /**
         * Adds a trigger to the database that when a new module is inserted, it deletes any other
         * modules already in the db that are of the same type as the newly inserted one.
         */
        private fun addTriggerToDeleteModulesOfTheSameType(db: SupportSQLiteDatabase) {
            val triggerSQL = "CREATE TRIGGER IF NOT EXISTS ${ModuleContract.TABLE_NAME}_one_of_each_type " +
                    "AFTER INSERT ON ${ModuleContract.TABLE_NAME} " +
                    "BEGIN\n" +
                    "DELETE FROM ${ModuleContract.TABLE_NAME} " +
                    "WHERE ${ModuleContract.ID} <> new.${ModuleContract.ID} " +
                    "AND ${ModuleContract.MODULE_TYPE} = new.${ModuleContract.MODULE_TYPE}" +
                    ";\nEND"
            db.execSQL(triggerSQL)
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ${ProgramContract.TABLE_NAME} ADD COLUMN ${ProgramContract.COLUMN_SCORE} REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `session_terminated` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `sessionId` INTEGER, `night` INTEGER, `cause` TEXT, `handled` INTEGER NOT NULL, `recorded` INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ${ModuleContract.TABLE_NAME} ADD COLUMN ${ModuleContract.IS_CURRENT} INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ${RawParameterReadingContract.TABLE_NAME} " +
                            "(${RawParameterReadingContract.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "${RawParameterReadingContract.COLUMN_TYPE} TEXT NOT NULL, " +
                            "${RawParameterReadingContract.COLUMN_VALUE} REAL NOT NULL, " +
                            "${RawParameterReadingContract.COLUMN_CREATED_AT} INTEGER NOT NULL)"
                )
            }
        }
    }
}
