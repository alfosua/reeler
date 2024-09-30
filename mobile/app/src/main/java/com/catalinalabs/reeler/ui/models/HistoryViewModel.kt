package com.catalinalabs.reeler.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.data.DownloadEntity
import com.catalinalabs.reeler.data.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {
    var uiState: StateFlow<HistoryUiState> =
        repository.getAllDownloadsStream()
            .map { HistoryUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoryUiState(),
            )
}

data class HistoryUiState(val items: List<DownloadEntity> = listOf())
