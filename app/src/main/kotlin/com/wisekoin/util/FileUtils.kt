package com.wisekoin.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

fun uriToMultipartPart (
    context: Context,
    uri: Uri,
    partName: String = "file"
): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri) ?: run {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "image/jpeg"
    }

    if (!mimeType.startsWith("image/")) {
        throw IllegalArgumentException("Invalid format ($mimeType). Please choose an image file.")
    }

    val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Cannot read file from Uri")
    val bytes = inputStream.use { it.readBytes() }

    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        name = partName,
        filename = "receipt_image.jpg",
        body = requestBody
    )
}