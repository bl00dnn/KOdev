package ru.hspm.kodev.menu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import ru.hspm.kodev.R
import ru.hspm.kodev.auth.UserValidator
import ru.hspm.kodev.databinding.ActivityOrderBinding
import ru.hspm.kodev.models.User
import ru.hspm.kodev.utils.NotificationManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var userValidator: UserValidator
    private var selectedFileUri: Uri? = null
    private val TELEGRAM_BOT_TOKEN = "7958488028:AAF62X2pdPzpp66vlPB762JSkBV2y9S6tOI"
    private val TELEGRAM_CHAT_ID = "452271178"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userValidator = UserValidator(this, binding.root)

        setupBottomNavigation()
        setupOrderForm()
    }

    override fun onStart() {
        super.onStart()
        userValidator.validateUser(object : UserValidator.UserValidationListener {
            override fun onUserValid(user: User) {
                // Автозаполнение контактов данными пользователя
                val contacts = buildString {
                    append("${user.firstName} ${user.lastName}".trim())
                    if (user.email.isNotBlank()) {
                        if (isNotEmpty()) append("\n")
                        append(user.email)
                    }
                }
                binding.contactsInput.editText?.setText(contacts)
            }

            override fun onUserInvalid() {
                binding.contactsInput.editText?.hint = getString(R.string.login_to_autofill)
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNav = binding.bottomNavigation
        bottomNav.selectedItemId = R.id.nav_order

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> navigateTo(MainActivity::class.java)
                R.id.nav_order -> true
                R.id.nav_notifications -> navigateTo(NotificationsActivity::class.java)
                R.id.nav_profile -> navigateTo(ProfileActivity::class.java)
                else -> false
            }
        }
    }

    private fun <T : AppCompatActivity> navigateTo(activity: Class<T>): Boolean {
        startActivity(Intent(this, activity))
        overridePendingTransition(0, 0)
        finish()
        return true
    }

    private fun setupOrderForm() {
        binding.attachFileButton.setOnClickListener {
            openFilePicker()
        }

        binding.submitOrderButton.setOnClickListener {
            submitOrder()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                val fileName = getFileName(uri)
                binding.attachFileButton.text = fileName ?: getString(R.string.selected_file)
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) return cursor.getString(nameIndex)
            }
        }
        return null
    }

    private fun submitOrder() {
        val description = binding.descriptionInput.editText?.text.toString().trim()
        val contacts = binding.contactsInput.editText?.text.toString().trim()

        when {
            description.isEmpty() -> {
                binding.descriptionInput.error = getString(R.string.error_description_required)
                return
            }
            contacts.isEmpty() -> {
                binding.contactsInput.error = getString(R.string.error_contacts_required)
                return
            }
            selectedFileUri == null -> {
                Toast.makeText(this, R.string.error_file_required, Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                binding.descriptionInput.error = null
                binding.contactsInput.error = null
                sendOrderToTelegram(description, contacts)
            }
        }
    }

    private fun sendOrderToTelegram(description: String, contacts: String) {
        try {
            val message = buildTelegramMessage(description, contacts)
            val requestBody = buildRequestBody(message)

            OkHttpClient().newCall(
                Request.Builder()
                    .url("https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendDocument")
                    .post(requestBody)
                    .build()
            ).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    showError(e.message ?: "Unknown error")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        showSuccess()
                    } else {
                        showError(response.message)
                    }
                }
            })
        } catch (e: Exception) {
            showError(e.message ?: "Unknown error")
        }
    }

    private fun buildTelegramMessage(description: String, contacts: String): String {
        val dateTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        return """
            🛠️ Новый заказ 3D-модели
            ⏳ $dateTime
            
            📝 Описание:
            $description
            
            📩 Контакты:
            $contacts
        """.trimIndent()
    }

    private fun buildRequestBody(message: String): MultipartBody {
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", TELEGRAM_CHAT_ID)
            .addFormDataPart("caption", message)
            .apply {
                selectedFileUri?.let { uri ->
                    val file = createTempFile(uri)
                    addFormDataPart(
                        "document",
                        getFileName(uri) ?: "file",
                        file.asRequestBody("*/*".toMediaType())
                    )
                }
            }
            .build()
    }

    private fun createTempFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("File not found")
        val file = File(cacheDir, getFileName(uri) ?: "attachment_${System.currentTimeMillis()}")
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        return file
    }

    private fun showSuccess() {
        runOnUiThread {
            Toast.makeText(this, R.string.order_success, Toast.LENGTH_LONG).show()

            NotificationManager.addNotification(
                this,
                getString(R.string.order_success_title),
                getString(R.string.order_success_message)
            )

            clearForm()
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, getString(R.string.order_error, message), Toast.LENGTH_LONG).show()
        }
    }

    private fun clearForm() {
        binding.descriptionInput.editText?.text?.clear()
        binding.attachFileButton.text = getString(R.string.select_file)
        selectedFileUri = null
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
    }
}