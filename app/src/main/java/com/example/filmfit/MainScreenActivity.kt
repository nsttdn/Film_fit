package com.example.filmfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.popularFilms.PopularMovie
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.navigation.compose.*

class MainScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FilmFitTheme {
                MainScreenContent()
            }
        }
    }
}
@Composable
fun MainScreenContent() {
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    var popularMovies by remember { mutableStateOf<List<PopularMovie>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            fetchPopularMovies(apiService) { movies ->
                popularMovies = movies
            }
        }
    }

    val navController = rememberNavController()

    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Title Section
                Text(
                    text = "Популярні фільми",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularMovies.size) { index ->
                        val movie = popularMovies[index]
                        MovieCard(movie)
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: PopularMovie) {

    Card(
        modifier = Modifier
            .width(360.dp)
            .padding(bottom = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Poster Image with rounded corners
            Image(
                painter = rememberAsyncImagePainter(movie.posterPath?:""),
                contentDescription = movie.title?:"",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(480.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Movie Title with a fresh font style
            Text(
                text = movie.title?:"",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = (movie.overview ?: "No description available").take(60) + "...", // Handle null overview
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

        }
    }
}

fun fetchPopularMovies(apiService: ApiService, onMoviesFetched: (List<PopularMovie>) -> Unit) {
    apiService.getPopularMovies().enqueue(object : Callback<List<PopularMovie>> {
        override fun onResponse(call: Call<List<PopularMovie>>, response: Response<List<PopularMovie>>) {
            if (response.isSuccessful) {
                response.body()?.let { onMoviesFetched(it) }
            }
        }

        override fun onFailure(call: Call<List<PopularMovie>>, t: Throwable) {
            Log.d("MainScreenContent", "Error fetching movies: ${t.message}")
        }
    })
}

