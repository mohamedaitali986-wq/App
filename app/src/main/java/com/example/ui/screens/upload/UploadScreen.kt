package com.example.ui.screens.upload

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ui.components.ShaghafButton
import com.example.ui.components.VideoPlayerView
import com.example.ui.components.shaghafGradientBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.UploadViewModel

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onUploadSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val postType by viewModel.type.collectAsState()
    val caption by viewModel.caption.collectAsState()
    val hashtags by viewModel.hashtags.collectAsState()
    val selectedMediaUrl by viewModel.selectedMediaUrl.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadSuccess by viewModel.uploadSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showPermissionRationale by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // 1. Photo Picker Launcher (PickVisualMedia)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.handleSelectedMedia(context, uri)
        }
    }

    // 2. Legacy / Fallback GetContent Launcher
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.handleSelectedMedia(context, uri)
        }
    }

    // 3. Permission Launcher for Fallback
    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (postType == "VIDEO") Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val mimeType = if (postType == "VIDEO") "video/*" else "image/*"
            getContentLauncher.launch(mimeType)
        } else {
            val activity = context.findActivity()
            if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionToRequest)) {
                showPermissionRationale = true
            } else {
                showSettingsDialog = true
            }
        }
    }

    fun launchMediaPicker() {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
            val mediaType = if (postType == "VIDEO") {
                ActivityResultContracts.PickVisualMedia.VideoOnly
            } else {
                ActivityResultContracts.PickVisualMedia.ImageOnly
            }
            photoPickerLauncher.launch(PickVisualMediaRequest(mediaType))
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(context, permissionToRequest)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                val mimeType = if (postType == "VIDEO") "video/*" else "image/*"
                getContentLauncher.launch(mimeType)
            } else {
                val activity = context.findActivity()
                if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionToRequest)) {
                    showPermissionRationale = true
                } else {
                    permissionLauncher.launch(permissionToRequest)
                }
            }
        }
    }

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            viewModel.resetSuccessFlag()
            onUploadSuccess()
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("إذن الوصول للوسائط", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("يحتاج التطبيق لإذن الوصول لمعرض الصور والفيديوهات لتتمكن من اختيار ملف ونشره.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(permissionToRequest)
                    }
                ) {
                    Text("سماح", color = PrimaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("الوصول محظور", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("تم رفض إذن الوصول للملفات. يرجى تفعيله من إعدادات التطبيق لتتمكن من رفع الوسائط.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("فتح الإعدادات", color = PrimaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .shaghafGradientBackground()
            .statusBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "إنشاء منشور جديد",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Text(
            text = "شارك فيديو قصير أو صورة مع المتابعين في شغف",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Post Type Selector Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceCard)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (postType == "VIDEO") PrimaryAccent else Color.Transparent)
                    .clickable { viewModel.setType("VIDEO") }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = if (postType == "VIDEO") Color.White else TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "فيديو قصير",
                        color = if (postType == "VIDEO") Color.White else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (postType == "PHOTO") PrimaryAccent else Color.Transparent)
                    .clickable { viewModel.setType("PHOTO") }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = if (postType == "PHOTO") Color.White else TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "صورة",
                        color = if (postType == "PHOTO") Color.White else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Media Preview / Picker Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { launchMediaPicker() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, PrimaryAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMediaUrl.isNotBlank()) {
                    if (postType == "PHOTO") {
                        AsyncImage(
                            model = selectedMediaUrl,
                            contentDescription = "معاينة الصورة المختارة",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        VideoPlayerView(
                            videoUrl = selectedMediaUrl,
                            isPlaying = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Top Change / Clear Overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تغيير الوسيط ✏️",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.clearSelectedMedia() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "حذف الوسيط",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (postType == "VIDEO") Icons.Default.Movie else Icons.Default.Image,
                            contentDescription = null,
                            tint = PrimaryAccent,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (postType == "VIDEO") "انقر هنا لاختيار فيديو قصير" else "انقر هنا لاختيار صورة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "من معرض الصور أو ملفات الجهاز",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = PrimaryAccent,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري حفظ ونشر المحتوى...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Caption Input Field
        OutlinedTextField(
            value = caption,
            onValueChange = { viewModel.setCaption(it) },
            label = { Text("الوصف أو التعليق (مثال: أحدث تجربة في الرسم اليوم...)", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("input_upload_caption"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hashtags Input Field
        OutlinedTextField(
            value = hashtags,
            onValueChange = { viewModel.setHashtags(it) },
            label = { Text("الوسوم / الوسوم المتعلقة (مثال: #تقنية #فنون #شغف)", color = TextSecondary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_upload_hashtags"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Submit Button
        ShaghafButton(
            text = if (selectedMediaUrl.isBlank()) {
                "اختر وسيطاً للنشر أولاً 📁"
            } else if (postType == "VIDEO") {
                "نشر الفيديو الآن 🚀"
            } else {
                "نشر الصورة الآن 📷"
            },
            isLoading = isUploading,
            enabled = selectedMediaUrl.isNotBlank() && !isUploading,
            onClick = { viewModel.uploadPost() },
            testTag = "btn_submit_upload"
        )
    }
}

