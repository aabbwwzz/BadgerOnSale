package com.cs407.badgeronsale

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.io.File
import com.cs407.badgeronsale.Base64Image

// Public so it can be used in callbacks without visibility warnings.
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val graduationYear: String,
    val address: String,
    @DrawableRes val avatarRes: Int = R.drawable.avatar,
    val profilePicUrl: String? = null  // Firebase Storage URL for profile picture
)

// Palette to match your app
private val BadgerRed = Color(0xFFC5050C)
private val LightBg = Color(0xFFF2F2F2)
private val CardBg = Color(0xFFFFFFFF)
private val InputBg = Color(0xFFEAE8E8)
private val CancelBg = Color(0xFFF4F1E6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    initial: UserProfile = UserProfile(
        name = "Jouhara Ali",
        email = "user@wisc.edu",
        phone = "(608) 555-1234",
        graduationYear = "2026",
        address = "123 University Ave",
        // Use any drawable you have (e.g., avatar.png or cs407logo)
        avatarRes = R.drawable.avatar
    ),
    onBack: () -> Unit = {},
    onSave: (UserProfile) -> Unit = {},
    onCancel: () -> Unit = {},
    onChangePhoto: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initial.name) }
    var email by remember { mutableStateOf(initial.email) }
    var phone by remember { mutableStateOf(initial.phone) }
    var gradYear by remember { mutableStateOf(initial.graduationYear) }
    var address by remember { mutableStateOf(initial.address) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profilePicUrl by remember { mutableStateOf<String?>(initial.profilePicUrl) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoadingPhoto by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

    var emailError by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Image picker launcher (gallery)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            // Upload to Firebase Storage
            isLoadingPhoto = true
            photoError = null
            coroutineScope.launch {
                val uploadResult = FirebaseStorageHelper.uploadProfilePicture(context, uri)
                isLoadingPhoto = false
                if (uploadResult.isSuccess) {
                    profilePicUrl = uploadResult.getOrNull()
                } else {
                    photoError = "Failed to upload photo: ${uploadResult.exceptionOrNull()?.message}"
                }
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraImageUri
        if (success && uri != null) {
            selectedImageUri = uri
            // Upload to Firebase Storage
            isLoadingPhoto = true
            photoError = null
            coroutineScope.launch {
                val uploadResult = FirebaseStorageHelper.uploadProfilePicture(context, uri)
                isLoadingPhoto = false
                if (uploadResult.isSuccess) {
                    profilePicUrl = uploadResult.getOrNull()
                } else {
                    photoError = "Failed to upload photo: ${uploadResult.exceptionOrNull()?.message}"
                }
            }
        }
    }

    // Permission launchers
    val cameraPermissionLauncher = rememberCameraPermissionLauncher(
        onPermissionGranted = {
            val imageFile = File(context.cacheDir, "profile_camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        },
        onPermissionDenied = {
            photoError = "Camera permission is required to take photos."
        }
    )

    val storagePermissionLauncher = rememberStoragePermissionLauncher(
        onPermissionGranted = {
            imagePickerLauncher.launch("image/*")
        },
        onPermissionDenied = {
            photoError = "Storage permission is required to select photos."
        }
    )

    // Function to handle image source selection
    fun handleImageSourceClick() {
        if (PermissionHelper.hasCameraPermission(context) && PermissionHelper.hasStoragePermission(context)) {
            showImageSourceDialog = true
        } else if (!PermissionHelper.hasCameraPermission(context)) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        } else if (!PermissionHelper.hasStoragePermission(context)) {
            storagePermissionLauncher.launch(PermissionHelper.getStoragePermission())
        } else {
            showImageSourceDialog = true
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp, color = CardBg, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    // Home icon on the right (matching wireframe)
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
                .padding(padding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (profilePicUrl != null) {
                    // Use Base64Image for more reliable data URL loading
                    Base64Image(
                        dataUrl = profilePicUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = {
                            Image(
                                painter = painterResource(R.drawable.avatar),
                                contentDescription = "Profile photo placeholder",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                } else if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(selectedImageUri)
                                .build()
                        ),
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(initial.avatarRes),
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
                if (isLoadingPhoto) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = BadgerRed,
                        strokeWidth = 3.dp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "CHANGE PHOTO",
                color = BadgerRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { handleImageSourceClick() }
                    .padding(4.dp)
            )
            if (photoError != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = photoError!!,
                    color = BadgerRed,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Card with inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                        .fillMaxWidth()
                ) {
                    LabeledField(
                        label = "Name",
                        value = name,
                        onValueChange = { name = it }
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "UW Email",
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        isError = emailError != null,
                        supportingText = emailError
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Phone Number",
                        value = phone,
                        onValueChange = { phone = it },
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Graduation Year",
                        value = gradYear,
                        onValueChange = { gradYear = it },
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledField(
                        label = "Address",
                        value = address,
                        onValueChange = { address = it },
                        imeAction = ImeAction.Done
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // very light validation
                        if (!email.endsWith("@wisc.edu", ignoreCase = true)) {
                            emailError = "Please use your @wisc.edu email."
                            return@Button
                        }
                        onSave(
                            UserProfile(
                                name = name.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                graduationYear = gradYear.trim(),
                                address = address.trim(),
                                avatarRes = initial.avatarRes,
                                profilePicUrl = profilePicUrl
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BadgerRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("SAVE", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CancelBg,  // Light beige/off-white
                        contentColor = Color.Black
                    )
                ) {
                    Text("CANCEL", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = {
                Text(
                    text = "Select Image Source",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Camera option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImageSourceDialog = false
                                if (PermissionHelper.hasCameraPermission(context)) {
                                    val imageFile = File(context.cacheDir, "profile_camera_${System.currentTimeMillis()}.jpg")
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        imageFile
                                    )
                                    cameraImageUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = BadgerRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Take Photo",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Divider()
                    
                    // Gallery option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImageSourceDialog = false
                                if (PermissionHelper.hasStoragePermission(context)) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    storagePermissionLauncher.launch(PermissionHelper.getStoragePermission())
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = BadgerRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Choose from Gallery",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Cancel", color = BadgerRed)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isError: Boolean = false,
    supportingText: String? = null
) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        shape = RoundedCornerShape(20.dp),
        isError = isError,
        supportingText = {
            if (supportingText != null) {
                Text(
                    supportingText,
                    color = BadgerRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(InputBg, RoundedCornerShape(20.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = InputBg,
            focusedContainerColor = InputBg,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        )
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    MaterialTheme {
        EditProfileScreen(
            onBack = {},
            onSave = {},
            onCancel = {}
        )
    }
}
