package com.aurora.music.di

import com.aurora.music.BuildConfig
import com.aurora.music.data.network.CatalogApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Phase 2 scaffolding (spec Section 13). Wired but unused in Phase 1 so the
 * catalog/streaming layer is additive rather than a retrofit.
 *
 * Hard constraint: **no user accounts, no login, no OAuth.** There is no
 * `Authorization` interceptor here and there must never be one carrying a
 * per-user identity — only an optional static, app-scoped key.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Placeholder; the real provider is chosen when Phase 2 begins. */
    private const val CATALOG_BASE_URL = "https://catalog.invalid/api/v1/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        // BASIC only: never log file paths or user data (Section 10).
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(CATALOG_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideCatalogApi(retrofit: Retrofit): CatalogApi = retrofit.create(CatalogApi::class.java)
}
