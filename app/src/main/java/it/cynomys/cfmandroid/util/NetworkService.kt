package it.cynomys.cfmandroid.util

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class NetworkService {

    // Create an OkHttpClient with a logging interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Logs request and response lines and their respective headers and bodies
        })
        .build()

    // Generic GET request
    suspend fun <T : Any> get(
        path: String,
        queryParams: Map<String, String>? = null,
        responseType: Class<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "${Config.BASE_URL}$path".toHttpUrlOrNull()?.newBuilder()
                ?: throw NetworkError.InvalidURL
            queryParams?.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value)
            }
            val url = urlBuilder.build()

            val request = Request.Builder()
                .url(url)
                .header("CYNOMYS-API-KEY", Config.CYNOMYS_API_KEY)
                .get()
                .build()

            Log.d("NetworkService", "Executing GET request: ${request.url}")
            val response = okHttpClient.newCall(request).execute()
            Log.d("NetworkService", "Received response for ${request.url}: ${response.code}")

            if (!response.isSuccessful) {
                Log.e("NetworkService", "HTTP request failed with code: ${response.code}, message: ${response.message}")
                throw NetworkError.BadStatusCode(response.code)
            }

            val responseBody = response.body?.string()
            Log.d("NetworkService", "Response body received: ${responseBody?.take(200)}...") // Log first 200 chars

            if (responseBody.isNullOrEmpty()) {
                Log.e("NetworkService", "Response body is null or empty.")
                // If the expected response type is String, an empty string might be valid.
                // Otherwise, it might be an error.
                if (responseType == String::class.java) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext Result.success("" as T) // Return empty string for String type
                } else {
                    throw NetworkError.NoContent // Or throw specific error if no content is not valid
                }
            }

            if (responseType == String::class.java) {
                @Suppress("UNCHECKED_CAST")
                Log.d("NetworkService", "Returning raw JSON string.")
                return@withContext Result.success(responseBody as T)
            }

            // For other types, try to deserialize JSON
            val gson = Gson()
            val parsedBody = gson.fromJson(responseBody, responseType)
            Log.d("NetworkService", "Successfully parsed JSON into ${responseType.simpleName}.")
            Result.success(parsedBody)

        } catch (e: Exception) {
            Log.e("NetworkService", "Exception in NetworkService GET: ${e.message}", e)
            Result.failure(NetworkError.fromException(e))
        }
    }


    // Generic POST request
    suspend fun <T : Any, U : Any> post(
        path: String,
        body: T,
        responseType: Class<U>
    ): Result<U> = withContext(Dispatchers.IO) {
        try {
            // Log the path and body
            Log.d("NetworkService", "POST Request Path: $path")
            Log.d("NetworkService", "POST Request Body: ${Gson().toJson(body)}")

            // Convert the body to JSON
            val jsonBody = Gson().toJson(body)
                .toRequestBody("application/json".toMediaType())

            // Create the request
            val request = Request.Builder()
                .url("${Config.BASE_URL}$path")
                .header("CYNOMYS-API-KEY", Config.CYNOMYS_API_KEY)
                .post(jsonBody)
                .build()

            // Execute the request
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw NetworkError.BadStatusCode(response.code)
            }

            // Parse the response
            val jsonString = response.body?.string() ?: throw NetworkError.NoContent
            Log.d("Response", "POST Response Body: $jsonString")
            val responseObject = Gson().fromJson(jsonString, responseType)
            Result.success(responseObject)
        } catch (e: Exception) {
            Log.e("NetworkService", "POST request failed: ${e.message}", e)
            Result.failure(NetworkError.fromException(e))
        }
    }

    // Generic PUT request
    suspend fun <T : Any, U : Any> put(
        path: String,
        body: T,
        responseType: Class<U>
    ): Result<U> = withContext(Dispatchers.IO) {
        try {
            // Convert the body to JSON
            val jsonBody = Gson().toJson(body)
                .toRequestBody("application/json".toMediaType())

            // Create the request
            val request = Request.Builder()
                .url("${Config.BASE_URL}$path")
                .header("CYNOMYS-API-KEY", Config.CYNOMYS_API_KEY)
                .put(jsonBody)
                .build()

            // Execute the request
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw NetworkError.BadStatusCode(response.code)
            }

            // Parse the response
            val jsonString = response.body?.string() ?: throw NetworkError.NoContent
            val responseObject = Gson().fromJson(jsonString, responseType)
            Result.success(responseObject)
        } catch (e: Exception) {
            Result.failure(NetworkError.fromException(e))
        }
    }

    // Generic DELETE request
    suspend fun delete(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Create the request
            val request = Request.Builder()
                .url("${Config.BASE_URL}$path")
                .header("CYNOMYS-API-KEY", Config.CYNOMYS_API_KEY)
                .delete()
                .build()

            // Execute the request
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw NetworkError.BadStatusCode(response.code)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(NetworkError.fromException(e))
        }
    }

    // Network error handling
    sealed class NetworkError : Exception() {
        object InvalidURL : NetworkError()
        object EncodingFailed : NetworkError()
        data class BadStatusCode(val code: Int) : NetworkError()
        object DecodingFailed : NetworkError()
        object NoContent : NetworkError()
        data class NetworkException(val exception: Exception) : NetworkError()

        companion object {
            fun fromException(e: Exception): NetworkError {
                return when (e) {
                    is IOException -> NetworkException(e)
                    else -> NetworkException(e)
                }
            }
        }
    }
}