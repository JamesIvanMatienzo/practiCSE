package com.jigen.practicse.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

data class LeaderboardUpsertRequest(val user_name: String, val total_score: Int)

interface SupabaseApi {
    @POST("/rest/v1/leaderboard")
    suspend fun upsertLeaderboard(
        @Body body: LeaderboardUpsertRequest,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Response<Unit>

    @GET("/rest/v1/leaderboard")
    suspend fun getTop(
        @Query("select") select: String = "*",
        @Query("order") order: String = "total_score.desc",
        @Query("limit") limit: Int = 100
    ): Response<List<Map<String, Any>>> // simple parsed response
}
