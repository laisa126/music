package com.aurora.music.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Phase 2 catalog contract sketch — **not called in Phase 1**.
 *
 * Exists so `network/` is scaffolded now (spec Section 13). Endpoints are
 * anonymous: no auth header, no account, no OAuth. If a chosen provider ever
 * demands per-user login, the provider is wrong, not this contract.
 */
interface CatalogApi {

    @GET("tracks/{id}")
    suspend fun track(@Path("id") id: String): CatalogTrackDto

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 50,
    ): CatalogSearchResponseDto

    @GET("discover")
    suspend fun discover(@Query("limit") limit: Int = 50): CatalogSectionResponseDto
}

@Serializable
data class CatalogTrackDto(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    @SerialName("duration_ms") val durationMs: Long = 0L,
    @SerialName("stream_url") val streamUrl: String? = null,
    @SerialName("artwork_url") val artworkUrl: String? = null,
    val year: Int = 0,
    val genre: String? = null,
)

@Serializable
data class CatalogSearchResponseDto(
    val tracks: List<CatalogTrackDto> = emptyList(),
)

@Serializable
data class CatalogSectionResponseDto(
    val sections: List<CatalogSectionDto> = emptyList(),
)

@Serializable
data class CatalogSectionDto(
    val id: String,
    val title: String,
    val tracks: List<CatalogTrackDto> = emptyList(),
)
