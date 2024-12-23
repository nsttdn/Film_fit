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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.films.Film
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.CancellationException

class SuggestionsUserFilmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SuggestionsScreen()
                }
            }
        }
    }
}

@Composable
fun SuggestionsScreen() {
    val context = LocalContext.current
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    var films by remember { mutableStateOf<List<Film>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val token = getAuthToken(context)
            if (token == null) {
                Toast.makeText(context, "Token not found! Please log in.", Toast.LENGTH_SHORT).show()
                loading = false
            } else {
                films = fetchSuggestions(apiService, token)
                loading = false
            }
        } catch (e: CancellationException) {
            Log.w("Suggestions", "Coroutine cancelled: ${e.message}")
        } catch (e: Exception) {
            Log.e("Suggestions", "Error fetching suggestions: ${e.localizedMessage}")
        }
    }


    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    } else {
        if (films.isEmpty()) {
            Text(
                text = "No recommendations available.",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(films) { film ->
                    SuggestedFilmItem(film)
                }
            }
        }
    }
}

@Composable
fun SuggestedFilmItem(film: Film) {
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
            Text(text = film.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = film.overview ?: "No description available", fontSize = 14.sp)
        }
    }
}

suspend fun fetchSuggestions(apiService: ApiService, token: String): List<Film> {
    return try {
        val response = apiService.getSuggestions("Bearer $token")
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            Log.e("Suggestions", "Failed to fetch suggestions: ${response.message()}")
            emptyList()
        }
    } catch (e: CancellationException) {
        Log.w("Suggestions", "Coroutine cancelled")
        throw e // Перевірка скасування
    } catch (e: Exception) {
        Log.e("Suggestions", "Error fetching suggestions: ${e.localizedMessage}")
        emptyList()
    }
}
