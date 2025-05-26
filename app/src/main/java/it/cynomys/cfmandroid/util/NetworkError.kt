package it.cynomys.cfmandroid.util

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
                is retrofit2.HttpException -> BadStatusCode(e.code())
                is java.net.UnknownHostException -> NetworkException(e)
                is java.io.IOException -> NetworkException(e)
                else -> NetworkException(e)
            }
        }
    }
}