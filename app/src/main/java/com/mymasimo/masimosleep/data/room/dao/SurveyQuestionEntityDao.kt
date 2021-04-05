package com.mymasimo.masimosleep.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestionEntity
import io.reactivex.Completable
import io.reactivex.Single
import com.mymasimo.masimosleep.data.room.entity.SurveyContract as Contract

@Dao
interface SurveyQuestionEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(surveyQuestion: SurveyQuestionEntity): Completable

    @Query("SELECT * FROM ${Contract.TABLE_NAME} WHERE ${Contract.COLUMN_SESSION_ID} = :sessionId")
    fun findAllBySessionId(sessionId: Long): Single<List<SurveyQuestionEntity>>
}
