package com.guavas.cz3002.ui.violation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.guavas.cz3002.data.violation.ViolationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    private val _assignedLocation: MutableStateFlow<String> = MutableStateFlow("")
    val assignedLocation = _assignedLocation.asLiveData()

    /** Returns `true` if the security guard is assigned a location*/
    val isAssignedLocation = _assignedLocation.map { it.isNotBlank() }.asLiveData()

    fun setAssignedLocation(location: String) {
        _assignedLocation.value = location
    }

    /** Contains violations of the assigned location. Violation is null means that the security guard
     * is not assigned to any location. Empty list means that there is no violation. */
    val violations = _assignedLocation.flatMapLatest {
        if (it.isBlank()) {
            flowOf(null)
        } else {
            violationRepo.getViolations(it)
        }
    }
}