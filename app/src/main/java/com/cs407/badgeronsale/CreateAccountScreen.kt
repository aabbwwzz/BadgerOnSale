package com.cs407.badgeronsale

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Color palette reused from the Sign In screen to keep branding consistent
private val BadgerRed = Color(0xFFC5050C)
private val LightBackground = Color(0xFFF2F2F2)
private val InputGrey = Color(0xFFE3E1E1)

/**
 * CreateAccountScreen
 *
 * UI for creating a new BadgerOnSale account.
 * For Checkpoint 2, this screen:
 *  - Validates basic fields (name, UW email, password, phone)
 *  - Shows inline error messages
 *  - Calls [onAccountCreated] when validation passes (for now, fake success)
 *
 * Navigation:
 *  - [onBackToSignInClick] is called when user taps "Sign in" button
 *  - [onAccountCreated] is called after a successful validation
 */
@Composable
fun CreateAccountScreen(
    onBackToSignInClick: () -> Unit = {},
    onAccountCreated: () -> Unit = {}
) {
    // Text field state
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Per-field error messages
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // General error if we want to show non-field specific messages
    var generalError by remember { mutableStateOf<String?>(null) }

    // Toggles password visibility ("Show"/"Hide" text)
    var passwordVisible by remember { mutableStateOf(false) }

    // Used to move focus between fields when user taps "Next" on keyboard
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // Main vertical column content with scroll support
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App logo at top (must exist in drawable resources)
            Image(
                painter = painterResource(id = R.drawable.cs407logo),
                contentDescription = "BadgerOnSale logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // App name
            Text(
                text = "BadgerOnSale",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            // Short tagline under logo
            Text(
                text = "Buy & sell on campus - safer, faster.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Card that contains the entire "Sign Up" form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {

                    // Card title
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ------------------------
                    // Name field
                    // ------------------------
                    Text("Name", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null          // clear error when user types
                            generalError = null
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputGrey, RoundedCornerShape(20.dp)),
                        isError = nameError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputGrey,
                            focusedContainerColor = InputGrey,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    // Inline error under name field
                    if (nameError != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(nameError!!, color = BadgerRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ------------------------
                    // UW Email field
                    // ------------------------
                    Text("UW Email", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null         // clear error when user types
                            generalError = null
                        },
                        singleLine = true,
                        placeholder = { Text("user@wisc.edu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputGrey, RoundedCornerShape(20.dp)),
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputGrey,
                            focusedContainerColor = InputGrey,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    // Inline error under email field
                    if (emailError != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(emailError!!, color = BadgerRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ------------------------
                    // Password field
                    // ------------------------
                    Text("Password", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null      // clear error when user types
                            generalError = null
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputGrey, RoundedCornerShape(20.dp)),
                        isError = passwordError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        // Toggle between hidden and visible password text
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            // Simple "Show"/"Hide" text button instead of eye icon
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputGrey,
                            focusedContainerColor = InputGrey,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    // Inline error under password field
                    if (passwordError != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(passwordError!!, color = BadgerRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ------------------------
                    // Phone Number field
                    // ------------------------
                    Text("Phone Number", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null         // clear error when user types
                            generalError = null
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputGrey, RoundedCornerShape(20.dp)),
                        isError = phoneError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputGrey,
                            focusedContainerColor = InputGrey,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    // Inline error under phone field
                    if (phoneError != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(phoneError!!, color = BadgerRed, style = MaterialTheme.typography.bodySmall)
                    }

                    // Optional spot for non-field specific error messages
                    if (generalError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(generalError!!, color = BadgerRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // -------------------------------------------------
                    // "Create account" button – validates all fields
                    // -------------------------------------------------
                    Button(
                        onClick = {
                            var hasError = false

                            // Basic validation rules
                            if (name.isBlank()) {
                                nameError = "Name cannot be empty."
                                hasError = true
                            }
                            if (email.isBlank()) {
                                emailError = "Email cannot be empty."
                                hasError = true
                            } else if (!email.endsWith("@wisc.edu", ignoreCase = true)) {
                                emailError = "Please use your @wisc.edu email address."
                                hasError = true
                            }
                            if (password.isBlank()) {
                                passwordError = "Password cannot be empty."
                                hasError = true
                            }
                            if (phone.isBlank()) {
                                phoneError = "Phone number cannot be empty."
                                hasError = true
                            }

                            // If all checks pass, treat this as a successful sign-up
                            if (!hasError) {
                                // Milestone 1 behavior: no real backend yet
                                // Just notify the caller that account creation "succeeded"
                                onAccountCreated()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BadgerRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Create account", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // -------------------------------------------------
                    // "Sign in" button – navigates back to login screen
                    // -------------------------------------------------
                    OutlinedButton(
                        onClick = onBackToSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = InputGrey,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Sign in", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
