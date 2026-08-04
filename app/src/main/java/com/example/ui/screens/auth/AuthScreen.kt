package com.example.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ShaghafButton
import com.example.ui.components.ShaghafTextField
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.PrimaryAccentVariant
import com.example.ui.theme.SecondaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AuthViewModel

import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.components.ShaghafLogo

import com.example.ui.components.shaghafGradientBackground

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Register

    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    var regUsername by remember { mutableStateOf("") }
    var regDisplayName by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    val loginError by viewModel.loginError.collectAsState()
    val registerError by viewModel.registerError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .shaghafGradientBackground()
    ) {
        // Decorative ambient top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SecondaryAccent.copy(alpha = 0.3f),
                            PrimaryAccent.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo Badge
            ShaghafLogo(size = 72.dp, showText = false)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = stringResource(id = R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Auth Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Custom Tab Row
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = DarkSurfaceCard,
                        contentColor = PrimaryAccent,
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = PrimaryAccent
                                )
                            }
                        },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = {
                                selectedTabIndex = 0
                                viewModel.clearErrors()
                            },
                            text = {
                                Text(
                                    text = stringResource(id = R.string.login),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTabIndex == 0) PrimaryAccent else TextMuted
                                    )
                                )
                            },
                            modifier = Modifier.testTag("tab_login")
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = {
                                selectedTabIndex = 1
                                viewModel.clearErrors()
                            },
                            text = {
                                Text(
                                    text = stringResource(id = R.string.register),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTabIndex == 1) PrimaryAccent else TextMuted
                                    )
                                )
                            },
                            modifier = Modifier.testTag("tab_register")
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (selectedTabIndex == 0) {
                        // LOGIN FORM
                        ShaghafTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            label = stringResource(id = R.string.username),
                            leadingIcon = Icons.Default.Person,
                            testTag = "input_login_username"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ShaghafTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = stringResource(id = R.string.password),
                            isPassword = true,
                            leadingIcon = Icons.Default.Lock,
                            testTag = "input_login_password"
                        )

                        if (loginError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = loginError.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ShaghafButton(
                            text = stringResource(id = R.string.login),
                            isLoading = isSubmitting,
                            onClick = { viewModel.login(loginUsername, loginPassword) },
                            testTag = "btn_submit_login"
                        )
                    } else {
                        // REGISTER FORM
                        ShaghafTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = stringResource(id = R.string.username),
                            leadingIcon = Icons.Default.Person,
                            testTag = "input_reg_username"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ShaghafTextField(
                            value = regDisplayName,
                            onValueChange = { regDisplayName = it },
                            label = stringResource(id = R.string.display_name),
                            leadingIcon = Icons.Default.Person,
                            testTag = "input_reg_display_name"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ShaghafTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = stringResource(id = R.string.password),
                            isPassword = true,
                            leadingIcon = Icons.Default.Lock,
                            testTag = "input_reg_password"
                        )

                        if (registerError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = registerError.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ShaghafButton(
                            text = stringResource(id = R.string.register),
                            isLoading = isSubmitting,
                            onClick = { viewModel.register(regUsername, regPassword, regDisplayName) },
                            testTag = "btn_submit_register"
                        )
                    }
                }
            }
        }
    }
}
