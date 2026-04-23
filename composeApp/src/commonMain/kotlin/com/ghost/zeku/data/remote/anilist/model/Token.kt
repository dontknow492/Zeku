package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class TokenRequest(
    @SerialName("grant_type")
    val grantType: String = "authorization_code",

    @SerialName("client_id")
    val clientId: Int,

    @SerialName("client_secret")
    val clientSecret: String,

    @SerialName("redirect_uri")
    val redirectUri: String,

    val code: String
) {
    // SECURITY: Prevent sensitive tokens from being printed in logs/crash reports
    override fun toString(): String {
        return "TokenRequest(" +
                "grantType='$grantType', " +
                "clientId=$clientId, " +
                "redirectUri='$redirectUri', " +
                "clientSecret='***REDACTED***', " +
                "code='***REDACTED***'" +
                ")"
    }
}


@Serializable
data class TokenResponse(
    @SerialName("token_type")
    val tokenType: String,

    @SerialName("expires_in")
    val expiresIn: Long,

    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String? = null
) {
    // CRITICAL SECURITY: Never allow access tokens to be printed in plain text
    override fun toString(): String {
        return "TokenResponse(" +
                "tokenType='$tokenType', " +
                "expiresIn=$expiresIn, " +
                "accessToken='***REDACTED***', " +
                "refreshToken=${if (refreshToken != null) "'***REDACTED***'" else "null"}" +
                ")"
    }
}