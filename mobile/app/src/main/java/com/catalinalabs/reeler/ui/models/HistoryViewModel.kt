package com.catalinalabs.reeler.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.data.schema.DownloadLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DownloadRepository,
) : ViewModel() {
    val uiState: StateFlow<UiState> =
        repository.flowAll()
            .map { UiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState(),
            )

    data class UiState(val items: List<DownloadLog> = listOf())
}
