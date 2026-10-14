package com.wisekoin.data.repository

import com.wisekoin.data.model.ChangePasswordRequest
import com.wisekoin.data.model.DeleteAccountRequest
import com.wisekoin.data.model.OtpPurpose
import com.wisekoin.data.model.OtpRequest
import com.wisekoin.data.model.OtpRequestResponse
import com.wisekoin.data.model.OtpVerifyRequest
import com.wisekoin.data.remote.RetrofitClient

interface AccountSecurityRepository {
    suspend fun requestOtp(purpose: OtpPurpose): OtpRequestResponse
    suspend fun verifyOtp(purpose: OtpPurpose, code: String): String
    suspend fun changePassword(currentPassword: String, newPassword: String, token: String)
    suspend fun deleteAccount(currentPassword: String, token: String)
}

class NetworkAccountSecurityRepository : AccountSecurityRepository {
    override suspend fun requestOtp(purpose: OtpPurpose): OtpRequestResponse =
        RetrofitClient.userApi.requestOtp(OtpRequest(purpose))

    override suspend fun verifyOtp(purpose: OtpPurpose, code: String): String =
        RetrofitClient.userApi.verifyOtp(OtpVerifyRequest(purpose, code)).verificationToken

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        token: String
    ) {
        RetrofitClient.userApi.changePassword(ChangePasswordRequest(currentPassword, newPassword, token))
    }

    override suspend fun deleteAccount(currentPassword: String, token: String) {
        RetrofitClient.userApi.deleteAccount(DeleteAccountRequest(currentPassword, token))
    }
}
