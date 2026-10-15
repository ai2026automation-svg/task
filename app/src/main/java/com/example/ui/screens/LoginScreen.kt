package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LineBorder
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.StatusDelay
import com.example.ui.theme.StatusOk
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun LoginScreen(
    email: String,
    code: String,
    step: Int,
    msg: String,
    isError: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onRequestCode: () -> Unit,
    onVerifyCode: () -> Unit,
    onBackToEmail: () -> Unit,
    onOpenServerConfig: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Top Server Config icon
        IconButton(
            onClick = onOpenServerConfig,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .testTag("btn_login_settings")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Apps Script Server Settings",
                tint = NavyPrimary
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand Header Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyPrimary)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SMARTFIN",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "CFO",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = OrangePrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Task Manager",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Text(
                    text = if (step == 1) "Sign in with your authorized email" else "Enter the verification code",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                if (step == 1) {
                    // Step 1: Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address") },
                        placeholder = { Text("you@company.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onRequestCode() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = LineBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_email")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onRequestCode,
                        enabled = !isLoading && email.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_send_code")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Send Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Predefined Quick-Login Hints for instant demo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF7F9FC))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Quick Demo Accounts:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { onEmailChange("automation@starterscfo.in") },
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Text("Admin (Avadhesh)", fontSize = 11.sp, color = OrangePrimary)
                            }
                            TextButton(
                                onClick = { onEmailChange("excelwithavi@gmail.com") },
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Text("User (Amit)", fontSize = 11.sp, color = OrangePrimary)
                            }
                        }
                    }

                } else {
                    // Step 2: 6-Digit OTP Code
                    Text(
                        text = "Code sent to $email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) onCodeChange(it.filter { c -> c.isDigit() }) },
                        label = { Text("6-Digit Code") },
                        placeholder = { Text("------") },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = OrangePrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onVerifyCode() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = LineBorder
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_code")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onVerifyCode,
                        enabled = !isLoading && code.length == 6,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_verify_code")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Verify & Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onBackToEmail,
                        modifier = Modifier.testTag("btn_back_to_email")
                    ) {
                        Text(
                            text = "← Use a different email",
                            color = OrangePrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status or Error Message
                if (msg.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = msg,
                        color = if (isError) StatusDelay else StatusOk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("login_status_msg")
                    )
                }
            }
        }
    }
}
