package com.wisekoin.data.repository

import com.wisekoin.data.model.ChangePasswordRequest
import com.wisekoin.data.model.DeleteAccountRequest
import com.wisekoin.data.model.OtpPurpose
import com.wisekoin.data.model.OtpRequest
import com.wisekoin.data.model.OtpRequestResponse
import com.wisekoin.data.model.OtpVerifyRequest
import com.wisekoin.data.remote.UserApiService

interface AccountSecurityRepository {
    suspend fun requestOtp(purpose: OtpPurpose): OtpRequestResponse
    suspend fun verifyOtp(purpose: OtpPurpose, code: String): String
    suspend fun changePassword(currentPassword: String, newPassword: String, token: String)
    suspend fun deleteAccount(currentPassword: String, token: String)
}

class NetworkAccountSecurityRepository(
    private val api: UserApiService
) : AccountSecurityRepository {
    override suspend fun requestOtp(purpose: OtpPurpose): OtpRequestResponse =
        api.requestOtp(OtpRequest(purpose))

    override suspend fun verifyOtp(purpose: OtpPurpose, code: String): String =
        api.verifyOtp(OtpVerifyRequest(purpose, code)).verificationToken

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        token: String
    ) {
        api.changePassword(ChangePasswordRequest(currentPassword, newPassword, token))
    }

    override suspend fun deleteAccount(currentPassword: String, token: String) {
        api.deleteAccount(DeleteAccountRequest(currentPassword, token))
    }
}
