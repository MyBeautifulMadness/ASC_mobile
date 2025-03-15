package com.example.asc_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.asc_mobile.api.AuthApiService
import com.example.asc_mobile.network.RetrofitClient
import com.example.asc_mobile.model.UserProfileResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation(R.id.nav_profile)
        loadProfile()
    }

    private fun loadProfile() {
        val apiService = RetrofitClient.instance.create(AuthApiService::class.java)
        val token = getSharedPreferences("auth_prefs", MODE_PRIVATE).getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getProfile(authHeader = "Bearer $token").enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        findViewById<EditText>(R.id.profileName).setText(profile.name)
                        findViewById<EditText>(R.id.profileLogin).setText(profile.login)
                        findViewById<EditText>(R.id.profilePhone).setText(profile.phone)
                        findViewById<EditText>(R.id.profileRole).setText(profile.role)
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}