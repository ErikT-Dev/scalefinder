package com.eriktrummal.scalefinder

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eriktrummal.scalefinder.data.Scale
import com.eriktrummal.scalefinder.data.ScaleFinderPreferences
import com.eriktrummal.scalefinder.data.ScalesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val scalesDao = ScalesDao.getInstance(application)
    private val preferences = ScaleFinderPreferences(application)

    private val _selectedNotes = MutableStateFlow<List<Int>>(emptyList())
    val selectedNotes: StateFlow<List<Int>> = _selectedNotes

    private val _familyInclusionState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val familyInclusionState: StateFlow<Map<String, Boolean>> = _familyInclusionState

    private val _rootNote = MutableStateFlow<Int?>(null)
    val rootNote: StateFlow<Int?> = _rootNote

    private val _useFlats = MutableStateFlow(false)
    val useFlats: StateFlow<Boolean> = _useFlats

    private val _currentScreen = mutableStateOf<Screen>(Screen.BottomScreen.ScaleFinder)
    val currentScreen: State<Screen> = _currentScreen

    private val _expandedFamily = mutableStateOf<String?>(null)
    val expandedFamily: State<String?> = _expandedFamily

    private val _families = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val families: StateFlow<List<Pair<String, Int>>> = _families

    private val _modes = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val modes: StateFlow<List<Pair<Int, String>>> = _modes

    private val _scales = MutableStateFlow<List<Scale>>(emptyList())
    val scales: StateFlow<List<Scale>> = _scales

    private val _myScales = MutableStateFlow<List<Scale>>(emptyList())
    val myScales: StateFlow<List<Scale>> = _myScales

    private val _myScalesStatus = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val myScalesStatus: StateFlow<Map<Int, Boolean>> = _myScalesStatus

    private val _familyInclusionUpdateTrigger = MutableStateFlow(0)
    val familyInclusionUpdateTrigger: StateFlow<Int> = _familyInclusionUpdateTrigger

    private fun handleDatabaseError(e: Exception) {
        Log.e("MainViewModel", "Database operation failed", e)
    }

    suspend fun getMatchingScales(selectedNotes: List<Int>, rootNote: Int?): List<Scale> {
        return try {
            withContext(Dispatchers.IO) {
                if (selectedNotes.isEmpty()) {
                    emptyList()
                } else {
                    scalesDao.getAllScales().filter { scale ->
                        scale.isIncluded &&
                                selectedNotes.all { it in scale.notes } &&
                                (rootNote?.let { scale.notes.firstOrNull() == it } ?: true)
                    }
                }
            }
        } catch (e: Exception) {
            handleDatabaseError(e)
            emptyList()
        }
    }
    suspend fun getPotentialMatchingScales(selectedNotes: List<Int>, rootNote: Int?): List<List<Scale>> {
        return withContext(Dispatchers.IO) {
            val allScales = scalesDao.getAllScales().filter { it.isIncluded }
            (1..12).map { noteToAdd ->
                if (selectedNotes.isEmpty()) {
                    allScales.filter { scale -> scale.notes.contains(noteToAdd) }
                } else if (noteToAdd in selectedNotes) {
                    allScales.filter { scale ->
                        selectedNotes.all { it in scale.notes } &&
                                (rootNote?.let { scale.notes.firstOrNull() == it } ?: true)
                    }
                } else {
                    allScales.filter { scale ->
                        (selectedNotes + noteToAdd).all { it in scale.notes } &&
                                (rootNote?.let { scale.notes.firstOrNull() == it } ?: true)
                    }
                }
            }
        }
    }

    fun toggleFamilyInclusion(family: String) {
        viewModelScope.launch {
            val currentState = _familyInclusionState.value
            val newIncludedState = !(currentState[family] ?: true)
            val newState = currentState.toMutableMap().apply { put(family, newIncludedState) }
            _familyInclusionState.value = newState
            preferences.saveFamilyInclusion(newState)
            scalesDao.updateScalesInclusion(family, newIncludedState)
            updateAllScales()
            _familyInclusionUpdateTrigger.value += 1
        }
    }

    private fun updateAllScales() {
        viewModelScope.launch {
            _scales.value = scalesDao.getAllScales()
        }
    }

    suspend fun getScaleByIdFromDatabase(id: Int): Scale? {
        return scalesDao.getScaleById(id)
    }

    fun setCurrentScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setExpandedFamily(familyName: String?) {
        _expandedFamily.value = familyName
    }

    fun checkScaleInMyScales(scaleId: Int) {
        viewModelScope.launch {
            val isInMyScales = scalesDao.isInMyScales(scaleId)
            _myScalesStatus.value += (scaleId to isInMyScales)
        }
    }

    fun loadMyScales() {
        viewModelScope.launch {
            _myScales.value = scalesDao.getMyScales()
            updateMyScalesStatus()
        }
    }

    private fun updateMyScalesStatus() {
        val status = _myScales.value.associate { it.id to true }
        _myScalesStatus.value = status
    }

    fun addToMyScales(scale: Scale) {
        viewModelScope.launch {
            scalesDao.addToMyScales(scale)
            _myScales.value += scale
            _myScalesStatus.value += (scale.id to true)
        }
    }

    fun removeFromMyScales(scale: Scale) {
        viewModelScope.launch {
            scalesDao.removeFromMyScales(scale)
            _myScales.value -= scale
            _myScalesStatus.value += (scale.id to false)
        }
    }

    private fun loadFamilies() {
        viewModelScope.launch {
            val familiesWithId = scalesDao.getAllFamiliesWithLowestId()
            _families.value = familiesWithId
            val currentInclusion = _familyInclusionState.value
            val updatedInclusion = familiesWithId.associate { (family, _) ->
                family to (currentInclusion[family] ?: true)
            }
            _familyInclusionState.value = updatedInclusion
            preferences.saveFamilyInclusion(updatedInclusion)
        }
    }

    fun loadModesForFamily(family: String) {
        viewModelScope.launch {
            _modes.value = scalesDao.getUniqueModeNamesForFamily(family)
        }
    }

    fun loadScalesForFamilyAndMode(family: String, mode: String) {
        viewModelScope.launch {
            _scales.value = scalesDao.getScalesForFamilyAndMode(family, mode)
        }
    }

    fun getScaleById(id: Int): Scale? {
        return scales.value.find { it.id == id }
    }

    init {
        viewModelScope.launch {
            preferences.selectedNotesFlow.collect {
                _selectedNotes.value = it
            }
        }
        viewModelScope.launch {
            preferences.rootNoteFlow.collect {
                _rootNote.value = it
            }
        }
        viewModelScope.launch {
            preferences.useFlatsFlow.collect {
                _useFlats.value = it
            }
        }
        viewModelScope.launch {
            preferences.familyInclusionFlow.collect {
                _familyInclusionState.value = it
            }
        }
        loadFamilies()
        loadMyScales()
    }

    fun setSelectedNotes(notes: List<Int>) {
        viewModelScope.launch {
            _selectedNotes.value = notes
            preferences.saveSelectedNotes(notes)
        }
    }

    fun setRootNote(note: Int?) {
        viewModelScope.launch {
            _rootNote.value = note
            preferences.saveRootNote(note)
        }
    }

    fun setUseFlats(useFlats: Boolean) {
        viewModelScope.launch {
            _useFlats.value = useFlats
            preferences.saveUseFlats(useFlats)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scalesDao.close()
    }
}