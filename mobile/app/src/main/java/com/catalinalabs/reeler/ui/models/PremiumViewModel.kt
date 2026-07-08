package com.catalinalabs.reeler.ui.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.services.ReelerUserService
import com.catalinalabs.reeler.services.readableMessage
import com.clerk.api.network.serialization.ClerkResult
import com.clerk.api.network.serialization.onFailure
import com.clerk.api.network.serialization.onSuccess
import com.clerk.api.signin.SignIn
import com.clerk.api.signup.SignUp
import com.clerk.api.signup.attemptVerification
import com.clerk.api.signup.prepareVerification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PremiumAuthState {
    data object Disabled : PremiumAuthState
    data object SignedOut : PremiumAuthState
    data object VerifyingEmail : PremiumAuthState
    data object SignedIn : PremiumAuthState
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    val userService: ReelerUserService,
) : ViewModel() {
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var code by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isBusy by mutableStateOf(false)
        private set
    var authState by mutableStateOf(
        if (userService.isEnabled) PremiumAuthState.SignedOut else PremiumAuthState.Disabled
    )
        private set

    private var inProgressSignUp: SignUp? = null

    val isPremium: StateFlow<Boolean> = userService.isPremium
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val username: StateFlow<String?> = userService.usernameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    init {
        if (userService.isEnabled) {
            viewModelScope.launch {
                userService.user.collect { user ->
                    if (authState != PremiumAuthState.VerifyingEmail) {
                        authState = if (user != null) {
                            PremiumAuthState.SignedIn
                        } else {
                            PremiumAuthState.SignedOut
                        }
                    }
                }
            }
        }
    }

    fun updateEmail(value: String) {
        email = value
    }

    fun updatePassword(value: String) {
        password = value
    }

    fun updateCode(value: String) {
        code = value
    }

    fun signIn() {
        runAuthAction {
            SignIn.create(
                SignIn.CreateParams.Strategy.Password(
                    identifier = email.trim(),
                    password = password,
                )
            )
                .onSuccess {
                    authState = PremiumAuthState.SignedIn
                }
                .onFailure {
                    errorMessage = it.readableMessage ?: "Could not sign in."
                }
        }
    }

    fun signUp() {
        runAuthAction {
            val result = SignUp.create(
                SignUp.CreateParams.Standard(
                    emailAddress = email.trim(),
                    password = password,
                )
            )
            when (result) {
                is ClerkResult.Success -> {
                    inProgressSignUp = result.value
                    prepareEmailVerification(result.value)
                }

                is ClerkResult.Failure -> {
                    errorMessage = result.readableMessage ?: "Could not create the account."
                }
            }
        }
    }

    private suspend fun prepareEmailVerification(signUp: SignUp) {
        signUp.prepareVerification(SignUp.PrepareVerificationParams.Strategy.EmailCode())
            .onSuccess {
                authState = PremiumAuthState.VerifyingEmail
            }
            .onFailure {
                errorMessage = it.readableMessage ?: "Could not send the verification code."
            }
    }

    fun verifyEmailCode() {
        val signUp = inProgressSignUp ?: return
        runAuthAction {
            signUp.attemptVerification(
                SignUp.AttemptVerificationParams.EmailCode(code.trim())
            )
                .onSuccess {
                    inProgressSignUp = null
                    authState = PremiumAuthState.SignedIn
                }
                .onFailure {
                    errorMessage = it.readableMessage ?: "Invalid verification code."
                }
        }
    }

    fun signOut() {
        runAuthAction {
            userService.signOut()
                .onSuccess {
                    authState = PremiumAuthState.SignedOut
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    private fun runAuthAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            errorMessage = null
            isBusy = true
            try {
                action()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isBusy = false
            }
        }
    }
}
