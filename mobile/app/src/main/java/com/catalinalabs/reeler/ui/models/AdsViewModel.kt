package com.catalinalabs.reeler.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.services.ReelerUserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Exposes whether ads should be shown; premium members never see them.
 */
@HiltViewModel
class AdsViewModel @Inject constructor(
    userService: ReelerUserService,
) : ViewModel() {
    val showAds: StateFlow<Boolean> = userService.isPremium
        .map { premium -> !premium }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = !userService.isPremiumNow(),
        )
}
