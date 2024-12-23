package com.example.filmfit

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.films.Film
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items


class WishlistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    WishlistScreen()
                }
            }
        }
    }
}
@Composable
fun WishlistScreen() {
    val context = LocalContext.current
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    var films by remember { mutableStateOf<List<Film>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val token = getAuthToken(context)
        if (token == null) {
            Toast.makeText(context, "Token not found! Please log in.", Toast.LENGTH_SHORT).show()
            loading = false
        } else {
            coroutineScope.launch {
                films = fetchWishlistFilms(apiService, token)
                loading = false
            }
        }
    }

    if (loading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
    } else {
        if (films.isEmpty()) {
            Text(
                text = "No films in your wishlist.",
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(films) { film ->
                    FilmItem(film)
                }
            }
        }
    }
}

@Composable
fun FilmItem(film: Film) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        val imagePath = "https://image.tmdb.org/t/p/w500${film.posterPath}"
        if (film.posterPath != null) {
            Image(
                painter = rememberImagePainter(data = imagePath),
                contentDescription = film.title,
                modifier = Modifier.size(100.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "Title: ${film.title}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Description: ${film.overview ?: "No description"}", fontSize = 14.sp)
        }
    }
}

suspend fun fetchWishlistFilms(apiService: ApiService, token: String): List<Film> {
    return try {
        val response = apiService.getWishlist("Bearer $token")
        if (response.isSuccessful) {
            response.body()?.films ?: emptyList()
        } else {
            Log.e("Wishlist", "Failed to fetch wishlist: ${response.message()}")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("Wishlist", "Error fetching wishlist: ${e.localizedMessage}")
        emptyList()
    }
}
