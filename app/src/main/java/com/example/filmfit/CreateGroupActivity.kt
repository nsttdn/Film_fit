package com.example.filmfit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.filmfit.data.api.ApiClient
import com.example.filmfit.data.api.ApiService
import com.example.filmfit.data.models.CreateGroupRequest
import com.example.filmfit.data.models.groups.Friend
import com.example.filmfit.getAuthToken
import com.example.filmfit.ui.theme.FilmFitTheme
import kotlinx.coroutines.launch

class CreateGroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { BottomNavigationBar(navController) }
                    ){ innerPadding ->
                    CreateGroupScreen(modifier = Modifier.padding(innerPadding))}
                }
            }
        }
    }
}

@Composable
fun CreateGroupScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }
    val selectedFriendIds = remember { mutableStateListOf<Long>() }
    var groupName by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val token = getAuthToken(context) ?: ""

    // Завантаження списку друзів
    LaunchedEffect(Unit) {
        if (token.isEmpty()) {
            isLoading = false
            Toast.makeText(context, "Токен не знайдено. Увійдіть у систему.", Toast.LENGTH_LONG).show()
        } else {
            coroutineScope.launch {
                val response = apiService.getUserFriends("Bearer $token")
                if (response.isSuccessful) {
                    friends = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Не вдалося завантажити друзів.", Toast.LENGTH_SHORT).show()
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
        Column(modifier = Modifier.padding(16.dp)) {
            // Список друзів
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                if (friends.isEmpty()) {
                    item {
                        Text("У вас немає друзів", style = MaterialTheme.typography.body1)
                    }
                } else {
                    items(friends) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(friend.username, style = MaterialTheme.typography.body1)
                            Button(onClick = {

                                    selectedFriendIds.add(friend.id)
                                Toast.makeText(context, "ljlfyj", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Додати в групу")
                            }
                        }
                    }
                }
            }

            // Поле для введення назви групи
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Назва групи") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Кнопка створення групи
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Перевірка, чи є дані для групи
                        if (groupName.text.isNotEmpty() && selectedFriendIds.isNotEmpty()) {
                            // Створення об'єкта запиту з назвою групи та списком ID друзів
                            val createGroupRequest = CreateGroupRequest(
                                name = groupName.text,
                                userIds = selectedFriendIds.map { it.toInt() } // Перевірка, щоб ID були типу Int
                            )

                            // Відправка запиту з об'єктом створення групи
                            val response = apiService.createUserGroup(
                                createGroupRequest = createGroupRequest,
                                token = "Bearer $token"
                            )

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Групу створено успішно!", Toast.LENGTH_SHORT).show()
                                // Переходьте на CastomeFilmIdeaActivity
                                val intent = Intent(context, CastomeFilmIdeaActivity::class.java)
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Не вдалося створити групу.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Заповніть усі поля.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Створити групу")
            }
        }
    }
}
