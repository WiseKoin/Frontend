package com.wisekoin.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wisekoin.R as AppR
import com.wisekoin.ui.theme.ControlBtnBg
import com.wisekoin.ui.theme.NunitoSansFamily
import com.wisekoin.ui.theme.ReticleRed
import com.wisekoin.ui.theme.neomorphicCircle
import com.wisekoin.util.uriToMultipartPart
import com.wisekoin.util.deleteTemporaryReceipt
import com.wisekoin.viewmodel.ReceiptViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ScanReceiptScreen(
    onClose: () -> Unit,
    onNavigateToConfirm: () -> Unit,
    viewModel: ReceiptViewModel
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Please provide camera access to scan receipt.", Toast.LENGTH_SHORT).show()
            onClose()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // Xử lý chọn ảnh từ bộ sưu tập
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val part = uriToMultipartPart(context, uri, "file")
            viewModel.uploadReceipt(uri, part) {
                deleteTemporaryReceipt(context, uri)
            }
            onNavigateToConfirm()
        }
    }

    var cameraApsectRatio by remember { mutableStateOf<Float?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(all = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan Receipt",
                    fontFamily = NunitoSansFamily,
                    color = Color(0xFF161A32),
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .neomorphicCircle(
                            shadowColor = Color.Black.copy(alpha = 0.18f),
                            lightColor = Color.White.copy(alpha = 0.9f),
                            offset = 3.dp,
                            blur = 6.dp
                        )
                        .background(Color(0xFFF0F5F9), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E293B)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Align receipt within the\nframe",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (cameraApsectRatio != null) {
                        Modifier.aspectRatio(cameraApsectRatio!!)
                    } else {
                        Modifier.aspectRatio(3f / 4f)
                    }
                )
        ) {
            CameraPreview(
                onImageCaptureReady = { imageCapture = it },
                onCameraReady = { camera = it },
                onAspectRatioDetermined = { ratio -> cameraApsectRatio = ratio }
            )
            ReceiptScanOverlay()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraBottomControls(
                onGalleryClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onCaptureClick = {
                    capturePhoto(context, imageCapture) { savedUri ->
                        try {
                            val part = uriToMultipartPart(context, savedUri, "file")
                            viewModel.uploadReceipt(savedUri, part) {
                                deleteTemporaryReceipt(context, savedUri)
                            }
                            onNavigateToConfirm()
                        } catch (error: IllegalArgumentException) {
                            deleteTemporaryReceipt(context, savedUri)
                            Toast.makeText(
                                context,
                                error.message ?: "Unable to read the captured receipt.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                isFlashOn = isFlashOn,
                onFlashToggle = {
                    camera?.cameraControl?.let { control ->
                        isFlashOn = !isFlashOn
                        control.enableTorch(isFlashOn)
                    }
                }
            )
        }
    }
}

@Composable
private fun CameraPreview(
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCameraReady: (Camera) -> Unit,
    onAspectRatioDetermined: (Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                onImageCaptureReady(imageCapture)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    val cam = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    onCameraReady(cam)
                    preview.resolutionInfo?.let { info ->
                        val rotation = info.rotationDegrees
                        val res = info.resolution
                        val isRotated = rotation == 90 || rotation == 270
                        val realWidth = if (isRotated) res.height else res.width
                        val realHeight = if (isRotated) res.width else realHeightHelper(res)
                        val actualRatio = realWidth.toFloat() / realHeight.toFloat()
                        onAspectRatioDetermined(actualRatio)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun realHeightHelper(res: android.util.Size): Int = res.height

@Composable
private fun ReceiptScanOverlay() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .fillMaxHeight(0.70f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                val cornerLength = 40.dp.toPx()
                val w = size.width
                val h = size.height
                drawLine(ReticleRed, Offset(0f, cornerLength), Offset(0f, 0f), strokeWidth)
                drawLine(ReticleRed, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
                drawLine(ReticleRed, Offset(w - cornerLength, 0f), Offset(w, 0f), strokeWidth)
                drawLine(ReticleRed, Offset(w, 0f), Offset(w, cornerLength), strokeWidth)
                drawLine(ReticleRed, Offset(0f, h - cornerLength), Offset(0f, h), strokeWidth)
                drawLine(ReticleRed, Offset(0f, h), Offset(cornerLength, h), strokeWidth)
                drawLine(ReticleRed, Offset(w - cornerLength, h), Offset(w, h), strokeWidth)
                drawLine(ReticleRed, Offset(w, h), Offset(w, h - cornerLength), strokeWidth)
            }
        }
    }
}

@Composable
private fun CameraBottomControls(
    modifier: Modifier = Modifier,
    onGalleryClick: () -> Unit,
    onCaptureClick: () -> Unit,
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ControlBtnBg)
                .clickable(onClick = onGalleryClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Gallery",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF424242))
                .clickable(onClick = onCaptureClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(ReticleRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Capture",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ControlBtnBg)
                .clickable(onClick = onFlashToggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFlashOn) Icons.Default.FlashAuto else Icons.Default.FlashOff,
                contentDescription = "Toggle Flash",
                tint = if (isFlashOn) Color.Yellow else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onImageSaved: (Uri) -> Unit
) {
    if (imageCapture == null) return
    val fileName = "Receipt_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
    val photoFile = File(context.cacheDir, fileName)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                onImageSaved(savedUri)
            }

            override fun onError(exc: ImageCaptureException) {
                photoFile.delete()
                Toast.makeText(context, AppR.string.camera_capture_failed, Toast.LENGTH_SHORT).show()
            }
        }
    )
}
