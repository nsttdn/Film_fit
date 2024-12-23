package com.example.filmfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.users.User
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.launch

class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmFitTheme {
                AccountScreen(context = this)
            }
        }
    }
}

@Composable
fun AccountScreen(context: Context) {
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val token = getAuthToken(context)
        if (token == null) {
            errorMessage = "Token not found! Please log in."
            loading = false
        } else {
            coroutineScope.launch {
                user = fetchUserInfo(apiService, token)
                if (user == null) {
                    errorMessage = "Failed to load user information."
                }
                loading = false
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = rememberNavController()) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
            } else {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "An error occurred.",
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    AccountContent(user = user, context = context)
                }
            }
        }
    }
}

@Composable
fun AccountContent(user: User?, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = " ${user?.username ?: "User"}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            StatisticCard(label = "Followers", count = user?.followersCount ?: 0)
            StatisticCard(label = "Following", count = user?.followingCount ?: 0)
        }

        Button(
            onClick = { logOutUser(context) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Log Out")
        }

        Button(
            onClick = {
                val intent = Intent(context, SuggestionsUserFilmActivity::class.java)
                context.startActivity(intent)
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Suggestions Films")
        }

        Button(
            onClick = {
                val intent = Intent(context, WishlistActivity::class.java)
                context.startActivity(intent)
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("View Wishlist")
        }
    }
}

@Composable
fun StatisticCard(label: String, count: Int) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        modifier = Modifier.size(120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = label, fontSize = 14.sp)
        }
    }
}

fun logOutUser(context: Context) {
    val sharedPreferences = context.getSharedPreferences("FilmFitPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.clear()
    editor.apply()

    val intent = Intent(context, MainActivity::class.java)
    context.startActivity(intent)
    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
}

fun getAuthToken(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("FilmFitPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("auth_token", null)
}

suspend fun fetchUserInfo(apiService: ApiService, token: String): User? {
    return try {
        val response = apiService.getUserInfo("Bearer $token")
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.e("fetchUserInfo", "Failed to fetch user info: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.e("fetchUserInfo", "Error fetching user info: ${e.localizedMessage}")
        null
    }
}