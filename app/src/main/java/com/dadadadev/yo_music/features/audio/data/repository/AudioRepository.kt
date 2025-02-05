package com.dadadadev.yo_music.features.audio.data.repository

import com.dadadadev.yo_music.features.audio.data.local.ContentResolverHelper
import com.dadadadev.yo_music.features.audio.data.local.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val contentResolver: ContentResolverHelper
) {
    suspend fun getAudioData() : List<Audio> = withContext(Dispatchers.IO) {
        contentResolver.getAudioData()
    }
}