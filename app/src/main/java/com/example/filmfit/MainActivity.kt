package com.example.filmfit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.delay

import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.login.LoginRequest
import com.example.filmfit.data.models.login.LoginResponse
import com.example.filmfit.data.models.register.RegisterRequest
import com.example.filmfit.data.models.register.RegisterResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmFitTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    var isSplashVisible by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(if (getLoginState(context)) "mainActivity" else "register") }

    LaunchedEffect(Unit) {
        delay(2000)
        isSplashVisible = false
    }

    if (isSplashVisible) {
        SplashScreen()
    } else {
        when (currentScreen) {
            "mainActivity" -> {
                val intent = Intent(context, MainScreenActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()  // Закриває поточну активність
            }
            "register" -> RegistrationScreen(
                onRegister = { name, email, password ->
                    saveLoginState(context, true)
                    currentScreen = "mainActivity"
                },
                onNavigateToLogin = { currentScreen = "login" }
            )
            "login" -> LoginScreen(
                onLogin = { email, password ->
                    saveLoginState(context, true)
                    currentScreen = "mainActivity"
                },
                onNavigateToRegister = { currentScreen = "register" }
            )
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}



@Composable
fun RegistrationScreen(
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Реєстрація",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = name.value,
                onValueChange = {
                    if (it.matches(Regex("^[a-zA-Z._-]*\$"))) {
                        name.value = it
                        errorMessage.value = null
                    } else {
                        errorMessage.value = "Ім'я користувача може містити тільки англійські літери, '.', '-', '_'"
                    }
                },
                label = { Text("Ім'я користувача") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Електронна пошта") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = { Text("Підтвердити пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            errorMessage.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (password.value != confirmPassword.value) {
                        errorMessage.value = "Паролі не співпадають"
                    } else if (password.value.length < 6) {
                        errorMessage.value = "Пароль має бути щонайменше 6 символів"
                    } else {
                        errorMessage.value = null
                        registerUser(name.value, email.value, password.value, apiService, errorMessage)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Зареєструватися")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ви вже маєте акаунт?",
                color = Color.Blue,
                modifier = Modifier.clickable { onNavigateToLogin() },
                fontSize = 16.sp
            )
        }
    }
}

fun registerUser(
    username: String,
    email: String,
    password: String,
    apiService: ApiService,
    errorMessage: MutableState<String?>
) {
    val request = RegisterRequest(username, email, password)

    apiService.register(request).enqueue(object : Callback<RegisterResponse> {
        override fun onResponse(
            call: Call<RegisterResponse>,
            response: Response<RegisterResponse>
        ) {
            if (response.isSuccessful) {
                Log.d("Registration", "Response body: ${response.body()}")
                Log.d("Registration", "Успішно зареєстровано: ${response.body()?.message}")
                errorMessage.value = "Реєстрація успішна!"
            } else {
                errorMessage.value = "Помилка сервера"
                Log.e("Registration", "Помилка відповіді: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
            Log.e("Registration", "Помилка: ${t.message}")
            errorMessage.value = "Не вдалося підключитися до сервера"
        }
    })
}

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current  // Get the context here
    val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Вхід",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Електронна пошта") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            errorMessage.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    loginUser(context, email.value, password.value, apiService, errorMessage) {
                        onLogin(email.value, password.value) // передаємо результат у головну активність
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Увійти")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Не маєте акаунту?",
                color = Color.Blue,
                modifier = Modifier.clickable { onNavigateToRegister() },
                fontSize = 16.sp
            )
        }
    }
}

fun loginUser(
    context: Context,
    email: String,
    password: String,
    apiService: ApiService,
    errorMessage: MutableState<String?>,
    onSuccess: () -> Unit
) {
    val request = LoginRequest(email, password)

    apiService.login(request).enqueue(object : Callback<LoginResponse> {
        override fun onResponse(
            call: Call<LoginResponse>,
            response: Response<LoginResponse>
        ) {
            if (response.isSuccessful) {
                Log.d("Login", "Response body: ${response.body()}")
                val token = response.body()?.token
                if (token != null) {
                    saveAuthToken(context, token)  // Використовуємо context
                    Log.d("Login", "Token saved: $token")
                }
                Log.d("Login", "Успішно увійшли: ${response.body()?.message}")
                errorMessage.value = "Вхід успішний!"
                onSuccess()
            } else {
                errorMessage.value = "Помилка сервера"
                Log.e("Login", "Помилка відповіді: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
            Log.e("Login", "Помилка: ${t.message}")
            errorMessage.value = "Не вдалося підключитися до сервера"
        }
    })
}

fun saveAuthToken(context: Context, token: String) {
    val sharedPreferences = context.getSharedPreferences("FilmFitPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("auth_token", token).apply()
    Log.d("Token", "Token saved: $token")
}


@Composable
fun MainScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Main Screen",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

fun saveLoginState(context: Context, isLoggedIn: Boolean) {
    val sharedPref = context.getSharedPreferences("FilmFitPrefs", Context.MODE_PRIVATE)
    sharedPref.edit().putBoolean("isUserLoggedIn", isLoggedIn).apply()
}

fun getLoginState(context: Context): Boolean {
    val sharedPref = context.getSharedPreferences("FilmFitPrefs", Context.MODE_PRIVATE)
    return sharedPref.getBoolean("isUserLoggedIn", false)
}
