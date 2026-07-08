package com.catalinalabs.reeler.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catalinalabs.reeler.BuildConfig
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.ui.models.PremiumAuthState
import com.catalinalabs.reeler.ui.models.PremiumViewModel
import com.catalinalabs.reeler.ui.theme.ReelerTheme
import com.clerk.api.sso.OAuthProvider

@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel,
    modifier: Modifier = Modifier,
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val username by viewModel.username.collectAsState()
    val context = LocalContext.current

    PremiumScreenContent(
        authState = viewModel.authState,
        isPremium = isPremium,
        username = username,
        email = viewModel.email,
        password = viewModel.password,
        code = viewModel.code,
        errorMessage = viewModel.errorMessage,
        isBusy = viewModel.isBusy,
        showSubscribeButton = BuildConfig.PREMIUM_CHECKOUT_URL.isNotBlank(),
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onCodeChange = viewModel::updateCode,
        onSignIn = viewModel::signIn,
        onSignInWithProvider = viewModel::signInWithProvider,
        onSignUp = viewModel::signUp,
        onVerifyCode = viewModel::verifyEmailCode,
        onSignOut = viewModel::signOut,
        onSubscribe = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(BuildConfig.PREMIUM_CHECKOUT_URL),
            )
            context.startActivity(intent)
        },
        modifier = modifier,
    )
}

@Composable
fun PremiumScreenContent(
    authState: PremiumAuthState,
    isPremium: Boolean,
    username: String?,
    email: String,
    password: String,
    code: String,
    errorMessage: String?,
    isBusy: Boolean,
    modifier: Modifier = Modifier,
    showSubscribeButton: Boolean = false,
    onEmailChange: (String) -> Unit = { },
    onPasswordChange: (String) -> Unit = { },
    onCodeChange: (String) -> Unit = { },
    onSignIn: () -> Unit = { },
    onSignInWithProvider: (OAuthProvider) -> Unit = { },
    onSignUp: () -> Unit = { },
    onVerifyCode: () -> Unit = { },
    onSignOut: () -> Unit = { },
    onSubscribe: () -> Unit = { },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.WorkspacePremium,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 16.dp)
                .size(48.dp),
        )
        Text(
            text = stringResource(R.string.premium_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.premium_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        PremiumFeatureList()

        Spacer(Modifier.height(24.dp))

        when (authState) {
            is PremiumAuthState.Disabled -> {
                Text(
                    text = "Coming soon!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }

            is PremiumAuthState.SignedOut -> {
                SignInForm(
                    email = email,
                    password = password,
                    isBusy = isBusy,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onSignIn = onSignIn,
                    onSignInWithProvider = onSignInWithProvider,
                    onSignUp = onSignUp,
                )
            }

            is PremiumAuthState.VerifyingEmail -> {
                VerifyEmailForm(
                    email = email,
                    code = code,
                    isBusy = isBusy,
                    onCodeChange = onCodeChange,
                    onVerifyCode = onVerifyCode,
                )
            }

            is PremiumAuthState.SignedIn -> {
                SignedInContent(
                    isPremium = isPremium,
                    username = username,
                    showSubscribeButton = showSubscribeButton,
                    onSubscribe = onSubscribe,
                    onSignOut = onSignOut,
                )
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun PremiumFeatureList(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PremiumFeature(stringResource(R.string.premium_feature_no_ads))
            PremiumFeature(stringResource(R.string.premium_feature_unlimited))
            PremiumFeature(stringResource(R.string.premium_feature_hd))
            PremiumFeature(stringResource(R.string.premium_feature_support))
        }
    }
}

@Composable
private fun PremiumFeature(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun SignInForm(
    email: String,
    password: String,
    isBusy: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignInWithProvider: (OAuthProvider) -> Unit,
    onSignUp: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            Button(
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.sign_in))
            }
            OutlinedButton(
                onClick = onSignUp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.create_account))
            }
            OrDivider()
            SsoButton(
                text = stringResource(R.string.continue_with_google),
                onClick = { onSignInWithProvider(OAuthProvider.GOOGLE) },
            )
            SsoButton(
                text = stringResource(R.string.continue_with_apple),
                onClick = { onSignInWithProvider(OAuthProvider.APPLE) },
            )
            SsoButton(
                text = stringResource(R.string.continue_with_microsoft),
                onClick = { onSignInWithProvider(OAuthProvider.MICROSOFT) },
            )
        }
    }
}

@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.or_divider),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(Modifier.weight(1f))
    }
}

@Composable
private fun SsoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text)
    }
}

@Composable
private fun VerifyEmailForm(
    email: String,
    code: String,
    isBusy: Boolean,
    onCodeChange: (String) -> Unit,
    onVerifyCode: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "We sent a verification code to $email",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text(stringResource(R.string.verification_code)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            Button(
                onClick = onVerifyCode,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.continue_text))
            }
        }
    }
}

@Composable
private fun SignedInContent(
    isPremium: Boolean,
    username: String?,
    showSubscribeButton: Boolean,
    onSubscribe: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        username?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (isPremium) {
            Text(
                text = stringResource(R.string.premium_active),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = "Your account doesn't have an active subscription yet.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            if (showSubscribeButton) {
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.subscribe))
                }
            }
        }
        TextButton(onClick = onSignOut) {
            Text(stringResource(R.string.sign_out))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PremiumScreenSignedOutPreview() {
    ReelerTheme {
        PremiumScreenContent(
            authState = PremiumAuthState.SignedOut,
            isPremium = false,
            username = null,
            email = "creator@example.com",
            password = "hunter2!",
            code = "",
            errorMessage = null,
            isBusy = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PremiumScreenSignedInPreview() {
    ReelerTheme {
        PremiumScreenContent(
            authState = PremiumAuthState.SignedIn,
            isPremium = true,
            username = "creator@example.com",
            email = "",
            password = "",
            code = "",
            errorMessage = null,
            isBusy = false,
        )
    }
}
