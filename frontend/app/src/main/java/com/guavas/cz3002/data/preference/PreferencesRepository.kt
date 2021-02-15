package com.guavas.cz3002.data.preference

import android.content.Context
import androidx.datastore.createDataStore
import com.guavas.cz3002.Preferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore(FILE_NAME, PreferencesSerializer)

    private val preferences = dataStore.data.catch { e ->
        if (e is IOException) {
            Timber.e(e, "Error reading preference!")
            emit(Preferences.getDefaultInstance())
        } else {
            throw e
        }
    }

    /** The Firebase Cloud Messaging topic that this device is currently subscribed to. */
    val subscribedTopic = preferences.map { it.subscribedTopic }

    suspend fun updateSubscribedTopic(newTopic: String) =
        savePreference { setSubscribedTopic(newTopic) }

    suspend fun clearSubscribedTopic() = savePreference { clearSubscribedTopic() }

    /**
     * Saves data into the application's settings.
     *
     * @param block Data changes to be made.
     */
    private suspend fun savePreference(block: Preferences.Builder.() -> Preferences.Builder) {
        dataStore.updateData {
            it.toBuilder().apply { block(this) }.build()
        }
    }

    companion object {
        const val FILE_NAME = "preferences"
    }
}