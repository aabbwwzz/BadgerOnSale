package com.cs407.badgeronsale

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Public so it can be used in callbacks without visibility warnings.
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val graduationYear: String,
    val address: String,
    @DrawableRes val avatarRes: Int
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

    var emailError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp, color = CardBg) {
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
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Image(
                painter = painterResource(initial.avatarRes),
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "CHANGE PHOTO",
                color = BadgerRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onChangePhoto?.invoke() }
                    .padding(4.dp)
            )

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
                                avatarRes = initial.avatarRes
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
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CancelBg,
                        contentColor = Color.Black
                    )
                ) {
                    Text("CANCEL", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
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
