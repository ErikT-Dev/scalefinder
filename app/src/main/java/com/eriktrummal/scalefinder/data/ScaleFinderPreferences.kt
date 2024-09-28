package com.eriktrummal.scalefinder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScaleFinderPreferences(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scale_finder_preferences")
        private val SELECTED_NOTES = stringPreferencesKey("selected_notes")
        private val ROOT_NOTE = intPreferencesKey("root_note")
        private val USE_FLATS = booleanPreferencesKey("use_flats")
        private val FAMILY_INCLUSION = stringPreferencesKey("family_inclusion")
    }

    val selectedNotesFlow: Flow<List<Int>> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_NOTES]?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?: emptyList()
        }

    val rootNoteFlow: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[ROOT_NOTE]
        }

    val useFlatsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[USE_FLATS] ?: false
        }

    val familyInclusionFlow: Flow<Map<String, Boolean>> = context.dataStore.data
        .map { preferences ->
            preferences[FAMILY_INCLUSION]?.split(",")
                ?.associate {
                    val (family, included) = it.split(":")
                    family to included.toBoolean()
                }
                ?: emptyMap()
        }

    suspend fun saveSelectedNotes(selectedNotes: List<Int>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_NOTES] = if (selectedNotes.isEmpty()) "" else selectedNotes.joinToString(",")
        }
    }

    suspend fun saveRootNote(rootNote: Int?) {
        context.dataStore.edit { preferences ->
            if (rootNote != null) {
                preferences[ROOT_NOTE] = rootNote
            } else {
                preferences.remove(ROOT_NOTE)
            }
        }
    }

    suspend fun saveUseFlats(useFlats: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_FLATS] = useFlats
        }
    }

    suspend fun saveFamilyInclusion(familyInclusion: Map<String, Boolean>) {
        context.dataStore.edit { preferences ->
            preferences[FAMILY_INCLUSION] = familyInclusion.entries.joinToString(",") { "${it.key}:${it.value}" }
        }
    }
}