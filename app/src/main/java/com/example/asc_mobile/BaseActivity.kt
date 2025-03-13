package com.example.asc_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.asc_mobile.api.AuthApiService
import com.example.asc_mobile.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setupBottomNavigation(selectedItemId: Int) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = selectedItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    if (this !is HomeActivity) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_absences -> {
                    if (this !is AbsencesActivity) {
                        startActivity(Intent(this, AbsencesActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
    }

    fun logoutUser() {
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
                Toast.makeText(this@BaseActivity, "Вы успешно вышли", Toast.LENGTH_SHORT).show()
                clearSession()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@BaseActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
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
