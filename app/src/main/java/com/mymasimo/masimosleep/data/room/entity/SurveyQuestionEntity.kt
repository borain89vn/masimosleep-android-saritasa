package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.SurveyContract.COLUMN_ANSWER
import com.mymasimo.masimosleep.data.room.entity.SurveyContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.SurveyContract.COLUMN_QUESTION
import com.mymasimo.masimosleep.data.room.entity.SurveyContract.COLUMN_SESSION_ID
import com.mymasimo.masimosleep.data.room.entity.SurveyContract.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class SurveyQuestionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
    @ColumnInfo(name = COLUMN_QUESTION) val question: SurveyQuestion,
    @ColumnInfo(name = COLUMN_ANSWER) val answer: SurveyAnswer,
    @ColumnInfo(name = COLUMN_SESSION_ID) val sessionId: Long
)

object SurveyContract {
    const val TABLE_NAME = "survey_questions"

    const val COLUMN_ID = "id"
    const val COLUMN_SESSION_ID = "sessionId"
    const val COLUMN_QUESTION = "question"
    const val COLUMN_ANSWER = "answer"
}
