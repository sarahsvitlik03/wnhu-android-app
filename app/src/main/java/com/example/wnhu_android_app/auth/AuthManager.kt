package com.example.wnhu_android_app

import android.app.Activity
import android.content.Context
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException

object AuthManager {
    private var singleAccountApp: ISingleAccountPublicClientApplication? = null
    private var initializing = false
    private val scopes = listOf("User.Read")

    fun signIn(
        activity: Activity,
        onSuccess: (MicrosoftAccountProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        getApplication(
            context = activity.applicationContext,
            onReady = { app ->
                val parameters = SignInParameters.builder()
                    .withActivity(activity)
                    .withScopes(scopes)
                    .withCallback(object : com.microsoft.identity.client.AuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult) {
                            onSuccess(authenticationResult.toProfile())
                        }

                        override fun onError(exception: MsalException) {
                            onError(exception.message ?: "Microsoft sign-in failed.")
                        }

                        override fun onCancel() {
                            onError("Microsoft sign-in was cancelled.")
                        }
                    })
                    .build()

                app.signIn(parameters)
            },
            onError = onError
        )
    }

    fun signOut(onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val app = singleAccountApp
        if (app == null) {
            onComplete()
            return
        }

        app.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
                onComplete()
            }

            override fun onError(exception: MsalException) {
                onError(exception.message ?: "Microsoft sign-out failed.")
            }
        })
    }

    private fun getApplication(
        context: Context,
        onReady: (ISingleAccountPublicClientApplication) -> Unit,
        onError: (String) -> Unit
    ) {
        singleAccountApp?.let {
            onReady(it)
            return
        }

        if (initializing) {
            onError("Microsoft login is still initializing. Try again.")
            return
        }

        initializing = true
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    initializing = false
                    singleAccountApp = application
                    onReady(application)
                }

                override fun onError(exception: MsalException) {
                    initializing = false
                    onError(exception.message ?: "Could not initialize Microsoft login.")
                }
            }
        )
    }

    private fun IAuthenticationResult.toProfile(): MicrosoftAccountProfile {
        val account: IAccount = account
        val claims = account.claims ?: emptyMap<String, Any>()
        val firstName = claims["given_name"]?.toString().orEmpty()
        val lastName = claims["family_name"]?.toString().orEmpty()

        return MicrosoftAccountProfile(
            email = account.username.orEmpty(),
            firstName = firstName,
            lastName = lastName
        )
    }
}
