package com.example.filmfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.Pageable
import com.example.filmfit.data.models.films.Film
import com.example.filmfit.data.models.films.FilmResponse
import com.example.filmfit.data.models.users.User
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmFitTheme {
                SearchScreen()
            }
        }
    }
}

@Composable
fun SearchScreen() {
    val context = LocalContext.current
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    var query by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var films by remember { mutableStateOf<List<Film>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Пошук", fontWeight = FontWeight.Bold) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Введіть пошуковий запит") },
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = KeyboardActions(onDone = {
                    coroutineScope.launch {
                        loading = true
                        searchUsers(apiService, query) { users = it }
                        searchFilms(apiService, query) { films = it }
                        loading = false
                    }
                })
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    users = emptyList()
                    films = emptyList()
                    loading = true
                    coroutineScope.launch {
                        searchUsers(apiService, query) { users = it }
                        loading = false
                    }
                }) {
                    Text("Шукати користувачів")
                }
                Button(onClick = {
                    users = emptyList()
                    films = emptyList()
                    loading = true
                    coroutineScope.launch {
                        searchFilms(apiService, query) { films = it }
                        loading = false
                    }
                }) {
                    Text("Шукати фільми")
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    if (users.isNotEmpty()) {
                        item {
                            Text(
                                text = "Користувачі",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(users.size) { index ->
                            val user = users[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = user.username, fontSize = 16.sp)
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            subscribeToUser(getAuthToken(context), apiService, context, user.id) {}
                                        }
                                    }) {
                                        Text("Підписатися")
                                    }
                                }
                            }
                        }
                    }

                    if (films.isNotEmpty()) {
                        item {
                            Text(
                                text = "Фільми",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(films.size) { index ->
                            val film = films[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = film.title, fontSize = 16.sp)
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            addFilmToWishlist(apiService, getAuthToken(context), film.id) {}
                                        }
                                    }) {
                                        Text("Додати")
                                    }
                                }
                            }
                        }
                    }

                    if (users.isEmpty() && films.isEmpty()) {
                        item {
                            Text(
                                text = "Нічого не знайдено",
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = MaterialTheme.colors.error
                            )
                        }
                    }
                }
            }
        }
    }
}

suspend fun addFilmToWishlist(
    apiService: ApiService,
    token: String?,
    filmId: Long,
    onResult: (Boolean) -> Unit
) {
    if (token.isNullOrEmpty()) {
        Log.d("API", "No token found")
        onResult(false)
        return
    }

    try {
        apiService.addFilmToWishlist("Bearer $token", filmId).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Log.d("API", "Film added to wishlist")
                    onResult(true)
                } else {
                    Log.d("API", "Failed to add film to wishlist. Error: ${response.message()}")
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d("API", "Error during adding film to wishlist: ${t.message}")
                onResult(false)
            }
        })
    } catch (e: Exception) {
        Log.d("API", "Error: ${e.message}")
        onResult(false)
    }
}

suspend fun subscribeToUser(
    token: String?,
    apiService: ApiService,
    context: Context,
    userId: Long,
    onResult: (Boolean) -> Unit
) {
    val loggedInUserId = getLoggedInUserId(context)

    if (token.isNullOrEmpty()) {
        Log.d("API", "No token found")
        onResult(false)
        return
    }

    try {
        val response = apiService.getUserInfo("Bearer $token")
        val userInfo = response.body()


        apiService.followUser(userId,"Bearer $token").enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    Log.d("API", "Failed to subscribe. Error: ${response.message()}")
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d("API", "Error during subscription: ${t.message}")
                onResult(false)
            }
        })
    } catch (e: Exception) {
        Log.d("API", "Error fetching user info: ${e.message}")
        onResult(false)
    }
}

fun getLoggedInUserId(context: Context): Long {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getLong("logged_in_user_id", -1L) // -1L означає, що користувач не залогінений
}


fun saveLoggedInUserId(context: Context, userId: Long) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putLong("logged_in_user_id", userId)
    editor.apply()
}


suspend fun searchFilms(apiService: ApiService, query: String, onResult: (List<Film>) -> Unit) {
    withContext(Dispatchers.IO) {
        val pageable = Pageable(page = 0, size = 10, sort = listOf("popularity"))

        apiService.searchFilms(titlePart = query, pageable = pageable).enqueue(object : Callback<FilmResponse> {
            override fun onResponse(call: Call<FilmResponse>, response: Response<FilmResponse>) {
                if (response.isSuccessful) {
                    val filmsList = response.body()?.content ?: emptyList()
                    Log.d("Search", "Films Response: $filmsList")
                    onResult(filmsList)
                } else {
                    Log.d("Search", "Error Body: ${response.errorBody()?.string()}")
                    Log.d("Search", "Films Response Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FilmResponse>, t: Throwable) {
                Log.d("Search", "Error fetching films: ${t.message}")
            }
        })
    }
}


fun searchUsers(apiService: ApiService, query: String, onResult: (List<User>) -> Unit) {
    apiService.searchUsers(query).enqueue(object : Callback<List<User>> {
        override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
            Log.d("Search", "Users Response Code: ${response.code()}")
            if (response.isSuccessful) {
                Log.d("Search", "Users: ${response.body()}")
                onResult(response.body() ?: emptyList())
            } else {
                Log.d("Search", "Error Body: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<List<User>>, t: Throwable) {
            Log.d("Search", "Error fetching users: ${t.message}")
        }
    })
}
