package com.example.filmfit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.films.Film
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.launch

class RecomentedFilmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve userGroupId from the Intent
        val groupId = intent.getLongExtra("userGroupId", -1L)

        setContent {
            FilmFitTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    // Pass groupId to RecomendedFilmScreen
                    RecomendedFilmScreen(groupId)
                }
            }
        }
    }
}

@Composable
fun RecomendedFilmScreen(groupId: Long) {
    val context = LocalContext.current
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    val coroutineScope = rememberCoroutineScope()
    var films by remember { mutableStateOf<List<Film>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val token = getAuthToken(context)

    LaunchedEffect(groupId) {
        if (groupId == -1L) {
            Toast.makeText(context, "Невірний ID групи.", Toast.LENGTH_SHORT).show()
            isLoading = false
        } else {
            coroutineScope.launch {
                val response = apiService.getFilmsForGroup(groupId, "Bearer $token")
                if (response.isSuccessful) {
                    films = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Не вдалося отримати фільми.", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (films.isEmpty()) {
                Text("Немає фільмів для цієї групи.", style = MaterialTheme.typography.h6)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(films) { film ->
                        FilmCard(film = film)
                    }
                }
            }
        }
    }
}

@Composable
fun FilmCard(film: Film) {
    // Provide a default value if the title is null
    val title = film.title ?: "No Title"
    val description = film.overview ?: "No description available."
    val imageUrl = film.posterPath ?: "" // You can also set a default image if you want.

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = rememberImagePainter(imageUrl),
                contentDescription = "Film Image",
                modifier = Modifier.size(120.dp).padding(8.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
