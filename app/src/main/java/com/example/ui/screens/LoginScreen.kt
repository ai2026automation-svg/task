package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AppsScriptClient
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LineBorder
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.StatusDelay
import com.example.ui.theme.StatusOk
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun LoginScreen(
    appsScriptUrl: String,
    email: String,
    code: String,
    step: Int, // 0 = Enter AppsScript URL, 1 = Email, 2 = 6-digit Code
    msg: String,
    isError: Boolean,
    isLoading: Boolean,
    onUrlSubmit: (String) -> Unit,
    onBackToUrl: () -> Unit,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onRequestCode: () -> Unit,
    onVerifyCode: () -> Unit,
    onBackToEmail: () -> Unit,
    onOpenServerConfig: () -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember(appsScriptUrl) { mutableStateOf(appsScriptUrl) }
    var showHelperCode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
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

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Task Manager",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                // Subtitle based on step
                val subtitle = when (step) {
                    0 -> "Step 1/3: Connect Google Apps Script"
                    1 -> "Step 2/3: Sign in with your email"
                    else -> "Step 3/3: Verify 6-digit code"
                }

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // STEP 0: APPS SCRIPT URL FIRST
                if (step == 0) {
                    Text(
                        text = "Enter your Google Apps Script Web App URL first to validate authorized emails directly against your Users sheet:",
                        fontSize = 12.sp,
                        color = Color(0xFF374151),
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Web App URL") },
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = OrangePrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = LineBorder
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (urlInput.isNotBlank()) onUrlSubmit(urlInput)
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_apps_script_url_login")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onUrlSubmit(urlInput) },
                        enabled = urlInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_proceed_to_email")
                    ) {
                        Text(
                            text = "Connect & Continue to Login →",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { onUrlSubmit("") },
                        modifier = Modifier.testTag("btn_skip_url")
                    ) {
                        Text(
                            text = "Skip for now (Use Local / Demo Mode)",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deploy instructions toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF7F9FC))
                            .clickable { showHelperCode = !showHelperCode }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showHelperCode) "Hide doPost(e) snippet" else "How to get Web App URL?",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }

                        if (showHelperCode) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("doPost snippet", AppsScriptClient.CODE_GS_DOPROST_SNIPPET))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = OrangePrimary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    if (showHelperCode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavyPrimary)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "1. In Google Apps Script, Deploy > New deployment > Web app.\n2. Execute as: Me, Who has access: Anyone.\n3. Ensure doPost(e) handles actions (copy snippet below):",
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppsScriptClient.CODE_GS_DOPROST_SNIPPET.trim().take(300) + "\n...",
                                color = Color(0xFFC7D2FE),
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                // STEP 1: EMAIL INPUT
                else if (step == 1) {
                    // Show current URL with change option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF7F9FC))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (appsScriptUrl.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (appsScriptUrl.isNotBlank()) StatusOk else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appsScriptUrl.isNotBlank()) "URL Connected: ${appsScriptUrl.take(28)}…" else "Local Demo Mode",
                                fontSize = 11.sp,
                                color = NavyPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(
                            onClick = onBackToUrl,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Change URL", fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address *") },
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

                    // Demo Accounts Helper
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
                }

                // STEP 2: 6-DIGIT CODE VERIFICATION
                else {
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
