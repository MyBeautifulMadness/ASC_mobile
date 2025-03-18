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
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import com.example.asc_mobile.adapters.SkippingRequestsAdapter
import com.example.asc_mobile.api.AuthApiService
import com.example.asc_mobile.model.SkippingRequestResponse
import com.example.asc_mobile.network.RetrofitClient
import com.example.asc_mobile.model.SkippingRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.content.FileProvider
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.Calendar
import android.app.DatePickerDialog

class AbsencesActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SkippingRequestsAdapter

    private lateinit var prevPageButton: Button
    private lateinit var nextPageButton: Button
    private lateinit var pageIndicator: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absences)
        setupBottomNavigation(R.id.nav_absences)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SkippingRequestsAdapter(emptyList()) { request ->
            showOptionsDialog(request)
        }
        recyclerView.adapter = adapter

        prevPageButton = findViewById(R.id.prevPageButton)
        nextPageButton = findViewById(R.id.nextPageButton)
        pageIndicator = findViewById(R.id.pageIndicator)

        prevPageButton.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                applyFilters("", "", "", "")
            }
        }

        nextPageButton.setOnClickListener {
            currentPage++
            applyFilters("", "", "", "")
        }

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

    private var selectedRequestId: String? = null
    private var requestId: String? = null

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

                    val totalPages = response.body()?.totalPagesCount ?: 1
                    updatePaginationUI(totalPages)
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
        val startDateInput = dialogView.findViewById<EditText>(R.id.startDateInput)
        val endDateInput = dialogView.findViewById<EditText>(R.id.endDateInput)

        startDateInput.setOnClickListener { showDatePicker(startDateInput) }
        endDateInput.setOnClickListener { showDatePicker(endDateInput) }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Выберите фильтры")
            .setView(dialogView)
            .setPositiveButton("Применить") { _, _ ->
                val startDate = startDateInput.text.toString()
                val endDate = endDateInput.text.toString()
                val isApproved = dialogView.findViewById<Spinner>(R.id.isApprovedSpinner).selectedItem.toString()
                val sortSetting = dialogView.findViewById<Spinner>(R.id.sortSettingSpinner).selectedItem.toString()
                applyFilters(startDate, endDate, isApproved, sortSetting)
            }
            .setNegativeButton("Отмена", null)
            .create()
        dialog.show()
    }

    private var currentPage = 1
    private val pageSize = 5

    private fun applyFilters(startDate: String, endDate: String, isApproved: String, sortSetting: String) {
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
            sortSetting = if (sortSetting == "--") null else sortSetting,
            page = currentPage,
            size = pageSize
        ).enqueue(object : Callback<SkippingRequestResponse> {
            override fun onResponse(call: Call<SkippingRequestResponse>, response: Response<SkippingRequestResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()?.list ?: emptyList()
                    adapter.updateData(data)
                    val totalPages = response.body()?.totalPagesCount ?: 1
                    updatePaginationUI(totalPages)
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

        if (requestCode == REQUEST_CODE_PICK_FILES && resultCode == RESULT_OK) {
            val fileUris = mutableListOf<Uri>()

            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    fileUris.add(clipData.getItemAt(i).uri)
                }
            } ?: data?.data?.let { uri ->
                fileUris.add(uri)
            }

            if (fileUris.isNotEmpty()) {
                if (selectedRequestId.isNullOrEmpty()) {
                    Toast.makeText(this, "Ошибка: requestId не задан", Toast.LENGTH_SHORT).show()
                    return
                }
                uploadDocuments(selectedRequestId!!, fileUris)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_CREATE = 1
        private const val REQUEST_CODE_PICK_FILES = 2
    }

    private fun showOptionsDialog(request: SkippingRequest) {
        val options = arrayOf("Добавить документы", "Посмотреть документы", "Продлить пропуск")

        AlertDialog.Builder(this)
            .setTitle("Выберите действие")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        selectedRequestId = request.id
                        selectDocumentsForUpload(selectedRequestId ?: "")
                    }
                    1 -> loadSkippingRequestDocuments(request.id)
                    2 -> showExtendSkippingRequestDialog(request.id)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun loadSkippingRequestDocuments(skippingRequestId: String) {
        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Ошибка: нет токена", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = RetrofitClient.instance.create(AuthApiService::class.java)
        val authHeader = "Bearer $token"

        apiService.getSkippingRequestDocuments(authHeader, skippingRequestId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            saveAndOpenDocument(responseBody, "documents.zip")
                        }
                    } else {
                        Toast.makeText(this@AbsencesActivity, "Ошибка загрузки документов", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@AbsencesActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveAndOpenDocument(body: ResponseBody, fileName: String) {
        try {
            val fileDir = getExternalFilesDir("documents") ?: return
            val file = File(fileDir, "documents.zip")

            Log.d("FileCheck", "Trying to save file in: ${file.absolutePath}")

            if (!fileDir.exists()) {
                Log.e("FileCheck", "Directory does not exist! Creating...")
                fileDir.mkdirs()
            }

            val outputStream = FileOutputStream(file)
            outputStream.use { it.write(body.bytes()) }

            val uri = FileProvider.getUriForFile(this, "com.example.asc_mobile.provider", file)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/zip")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Нет приложения для открытия ZIP-файлов", Toast.LENGTH_SHORT).show()
            }

            startActivity(Intent.createChooser(intent, "Открыть с помощью"))
        } catch (e: IOException) {
            Toast.makeText(this, "Ошибка сохранения файла", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadDocuments(requestId: String, fileUris: List<Uri>) {
        val token = getSharedPreferences("auth_prefs", MODE_PRIVATE).getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = RetrofitClient.instance.create(AuthApiService::class.java)

        val requestBody = requestId.toRequestBody("text/plain".toMediaTypeOrNull())

        val multipartFiles = fileUris.mapNotNull { uri ->
            val file = getFileFromUri(uri) ?: return@mapNotNull null

            val fileName = getFileNameFromUri(uri) ?: file.name

            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", fileName, requestFile)
        }

        val authHeader = "Bearer $token"
        apiService.addSkippingRequestDocuments(authHeader, requestBody, multipartFiles)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AbsencesActivity, "Документы успешно добавлены!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@AbsencesActivity, "Ошибка при загрузке документов", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@AbsencesActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", "Ошибка сети: ${t.message}")
                }
            })
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex("_display_name")
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    private fun selectDocumentsForUpload(requestId: String) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "Выберите документы"), REQUEST_CODE_PICK_FILES)
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload", ".tmp", cacheDir)
        tempFile.outputStream().use { output -> inputStream.copyTo(output) }
        return tempFile
    }

    private fun showExtendSkippingRequestDialog(requestId: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_extend_skipping_request, null)

        val startDateInput = dialogView.findViewById<EditText>(R.id.newStartDateInput)
        val endDateInput = dialogView.findViewById<EditText>(R.id.newEndDateInput)

        startDateInput.setOnClickListener { showDatePicker(startDateInput) }
        endDateInput.setOnClickListener { showDatePicker(endDateInput) }

        AlertDialog.Builder(this)
            .setTitle("Продлить пропуск")
            .setView(dialogView)
            .setPositiveButton("Продлить") { _, _ ->
                val newStartDate = startDateInput.text.toString()
                val newEndDate = endDateInput.text.toString()

                if (newStartDate.isNotEmpty() && newEndDate.isNotEmpty()) {
                    extendSkippingRequest(requestId, newStartDate, newEndDate)
                } else {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun extendSkippingRequest(requestId: String, newStartDate: String, newEndDate: String) {
        val token = getSharedPreferences("auth_prefs", MODE_PRIVATE).getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = RetrofitClient.instance.create(AuthApiService::class.java)

        apiService.changeSkippingRequestDate(
            authHeader = "Bearer $token",
            skippingRequestId = requestId,
            newStartDate = newStartDate,
            newEndDate = newEndDate
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AbsencesActivity, "Даты пропуска успешно обновлены!", Toast.LENGTH_SHORT).show()
                    loadSkippingRequests()
                } else {
                    Toast.makeText(this@AbsencesActivity, "Ошибка обновления дат", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AbsencesActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePaginationUI(totalPages: Int) {
        pageIndicator.text = "Страница $currentPage"

        prevPageButton.isEnabled = currentPage > 1
        nextPageButton.isEnabled = currentPage < totalPages

        findViewById<LinearLayout>(R.id.paginationLayout).visibility =
            if (totalPages > 1) View.VISIBLE else View.GONE
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            editText.setText(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.show()
    }
}