package com.limpyo.app.core.auth

import android.content.Context
import com.limpyo.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton

/** Deep links from auth emails come back as limpyo://auth/... (declared in AndroidManifest.xml). */
const val AUTH_SCHEME = "limpyo"
const val AUTH_HOST = "auth"
const val REDIRECT_CONFIRM = "$AUTH_SCHEME://$AUTH_HOST/callback"
const val REDIRECT_RESET = "$AUTH_SCHEME://$AUTH_HOST/reset"

/** True when local.properties (or CI) provided the dev backend. Without it the app stays guest-only. */
val isBackendConfigured: Boolean
    get() = BuildConfig.SUPABASE_URL.startsWith("https://") && BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()

val isGoogleConfigured: Boolean
    get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.endsWith(".apps.googleusercontent.com")

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    @Provides
    @Singleton
    fun supabase(@ApplicationContext context: Context): SupabaseClient = createSupabaseClient(
        // Placeholder values keep the client valid when unconfigured; isBackendConfigured guards every call.
        supabaseUrl = BuildConfig.SUPABASE_URL.ifBlank { "https://unconfigured.invalid" },
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY.ifBlank { "unconfigured" },
    ) {
        httpEngine = OkHttp.create()
        install(Auth) {
            scheme = AUTH_SCHEME
            host = AUTH_HOST
            flowType = FlowType.PKCE // email links carry a one-time code, not the session itself
            sessionManager = KeystoreSessionManager(context)
        }
        install(Postgrest)
        install(ComposeAuth) {
            if (isGoogleConfigured) googleNativeLogin(serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID)
        }
    }
}
