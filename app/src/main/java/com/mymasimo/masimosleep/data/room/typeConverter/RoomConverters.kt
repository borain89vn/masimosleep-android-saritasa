package com.mymasimo.masimosleep.data.room.typeConverter

import android.net.Uri
import androidx.room.TypeConverter
import com.masimo.android.airlib.ProductType
import com.masimo.android.airlib.ProductVariant
import com.masimo.common.model.universal.ParameterID
import com.mymasimo.masimosleep.data.room.entity.*
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import com.mymasimo.masimosleep.model.SessionType
import java.util.*

private const val COLLECTION_DELIMITER = ","

object RoomConverters {

    @TypeConverter
    @JvmStatic
    fun uriFromString(value: String?) = if (value == null) null else Uri.parse(value)

    @TypeConverter
    @JvmStatic
    fun uriToString(uri: Uri?) = uri?.toString()

    @TypeConverter
    @JvmStatic
    fun productTypeFromName(name: String?) = if (name == null) null else ProductType.valueOf(name)

    @TypeConverter
    @JvmStatic
    fun productTypeToName(productType: ProductType?) = productType?.name

    @TypeConverter
    @JvmStatic
    fun productVariantFromName(name: String?) = if (name == null) null else ProductVariant.valueOf(name)

    @TypeConverter
    @JvmStatic
    fun productVariantToName(productVariant: ProductVariant?) = productVariant?.name

    @TypeConverter
    @JvmStatic
    fun sessionTypeFromValue(value: Int) = if (value == -1) null else SessionType.fromValue(value)

    @TypeConverter
    @JvmStatic
    fun sessionTypeToValue(sessionType: SessionType?) = sessionType?.value ?: -1

    @TypeConverter
    @JvmStatic
    fun parameterIDFromValue(value: Int) = if (value == -1) null else ParameterID.fromValue(value)

    @TypeConverter
    @JvmStatic
    fun parameterIDToValue(parameterID: ParameterID?) = parameterID?.value ?: -1

    @TypeConverter
    @JvmStatic
    fun uuidToString(uuid: UUID?) = uuid?.toString()

    @TypeConverter
    @JvmStatic
    fun uuidFromString(uuidString: String?) = if (uuidString == null) null else UUID.fromString(uuidString)

    @TypeConverter
    @JvmStatic
    fun parameterIDsetToString(set: EnumSet<ParameterID>) = set.joinToString(COLLECTION_DELIMITER)

    @TypeConverter
    @JvmStatic
    fun parameterIDStringsToSet(string: String): EnumSet<ParameterID> {
        val set = EnumSet.noneOf(ParameterID::class.java)
        string.split(COLLECTION_DELIMITER).forEach {
            try {
                set.add(ParameterID.valueOf(it.trim()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return set
    }

    @TypeConverter
    @JvmStatic
    fun fromScoreTypeToString(type: ScoreType): String {
        return type.key
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToScoreType(key: String): ScoreType {
        return ScoreType.fromKey(key)
    }

    @TypeConverter
    @JvmStatic
    fun fromReadingTypeToString(type: ReadingType): String {
        return type.key
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToReadingType(key: String): ReadingType {
        return ReadingType.fromKey(key)
    }

    @TypeConverter
    @JvmStatic
    fun fromSleepEventTypeToString(type: SleepEventType): String {
        return type.key
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToSleepEventType(key: String): SleepEventType {
        return SleepEventType.fromKey(key)
    }

    @TypeConverter
    @JvmStatic
    fun fromSurveyQuestionToString(question: SurveyQuestion): String {
        return question.key
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToSurveyQuestion(key: String): SurveyQuestion {
        return SurveyQuestion.fromKey(key)
    }

    @TypeConverter
    @JvmStatic
    fun fromSurveyAnswerToString(answer: SurveyAnswer): String {
        return answer.key
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToSurveyAnswer(key: String): SurveyAnswer {
        return SurveyAnswer.fromKey(key)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToSessionTerminatedCause(key: String): SessionTerminatedCause {
        return SessionTerminatedCause.fromKey(key)
    }

    @TypeConverter
    @JvmStatic
    fun fromSessionTerminatedCauseToString(sessionTerminatedCause: SessionTerminatedCause?): String {
        return sessionTerminatedCause?.name ?: SessionTerminatedCause.NONE.name
    }
}
