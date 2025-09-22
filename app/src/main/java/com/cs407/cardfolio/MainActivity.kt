package com.cs407.cardfolio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cs407.cardfolio.ui.theme.AppTheme
import com.cs407.cardfolio.ui.theme.CardfolioTheme

// Main entry point of the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables drawing behind system bars for immersive UI
        enableEdgeToEdge()

        // Sets the UI content using Jetpack Compose
        setContent {
            CardfolioTheme {
                val gradientTopColor = AppTheme.customColors.gradientTop
                val gradientBottomColor = AppTheme.customColors.gradientBottom

                // Root surface with gradient background
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    gradientTopColor,
                                    gradientBottomColor
                                )
                            )
                        ),
                    color = Color.Transparent
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // App title
                        Text(
                            text = stringResource(id = R.string.app_title),
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                        )

                        // Main card UI
                        Cardfolio()
                    }
                }
            }
        }
    }
}

@Composable
fun Cardfolio() {
    // State variables for user inputs
    var name by remember { mutableStateOf("") }
    var hobby by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    // Tracks edit mode (true = editable, false = locked)
    var isEditing by remember { mutableStateOf(true) }

    // Controls visibility of the hint banner
    var showHintBanner by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val outlineColor = MaterialTheme.colorScheme.outline

    // Custom text field colors for read-only state
    val readOnlyTextFieldColors = OutlinedTextFieldDefaults.colors(
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outline,
    )

    // Auto-hide banner after 3 seconds when shown
    if (showHintBanner) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            showHintBanner = false
        }
    }

    // Show hint banner when editing is finished with valid data
    LaunchedEffect(isEditing) {
        if (!isEditing && (name.isNotBlank() && hobby.isNotBlank() && age.isNotBlank())) {
            showHintBanner = true
        }
    }

    // Main card container
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            // Card header: profile image + name + hobby + edit/lock chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile image with circular border
                Image(
                    painter = painterResource(id = R.drawable.download),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .border(1.dp, outlineColor, CircleShape)
                )
                Spacer(Modifier.width(16.dp))

                // Displays name & hobby or fallback text
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (name.isBlank()) stringResource(id = R.string.card_name) else name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (hobby.isBlank()) stringResource(id = R.string.card_hobby) else hobby,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Toggle between Edit and Lock states
                AssistChip(
                    onClick = {
                        isEditing = !isEditing
                    },
                    label = {
                        val statusTextResId =
                            if (isEditing) R.string.editing_status else R.string.locked_status
                        Text(stringResource(id = statusTextResId))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Divider between header and fields
            HorizontalDivider(color = outlineColor)

            // Input fields and actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.card_name_label)) },
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = readOnlyTextFieldColors
                )

                // Hobby input
                OutlinedTextField(
                    value = hobby,
                    onValueChange = { hobby = it },
                    label = { Text(stringResource(id = R.string.card_hobby_label)) },
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = readOnlyTextFieldColors
                )

                // Age input (only allows digits)
                OutlinedTextField(
                    value = age,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) { age = input }
                    },
                    label = { Text(stringResource(id = R.string.card_age_label)) },
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { if (isEditing) Text(stringResource(id = R.string.age_warning)) },
                    colors = readOnlyTextFieldColors
                )

                Spacer(Modifier.height(4.dp))

                // Action buttons (Edit and Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Re-enable editing when locked
                    OutlinedButton(
                        onClick = { isEditing = true },
                        enabled = !isEditing
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.button_edit))
                    }

                    Spacer(Modifier.width(8.dp))

                    // Save button with validation
                    Button(
                        onClick = {
                            val missing = buildList {
                                if (name.isBlank()) add("Name")
                                if (hobby.isBlank()) add("Hobby")
                                if (age.isBlank()) add("Age")
                            }

                            if (missing.isNotEmpty()) {
                                // Warn user about missing fields
                                Toast.makeText(
                                    context,
                                    "Please enter: ${missing.joinToString(", ")}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Save success → lock form
                                isEditing = false
                                Toast.makeText(
                                    context,
                                    "Saved successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = isEditing
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.button_show))
                    }
                }
            }
        }
    }
}

// Preview for Compose UI in Android Studio
@Preview(showBackground = true)
@Composable
fun CardfolioPreview() {
    CardfolioTheme {
        Cardfolio()
    }
}
