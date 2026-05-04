package com.jigen.practicse.network

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseService {
    private const val TAG = "SupabaseService"

    fun create(supabaseUrl: String, supabaseKey: String): SupabaseApi {
        val gson = GsonBuilder().create()

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val authInterceptor = Interceptor { chain ->
            val original: Request = chain.request()
            val reqBuilder = original.newBuilder()
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Content-Type", "application/json")
            chain.proceed(reqBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val baseUrl = if (supabaseUrl.endsWith("/")) supabaseUrl.dropLast(1) else supabaseUrl
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        Log.d(TAG, "SupabaseService created for $baseUrl")
        return retrofit.create(SupabaseApi::class.java)
    }
}
