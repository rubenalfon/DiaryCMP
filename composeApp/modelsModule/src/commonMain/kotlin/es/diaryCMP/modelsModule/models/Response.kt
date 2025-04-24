package es.diaryCMP.modelsModule.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val idToken: String,
    val email: String,
    val refreshToken: String,
    val expiresIn: Int,
    val localId: String
)

@Serializable
data class TokenResponse(
    @SerialName("expires_in")
    val expiresIn: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("id_token")
    val idToken: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("project_id")
    val projectId: String
)

@Serializable
data class ResetResponse(
    val kind: String,
    val email: String
)

@Serializable
data class ChangePasswordResponse(
    val localId: String,
    val idToken: String,
    val refreshToken: String
)

@Serializable
data class ErrorResponse(
    val error: Error
)

@Serializable
data class Error(
    val code: Int,
    val message: String
)

@Serializable
data class AccountInfoResponse(
    val kind: String,
    val users: List<AccountInfoUserResponse>
) {
    @Serializable
    data class AccountInfoUserResponse(
        val emailVerified: Boolean
    )
}