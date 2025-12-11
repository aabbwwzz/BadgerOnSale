package com.cs407.badgeronsale

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val BadgerRed = Color(0xFFC5050C)
private val LightBg   = Color(0xFFF2F2F2)
private val InputGrey = Color(0xFFE3E1E1)

@Composable
fun SignInScreen(
    onCreateAccountClick: () -> Unit = {},
    onSignInSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(LightBg).statusBarsPadding().navigationBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.cs407logo),
                contentDescription = "BadgerOnSale logo",
                modifier = Modifier.size(140.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "BadgerOnSale",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black, textAlign = TextAlign.Center
            )
            Text(
                text = "Buy & sell on campus - safer, faster.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("UW Email", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null; generalError = null },
                        placeholder = { Text("user@wisc.edu") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputGrey, RoundedCornerShape(20.dp)),
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focus.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputGrey,
                            focusedContainerColor = InputGrey,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    if (emailError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(emailError!!, color = BadgerRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Password", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null; generalError = null },
                        placeholder = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputGrey, RoundedCornerShape(20.dp)),
                        isError = passwordError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                color = Color.DarkGray,
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }.padding(end = 8.dp)
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
                    if (passwordError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(passwordError!!, color = BadgerRed, style = MaterialTheme.typography.bodySmall)
                    }

                    if (generalError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(generalError!!, color = BadgerRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            var hasError = false
                            if (email.isBlank()) { emailError = "Email cannot be empty."; hasError = true }
                            else if (!email.endsWith("@wisc.edu", true)) { emailError = "Please use your @wisc.edu email address."; hasError = true }
                            if (password.isBlank()) { passwordError = "Password cannot be empty."; hasError = true }

                            if (!hasError && !isLoading) {
                                isLoading = true
                                generalError = null
                                coroutineScope.launch {
                                    val result = FirebaseAuthHelper.signIn(email, password)
                                    isLoading = false
                                    result.onSuccess {
                                        onSignInSuccess()
                                    }.onFailure { exception ->
                                        generalError = when {
                                            exception.message?.contains("invalid-credential") == true ||
                                            exception.message?.contains("wrong-password") == true ||
                                            exception.message?.contains("user-not-found") == true ->
                                                "Incorrect email or password."
                                            exception.message?.contains("network") == true ->
                                                "Network error. Please check your connection."
                                            else -> "Sign in failed: ${exception.message}"
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BadgerRed, contentColor = Color.White),
                        enabled = !isLoading
                    ) { 
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign in", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onCreateAccountClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = InputGrey, contentColor = Color.Black
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(false)
                    ) { Text("Create an account", style = MaterialTheme.typography.titleMedium) }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
