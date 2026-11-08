package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AudioVersionEntity
import com.example.data.model.DialogueEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SpeakerEntity
import com.example.data.model.VoiceProfileEntity

@Database(
    entities = [
        ProjectEntity::class,
        SpeakerEntity::class,
        DialogueEntity::class,
        VoiceProfileEntity::class,
        AudioVersionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun speakerDao(): SpeakerDao
    abstract fun dialogueDao(): DialogueDao
    abstract fun voiceProfileDao(): VoiceProfileDao
    abstract fun audioVersionDao(): AudioVersionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emotivoice_studio.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
