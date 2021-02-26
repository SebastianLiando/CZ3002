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
class ViolationsFragmentViewModel @Inject constructor(
    private val violationRepo: ViolationRepository,
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)

    /** Returns `true` if the app is still fetching data. */
    val isLoading = _isLoading.asLiveData()

    fun updateLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    private val _assignedLocation = MutableStateFlow("")
    val assignedLocation = _assignedLocation.asLiveData()

    /** Returns `true` if the security guard is assigned a location*/
    val isAssignedLocation = _assignedLocation.map { it.isNotBlank() }.asLiveData()

    fun setAssignedLocation(location: String) {
        _assignedLocation.value = location
    }

    private val _showFalsePositive = MutableStateFlow(false)

    fun setShowFalsePositive(show: Boolean) {
        _showFalsePositive.value = show
    }

    private val allViolations = _assignedLocation.flatMapLatest { location ->
        if (location.isBlank()) {
            flowOf(null)
        } else {
            violationRepo.getViolations(location)
        }
    }

    /** Contains violations of the assigned location. Violation is null means that the security guard
     * is not assigned to any location. Empty list means that there is no violation. */
    val violations =
        allViolations.combine(_showFalsePositive) { listViolations, withFalsePositive ->
            if (withFalsePositive) {
                listViolations
            } else {
                listViolations?.filter { !it.isFalsePositive }
            }?.sortedByDescending { it.timestamp }
                ?.sortedBy { it.isVerified }

        }

    fun loadImage(violation: Violation, image: ImageView) {
        violationRepo.loadViolationImage(view = image, violation = violation)
    }

    fun verifyViolation(violation: Violation, isTrue: Boolean, uid: String) {
        viewModelScope.launch {
            violationRepo.verifyViolation(violation, isTrue, uid)
        }
    }
}