package com.example.asc_mobile

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.asc_mobile.api.AuthApiService
import com.example.asc_mobile.model.CreateSkippingRequest
import com.example.asc_mobile.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CreateSkippingRequestActivity : AppCompatActivity() {
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var reasonInput: EditText
    private lateinit var submitButton: Button
    private lateinit var checkBoxes: List<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_skipping_request)

        startDateInput = findViewById(R.id.startDateInput)
        endDateInput = findViewById(R.id.endDateInput)
        reasonInput = findViewById(R.id.reasonInput)
        submitButton = findViewById(R.id.submitRequestButton)

        checkBoxes = listOf(
            findViewById(R.id.lesson1),
            findViewById(R.id.lesson2),
            findViewById(R.id.lesson3),
            findViewById(R.id.lesson4),
            findViewById(R.id.lesson5),
            findViewById(R.id.lesson6),
            findViewById(R.id.lesson7)
        )

        startDateInput.setOnClickListener { showDatePicker(startDateInput) }
        endDateInput.setOnClickListener { showDatePicker(endDateInput) }

        submitButton.setOnClickListener { sendSkippingRequest() }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            editText.setText(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun sendSkippingRequest() {
        val startDate = startDateInput.text.toString()
        val endDate = endDateInput.text.toString()
        val reason = reasonInput.text.toString()
        val lessons = checkBoxes.filter { it.isChecked }.map { it.text.toString().toInt() }

        if (startDate.isEmpty() || endDate.isEmpty() || reason.isEmpty() || lessons.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = RetrofitClient.instance.create(AuthApiService::class.java)
        val request = CreateSkippingRequest(startDate, endDate, reason, lessons)
        val authHeader = "Bearer $token"

        apiService.createSkippingRequest(authHeader, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateSkippingRequestActivity, "Пропуск создан!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@CreateSkippingRequestActivity, "Ошибка создания", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@CreateSkippingRequestActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}