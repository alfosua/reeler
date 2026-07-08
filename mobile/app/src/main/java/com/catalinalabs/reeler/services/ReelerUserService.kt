package com.catalinalabs.reeler.services

import com.catalinalabs.reeler.BuildConfig
import com.clerk.api.Clerk
import com.clerk.api.network.model.error.ClerkErrorResponse
import com.clerk.api.network.model.error.firstMessage
import com.clerk.api.network.serialization.ClerkResult
import com.clerk.api.session.Session
import com.clerk.api.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Wraps Clerk authentication and the premium entitlement. A user is premium
 * when their Clerk account carries `"premium": true` in its public metadata,
 * which the Clerk dashboard (or a billing webhook) sets after they subscribe.
 */
class ReelerUserService {
    val isEnabled: Boolean
        get() = BuildConfig.CLERK_PUBLISHABLE_KEY.isNotBlank()

    val user: StateFlow<User?>
        get() = Clerk.userFlow

    val isPremium: Flow<Boolean>
        get() = user.map { it.isPremium() }

    fun isPremiumNow(): Boolean = isEnabled && user.value.isPremium()

    val usernameFlow: Flow<String?>
        get() = user.map { user ->
            user?.let {
                it.username
                    ?: it.emailAddresses?.firstOrNull()?.emailAddress
            }
        }

    val session: Session?
        get() = Clerk.session

    suspend fun signOut(): Result<Unit> {
        return when (val result = Clerk.signOut()) {
            is ClerkResult.Success -> Result.success(Unit)
            is ClerkResult.Failure -> Result.failure(
                Exception(result.readableMessage ?: "Could not sign out.")
            )
        }
    }

    private fun User?.isPremium(): Boolean {
        val metadata = this?.publicMetadata ?: return false
        val value = metadata["premium"]?.toString()?.trim('"')
        return value == "true"
    }
}

val ClerkResult.Failure<ClerkErrorResponse>.readableMessage: String?
    get() = error?.firstMessage() ?: throwable?.message
