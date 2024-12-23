package com.example.filmfit.data.api
import com.example.filmfit.data.models.CreateGroupRequest
import com.example.filmfit.data.models.Pageable
import com.example.filmfit.data.models.films.Film
import com.example.filmfit.data.models.films.FilmResponse
import com.example.filmfit.data.models.folowing.UserFollowing
import com.example.filmfit.data.models.groups.Friend
import com.example.filmfit.data.models.groups.Group
import com.example.filmfit.data.models.groups.GroupResponse
import com.example.filmfit.data.models.login.LoginRequest
import com.example.filmfit.data.models.login.LoginResponse
import com.example.filmfit.data.models.popularFilms.PopularMovie
import com.example.filmfit.data.models.register.RegisterRequest
import com.example.filmfit.data.models.register.RegisterResponse
import com.example.filmfit.data.models.users.User
import com.example.filmfit.data.models.users.WishlistResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("/films/popular")
    fun getPopularMovies(): Call<List<PopularMovie>>

    @GET("/users")
    fun searchUsers(@Query(value = "namePart", encoded = true) namePart: String): Call<List<User>>

    @GET("/films")
    fun searchFilms(
        @Query("titlePart") titlePart: String,
        @Query("pageable") pageable: Pageable
    ): Call<FilmResponse>

    @GET("/users/me")
    suspend fun getUserInfo(
        @Header("Authorization") token: String,
        @Header("ngrok-skip-browser-warning") skipBrowserWarning: Boolean = true
    ): Response<User>

    @GET("/users/{id}/following")
    fun getUserFollowing(
        @Path("id") userId: Long,
        @Header("ngrok-skip-browser-warning") ngrokSkipBrowserWarning: Boolean = true
    ): Call<List<UserFollowing>>

    @POST("/users/{id}/follow")
    fun followUser(
        @Path("id") userId: Long,
        @Header("Authorization") token: String
    ): Call<Unit>

    @POST("/whishlists/me/films")
    fun addFilmToWishlist(
        @Header("Authorization") authHeader: String,
        @Query("filmId") filmId: Long
    ): Call<Unit>

    @GET("whishlists/me")
    suspend fun getWishlist(@Header("Authorization") token: String): Response<WishlistResponse>

    @GET("/films/me/suggestions")
    suspend fun getSuggestions(
        @Header("Authorization") token: String
    ): Response<List<Film>>

    @GET("/users/me/friends")
    suspend fun getUserFriends(
        @Header("Authorization") token: String
    ): Response<List<Friend>>

    @POST("/user-groups")
    suspend fun createUserGroup(
        @Body createGroupRequest: CreateGroupRequest,
        @Header("Authorization") token: String
    ): Response<GroupResponse>

    @GET("/user-groups/me")
    suspend fun getUserGroups(
        @Header("Authorization") token: String
    ): Response<List<Group>>

    @GET("/films/common-for-group/{userGroupId}")
    suspend fun getFilmsForGroup(
        @Path("userGroupId") userGroupId: Long,
        @Header("Authorization") token: String
    ): Response<List<Film>>

}

