package com.example.filmfit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.groups.Group
import com.example.filmfit.getAuthToken
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.launch

class CastomeFilmIdeaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { BottomNavigationBar(navController) }
                    ) { innerPadding ->
                        GroupListScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun GroupListScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val token = getAuthToken(context)

    LaunchedEffect(Unit) {
        if (token == null) {
            isLoading = false
            Toast.makeText(context, "Токен не знайдено. Увійдіть у систему.", Toast.LENGTH_LONG).show()
        } else {
            coroutineScope.launch {
                val response = apiService.getUserGroups("Bearer $token")
                if (response.isSuccessful) {
                    groups = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Не вдалося отримати групи.", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (groups.isEmpty()) {
                    item {
                        Text(
                            "Групи відсутні",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                } else {
                    items(groups) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    val intent = Intent(context, RecomentedFilmActivity::class.java)
                                    intent.putExtra("userGroupId", group.id)
                                    context.startActivity(intent)
                                },
                            elevation = 4.dp,
                            backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(group.name, style = MaterialTheme.typography.subtitle1)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val intent = Intent(context, CreateGroupActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
            ) {
                Text("Створити нову групу", color = Color.White)
            }
        }
    }
}

