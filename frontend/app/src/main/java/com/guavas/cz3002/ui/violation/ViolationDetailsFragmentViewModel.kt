package com.guavas.cz3002.ui.violation

import android.widget.ImageView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.guavas.cz3002.data.violation.Violation
import com.guavas.cz3002.data.violation.ViolationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ViolationDetailsFragmentViewModel @Inject constructor(
    private val repository: ViolationRepository
) : ViewModel() {
    private val _location = MutableStateFlow("")
    val location = _location.asLiveData()

    fun setLocation(location: String) {
        _location.value = location
    }

    private val _violationId = MutableStateFlow("")

    fun setViolationId(id: String) {
        _violationId.value = id
    }

    private val _violation = _location.combine(_violationId) { location, id -> location to id }
        .flatMapLatest { (location, id) ->
            return@flatMapLatest if (location.isNotBlank()) {
                repository.getViolations(location)
                    .mapNotNull { list -> list.find { it.id == id } }
            } else {
                flowOf(null)
            }
        }.filterNotNull()

    val violation = _violation.asLiveData()

    fun loadImage(image: ImageView, violation: Violation) {
        repository.loadViolationImage(image, violation)
    }

    fun verifyViolation(violation: Violation, isTrue: Boolean, uid: String) = viewModelScope.launch {
        repository.verifyViolation(violation, isTrue, uid)
    }
}