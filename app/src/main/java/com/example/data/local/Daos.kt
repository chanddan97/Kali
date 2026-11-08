package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AudioVersionEntity
import com.example.data.model.DialogueEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SpeakerEntity
import com.example.data.model.VoiceProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

@Dao
interface SpeakerDao {
    @Query("SELECT * FROM speakers WHERE projectId = :projectId")
    fun getSpeakersForProject(projectId: String): Flow<List<SpeakerEntity>>

    @Query("SELECT * FROM speakers WHERE projectId = :projectId")
    suspend fun getSpeakersForProjectSync(projectId: String): List<SpeakerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeaker(speaker: SpeakerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeakers(speakers: List<SpeakerEntity>)

    @Query("DELETE FROM speakers WHERE projectId = :projectId")
    suspend fun deleteSpeakersByProject(projectId: String)
}

@Dao
interface DialogueDao {
    @Query("SELECT * FROM dialogues WHERE projectId = :projectId ORDER BY sequenceNumber ASC")
    fun getDialoguesForProject(projectId: String): Flow<List<DialogueEntity>>

    @Query("SELECT * FROM dialogues WHERE projectId = :projectId ORDER BY sequenceNumber ASC")
    suspend fun getDialoguesForProjectSync(projectId: String): List<DialogueEntity>

    @Query("SELECT * FROM dialogues WHERE dialogueId = :id LIMIT 1")
    suspend fun getDialogueById(id: String): DialogueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDialogue(dialogue: DialogueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDialogues(dialogues: List<DialogueEntity>)

    @Update
    suspend fun updateDialogue(dialogue: DialogueEntity)

    @Query("DELETE FROM dialogues WHERE dialogueId = :id")
    suspend fun deleteDialogueById(id: String)

    @Query("DELETE FROM dialogues WHERE projectId = :projectId")
    suspend fun deleteDialoguesByProject(projectId: String)

    @Query("UPDATE dialogues SET currentAudioPath = :audioPath WHERE dialogueId = :dialogueId")
    suspend fun updateAudioPath(dialogueId: String, audioPath: String)
}

@Dao
interface VoiceProfileDao {
    @Query("SELECT * FROM voice_profiles ORDER BY isFavourite DESC, name ASC")
    fun getAllVoiceProfiles(): Flow<List<VoiceProfileEntity>>

    @Query("SELECT * FROM voice_profiles WHERE voiceProfileId = :id LIMIT 1")
    suspend fun getVoiceProfileById(id: String): VoiceProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceProfile(profile: VoiceProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceProfiles(profiles: List<VoiceProfileEntity>)

    @Query("SELECT COUNT(*) FROM voice_profiles")
    suspend fun getCount(): Int

    @Update
    suspend fun updateVoiceProfile(profile: VoiceProfileEntity)

    @Delete
    suspend fun deleteVoiceProfile(profile: VoiceProfileEntity)
}

@Dao
interface AudioVersionDao {
    @Query("SELECT * FROM audio_versions WHERE dialogueId = :dialogueId ORDER BY versionNumber DESC")
    fun getVersionsForDialogue(dialogueId: String): Flow<List<AudioVersionEntity>>

    @Query("SELECT * FROM audio_versions WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getVersionsForProject(projectId: String): Flow<List<AudioVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioVersion(version: AudioVersionEntity)

    @Query("SELECT COUNT(*) FROM audio_versions WHERE dialogueId = :dialogueId")
    suspend fun getVersionCountForDialogue(dialogueId: String): Int

    @Query("DELETE FROM audio_versions WHERE audioVersionId = :id")
    suspend fun deleteAudioVersionById(id: String)

    @Query("SELECT SUM(fileSizeBytes) FROM audio_versions")
    suspend fun getTotalAudioSizeBytes(): Long?
}
