package com.example.asc_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.asc_mobile.api.AuthApiService
import com.example.asc_mobile.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            openLoginScreen()
            return
        }

        val apiService = RetrofitClient.instance.create(AuthApiService::class.java)
        val authHeader = "Bearer $token"

        apiService.logout(authHeader).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@HomeActivity, "Вы успешно вышли", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@HomeActivity, "Ошибка выхода: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

                clearSession()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                clearSession()
            }
        })
    }

    private fun clearSession() {
        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        sharedPref.edit().remove("token").apply()

        openLoginScreen()
    }

    private fun openLoginScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}