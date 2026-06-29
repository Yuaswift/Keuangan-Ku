package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserAccount

enum class AuthScreenState {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

@Composable
fun AuthRootScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var screenState by remember { mutableStateOf(AuthScreenState.LOGIN) }
    var focusedResetEmail by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        AnimatedContent(
            targetState = screenState,
            transitionSpec = {
                slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width } + fadeIn() togetherWith
                slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width } + fadeOut()
            },
            label = "auth_screen_transition"
        ) { state ->
            when (state) {
                AuthScreenState.LOGIN -> {
                    LoginScreen(
                        onLoginClick = { email, pwd ->
                            viewModel.login(email, pwd)
                        },
                        onRegisterNavigate = {
                            screenState = AuthScreenState.REGISTER
                        },
                        onForgotPasswordNavigate = { email ->
                            focusedResetEmail = email
                            screenState = AuthScreenState.FORGOT_PASSWORD
                        }
                    )
                }
                AuthScreenState.REGISTER -> {
                    RegisterScreen(
                        onRegisterClick = { fullName, email, pwd, question, answer ->
                            viewModel.register(fullName, email, pwd, question, answer)
                        },
                        onLoginNavigate = {
                            screenState = AuthScreenState.LOGIN
                        }
                    )
                }
                AuthScreenState.FORGOT_PASSWORD -> {
                    ForgotPasswordScreen(
                        initialEmail = focusedResetEmail,
                        onGetQuestion = { email ->
                            viewModel.getSecurityQuestion(email)
                        },
                        onResetPassword = { email, answer, newPwd ->
                            viewModel.resetPassword(email, answer, newPwd)
                        },
                        onBackToLogin = {
                            screenState = AuthScreenState.LOGIN
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Boolean,
    onRegisterNavigate: () -> Unit,
    onForgotPasswordNavigate: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large stylized Finance icon or custom banner
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "KeuanganKu Logo",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "KeuanganKu",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Kelola finansial harian Anda secara terarah",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Masuk Akun",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("E-mail") },
                    placeholder = { Text("contoh@Email.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Star else Icons.Default.Lock,
                                contentDescription = if (showPassword) "Sembunyikan sandi" else "Tampilkan sandi"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Lupa Password?",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onForgotPasswordNavigate(email) }
                            .padding(vertical = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        if (trimmedEmail.isEmpty()) {
                            errorMessage = "Email tidak boleh kosong"
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                            errorMessage = "Format email tidak valid"
                        } else if (password.isEmpty()) {
                            errorMessage = "Password tidak boleh kosong"
                        } else {
                            val success = onLoginClick(trimmedEmail, password)
                            if (!success) {
                                errorMessage = "Email atau password salah"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Masuk", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Belum punya akun? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Daftar Sekarang",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onRegisterNavigate() }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String, String, String) -> Boolean,
    onLoginNavigate: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Security Question
    var selectedQuestionIndex by remember { mutableIntStateOf(0) }
    var securityAnswer by remember { mutableStateOf("") }
    var isExpandedQuestionDrop by remember { mutableStateOf(false) }

    val safetyQuestions = listOf(
        "Siapa nama hewan peliharaan pertama Anda?",
        "Apa nama sekolah dasar (SD) Anda dahulu?",
        "Di kota manakah orang tua Anda bertemu?",
        "Siapa nama guru favorit Anda di SMA?",
        "Apa warna favorit Anda saat kecil?"
    )

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Daftar Akun Baru",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Isi data berikut untuk mulai menabung terarah",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                if (successMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = successMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // Full Name Input
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = null
                    },
                    label = { Text("Nama Lengkap") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Name",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("E-mail") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Confirm Password Input
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Konfirmasi Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Security Question Block
                Text(
                    text = "Pertanyaan Keamanan (Pemulihan Password)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = safetyQuestions[selectedQuestionIndex],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pertanyaan") },
                        trailingIcon = {
                            IconButton(onClick = { isExpandedQuestionDrop = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih pertanyaan"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpandedQuestionDrop = true },
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = isExpandedQuestionDrop,
                        onDismissRequest = { isExpandedQuestionDrop = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        safetyQuestions.forEachIndexed { idx, q ->
                            DropdownMenuItem(
                                text = { Text(q, fontSize = 13.sp) },
                                onClick = {
                                    selectedQuestionIndex = idx
                                    isExpandedQuestionDrop = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = securityAnswer,
                    onValueChange = {
                        securityAnswer = it
                        errorMessage = null
                    },
                    label = { Text("Jawaban Anda") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Jawaban",
							tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    placeholder = { Text("Jawaban pemulihan sandi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val cleanName = fullName.trim()
                        val cleanEmail = email.trim()
                        val cleanAnswer = securityAnswer.trim()
                        
                        if (cleanName.isEmpty() || cleanEmail.isEmpty() || password.isEmpty() || cleanAnswer.isEmpty()) {
                            errorMessage = "Mohon lengkapi semua isian di atas"
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                            errorMessage = "Format email tidak valid"
                        } else if (password.length < 4) {
                            errorMessage = "Password minimal terdiri dari 4 karakter"
                        } else if (password != confirmPassword) {
                            errorMessage = "Konfirmasi password tidak cocok"
                        } else {
                            val success = onRegisterClick(
                                cleanName,
                                cleanEmail,
                                password,
                                safetyQuestions[selectedQuestionIndex],
                                cleanAnswer
                            )
                            if (success) {
                                successMessage = "Pendaftaran berhasil! Mengalihkan..."
                                errorMessage = null
                                // Auto navigate to login after delay
                                // delegated to onBackToLogin eventually, let's keep it simple
                                onLoginNavigate()
                            } else {
                                errorMessage = "Email sudah terdaftar. Silakan masuk."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Daftar Akun Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sudah punya akun? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Masuk Di Sini",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onLoginNavigate() }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ForgotPasswordScreen(
    initialEmail: String,
    onGetQuestion: (String) -> String?,
    onResetPassword: (String, String, String) -> Boolean,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var step by remember { mutableIntStateOf(1) } // 1: Input Email, 2: Answer safety & reset password
    
    var securityQuestion by remember { mutableStateOf<String?>(null) }
    var securityAnswer by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Lupa Password",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pulihkan akses akun Anda dengan pertanyaan keamanan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                if (successMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = successMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                if (step == 1) {
                    Text(
                        text = "Langkah 1: Verifikasi E-mail",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Alamat E-mail") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        placeholder = { Text("contoh@Email.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val cleanEmail = email.trim()
                            if (cleanEmail.isEmpty()) {
                                errorMessage = "Mohon masukkan email Anda"
                            } else {
                                val question = onGetQuestion(cleanEmail)
                                if (question != null) {
                                    securityQuestion = question
                                    step = 2
                                    errorMessage = null
                                } else {
                                    errorMessage = "E-mail tidak terdaftar dalam sistem kami"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Lanjut Pemulihan", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Langkah 2: Verifikasi Keamanan",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Display security question
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Pertanyaan Keamanan Anda:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = securityQuestion ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = securityAnswer,
                        onValueChange = {
                            securityAnswer = it
                            errorMessage = null
                        },
                        label = { Text("Jawaban Keamanan") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Jawaban",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        placeholder = { Text("Tulis jawaban saat pendaftaran") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("Password Baru") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password baru",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = {
                            confirmNewPassword = it
                            errorMessage = null
                        },
                        label = { Text("Konfirmasi Password Baru") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Konfirmasi password baru",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val cleanAnswer = securityAnswer.trim()
                            if (cleanAnswer.isEmpty() || newPassword.isEmpty()) {
                                errorMessage = "Semua kolom wajib diisi"
                            } else if (newPassword.length < 4) {
                                errorMessage = "Password baru minimal terdiri dari 4 karakter"
                            } else if (newPassword != confirmNewPassword) {
                                errorMessage = "Konfirmasi password baru tidak cocok"
                            } else {
                                val success = onResetPassword(email.trim(), cleanAnswer, newPassword)
                                if (success) {
                                    successMessage = "Password berhasil diperbarui! Silakan Masuk."
                                    errorMessage = null
                                    // Navigate to log in screen after success
                                    onBackToLogin()
                                } else {
                                    errorMessage = "Jawaban keamanan salah. Mohon ulangi."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Perbarui Sandi & Masuk", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Kembali ke Masuk",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Kembali ke Halaman Masuk", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
