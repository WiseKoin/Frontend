package com.wisekoin.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File

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

    contentResolver.openInputStream(uri)?.close()
        ?: throw IllegalArgumentException("Cannot read file from Uri")
    val requestBody = ContentUriRequestBody(context.applicationContext, uri, mimeType.toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        name = partName,
        filename = "receipt_image.jpg",
        body = requestBody
    )
}

fun deleteTemporaryReceipt(context: Context, uri: Uri) {
    if (uri.scheme != "file") return
    val candidate = uri.path?.let(::File)?.canonicalFile ?: return
    val cacheDirectory = context.cacheDir.canonicalFile
    if (candidate.parentFile == cacheDirectory && candidate.name.startsWith("Receipt_")) {
        candidate.delete()
    }
}

private class ContentUriRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val mediaType: MediaType?
) : RequestBody() {
    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long =
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L

    override fun writeTo(sink: BufferedSink) {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot read file from Uri")
        input.source().use(sink::writeAll)
    }
}
