package com.example.asc_mobile

import android.app.AlertDialog
import android.widget.TextView
import android.widget.EditText
import android.widget.Spinner
import android.view.LayoutInflater
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import com.example.asc_mobile.adapters.SkippingRequestsAdapter
import com.example.asc_mobile.api.AuthApiService
import com.example.asc_mobile.model.SkippingRequestResponse
import com.example.asc_mobile.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AbsencesActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SkippingRequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absences)
        setupBottomNavigation(R.id.nav_absences)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SkippingRequestsAdapter(emptyList())
        recyclerView.adapter = adapter

        loadSkippingRequests()

        val createButton = findViewById<Button>(R.id.btnCreateSkippingRequest)
        createButton.setOnClickListener {
            val intent = Intent(this, CreateSkippingRequestActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CREATE)
        }

        findViewById<TextView>(R.id.filtersText).setOnClickListener {
            showFiltersDialog()
        }
    }

    private fun loadSkippingRequests() {
        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Ошибка: нет токена", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = RetrofitClient.instance.create(AuthApiService::class.java)
        val authHeader = "Bearer $token"

        apiService.getSkippingRequests(authHeader).enqueue(object : Callback<SkippingRequestResponse> {
            override fun onResponse(call: Call<SkippingRequestResponse>, response: Response<SkippingRequestResponse>) {
                if (response.isSuccessful) {
                    val skippingRequests = response.body()?.list ?: emptyList()
                    adapter.updateData(skippingRequests)
                } else {
                    Toast.makeText(this@AbsencesActivity, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SkippingRequestResponse>, t: Throwable) {
                Log.e("API_ERROR", "Ошибка запроса: ${t.message}")
            }
        })
    }

    private fun showFiltersDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filters, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Выберите фильтры")
            .setView(dialogView)
            .setPositiveButton("Применить") { _, _ ->
                val startDate = dialogView.findViewById<EditText>(R.id.startDateInput).text.toString()
                val endDate = dialogView.findViewById<EditText>(R.id.endDateInput).text.toString()
                val isApproved = dialogView.findViewById<Spinner>(R.id.isApprovedSpinner).selectedItem.toString()
                applyFilters(startDate, endDate, isApproved)
            }
            .setNegativeButton("Отмена", null)
            .create()
        dialog.show()
    }

    private fun applyFilters(startDate: String, endDate: String, isApproved: String) {
        val apiService = RetrofitClient.instance.create(AuthApiService::class.java)
        val token = getSharedPreferences("auth_prefs", MODE_PRIVATE).getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        val isApprovedValue = when (isApproved) {
            "Да" -> true
            "Нет" -> false
            else -> null
        }

        apiService.getSkippingRequests(
            authHeader = "Bearer $token",
            startDate = startDate.ifEmpty { null },
            endDate = endDate.ifEmpty { null },
            isApproved = isApprovedValue,
            page = 1,
            size = 20
        ).enqueue(object : Callback<SkippingRequestResponse> {
            override fun onResponse(call: Call<SkippingRequestResponse>, response: Response<SkippingRequestResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()?.list ?: emptyList()
                    adapter.updateData(data)
                } else {
                    Toast.makeText(this@AbsencesActivity, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SkippingRequestResponse>, t: Throwable) {
                Toast.makeText(this@AbsencesActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CREATE && resultCode == RESULT_OK) {
            loadSkippingRequests()
        }
    }

    companion object {
        private const val REQUEST_CODE_CREATE = 1
    }
}