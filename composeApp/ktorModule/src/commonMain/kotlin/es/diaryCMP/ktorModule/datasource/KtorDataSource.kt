package es.diaryCMP.ktorModule.datasource

import es.diaryCMP.modelsModule.models.AccountInfoResponse
import es.diaryCMP.modelsModule.models.AuthResponse
import es.diaryCMP.modelsModule.models.ChangePasswordResponse
import es.diaryCMP.modelsModule.models.FirebaseUser
import es.diaryCMP.modelsModule.models.TokenResponse
import es.diaryCMP.utilsModule.utils.FirebaseConstants
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class KtorDataSource(
    private val httpClient: HttpClient,
    private val appConstants: FirebaseConstants
) {
    // PART FirebaseAuth
    suspend fun signup(email: String, password: String): AuthResponse {
        val requestBody = mapOf(
            "email" to email,
            "password" to password,
            "returnSecureToken" to "true"
        )
        val response: HttpResponse =
            httpClient.post("${appConstants.AUTH_URL}accounts:signUp?key=${appConstants.APIKEY}") {
                header("Content-Type", "application/json")
                setBody(Json.encodeToString(requestBody))
            }

        return if (response.status.value in 200..299) {
            response.body<AuthResponse>()
        } else {
            Napier.e("KtorDataSource.signUpWithEmailAndPassword: ${response.bodyAsText()}")
            throw IOException(response.bodyAsText())
        }
    }

    suspend fun sendVerificationEmail(idToken: String) {
        val requestBody = mapOf(
            "requestType" to "VERIFY_EMAIL",
            "idToken" to idToken
        )

        val response =
            httpClient.post("${appConstants.AUTH_URL}accounts:sendOobCode?key=${appConstants.APIKEY}") {
                header("Content-Type", "application/json")
                setBody(Json.encodeToString(requestBody))
            }

        if (response.status.value in 200..299) {
            return
        } else {
            Napier.e("KtorDataSource.login: ${response.bodyAsText()}")
            throw IOException(response.bodyAsText())
        }
    }

    suspend fun isEmailVerified(idToken: String): Boolean {
        val requestBody = mapOf(
            "idToken" to idToken
        )

        val response =
            httpClient.post("${appConstants.AUTH_URL}accounts:lookup?key=${appConstants.APIKEY}") {
                header("Content-Type", "application/json")
                setBody(requestBody)
            }

        if (response.status.value in 200..299) {
            val accountInfoResponse = response.body<AccountInfoResponse>()
            return accountInfoResponse.users.first().emailVerified
        } else {
            Napier.e("KtorDataSource.login: ${response.bodyAsText()}")
            throw IOException(response.bodyAsText())
        }
    }

    @Throws(IOException::class, CancellationException::class)
    suspend fun login(email: String, password: String): AuthResponse {
        val requestBody = mapOf(
            "email" to email,
            "password" to password,
            "returnSecureToken" to "true"
        )

        val response =
            httpClient.post("${appConstants.AUTH_URL}accounts:signInWithPassword?key=${appConstants.APIKEY}") {
                header("Content-Type", "application/json")
                setBody(Json.encodeToString(requestBody))
            }

        return if (response.status.value in 200..299) {
            response.body<AuthResponse>()
        } else {
            Napier.e("KtorDataSource.login: ${response.bodyAsText()}")
            throw IOException(response.bodyAsText())
        }
    }

    suspend fun getRefreshToken(refreshToken: String?): TokenResponse {
        val requestBody = mapOf(
            "grant_type" to "refresh_token",
            "refresh_token" to refreshToken
        )

        val response = httpClient.post("${appConstants.AUTH_URL}token?key=${appConstants.APIKEY}") {
            header("Content-Type", "application/json")
            setBody(Json.encodeToString(requestBody))
        }

        return if (response.status.value in 200..299) {
            response.body<TokenResponse>()
        } else {
            Napier.e("KtorDataSource.getRefreshToken: ${response.bodyAsText()}")
            throw IOException(response.bodyAsText())
        }
    }

    suspend fun changeUserPassword(idToken: String, password: String): ChangePasswordResponse {
        val requestBody = mapOf(
            "idToken" to idToken,
            "password" to password,
            "returnSecureToken" to "true"
        )

        val response =
            httpClient.post("${appConstants.AUTH_URL}accounts:update?key=${appConstants.APIKEY}") {
                header("Content-Type", "application/json")
                setBody(Json.encodeToString(requestBody))
            }

        return if (response.status.value in 200..299) {
            response.body<ChangePasswordResponse>()
        } else {
            Napier.e("KtorDataSource.changeUserPassword: ${response.bodyAsText()}")
            throw IOException(response.bodyAsText())
        }
    }


    // SECTION FIREBASE DATABASE
    @Throws(IOException::class, CancellationException::class)
    suspend fun firebaseFirestoreRequest(
        method: HttpMethod,
        child: List<String>,
        parameter: HashMap<String, Any>? = null,
        query: String? = null,
        currentUser: FirebaseUser
    ): String {
        try {
            val idToken = currentUser.idToken
            val childPath = child.joinToString("/")
            val url = "${appConstants.DATABASE_URL}/$childPath${query?.let { "/$it" } ?: ""}"

            val responseBody = httpClient.request(url) {
                this.method = method
                header("Authorization", "Bearer $idToken")
                header("Content-Type", "application/json")
                if (parameter != null) {
                    setBody(parameter)
                }
            }


            return if (responseBody.status.value in 200..299) {
                responseBody.bodyAsText()
            } else {
                Napier.e("KtorDataSource.firebaseFirestoreRequest: ${responseBody.request.url} ${responseBody.bodyAsText()}")
                throw Exception("${responseBody.request.url} ${responseBody.bodyAsText()}")
            }
        } catch (ex: Exception) {
            Napier.e("KtorDataSource.firebaseFirestoreRequest: ${ex.message}")

            if (ex.message?.contains("Failed to connect") == true) {
                throw NoConnectionException("No internet connection")
            }

            throw ex
        }
    }

    @Throws(IOException::class, CancellationException::class)
    suspend fun firebaseFirestorePut(
        child: List<String>,
        parameter: HashMap<String, Any>,
        currentUser: FirebaseUser
    ): String =
        firebaseFirestoreRequest(HttpMethod.Put, child, parameter, currentUser = currentUser)

    @Throws(IOException::class, CancellationException::class)
    suspend fun firebaseFirestorePost(
        child: List<String>,
        parameter: HashMap<String, Any>,
        currentUser: FirebaseUser
    ): String =
        firebaseFirestoreRequest(HttpMethod.Post, child, parameter, currentUser = currentUser)

    @Throws(IOException::class, CancellationException::class)
    suspend fun firebaseFirestorePatch(
        child: List<String>,
        parameter: HashMap<String, Any>,
        currentUser: FirebaseUser
    ): String {
        val updateMasks = (parameter.values.first() as HashMap<*, *>).keys
            .joinToString(
                separator = "&",
                prefix = "?",
                transform = { "updateMask.fieldPaths=$it" }
            )
        return firebaseFirestoreRequest(
            HttpMethod.Patch,
            child,
            parameter,
            query = updateMasks,
            currentUser = currentUser
        )
    }

    suspend fun firebaseFirestoreGet(
        child: List<String>,
        query: String?,
        currentUser: FirebaseUser
    ): String =
        firebaseFirestoreRequest(HttpMethod.Get, child, query = query, currentUser = currentUser)

}