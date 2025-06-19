package ru.hspm.kodev.profile_button

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.hspm.kodev.R
import ru.hspm.kodev.databinding.ActivitySettingsBinding
import ru.hspm.kodev.models.User
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)

        setupToolbar()
        loadUserData()
        loadAppSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun loadUserData() {
        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    document.toObject(User::class.java)?.let { user ->
                        binding.firstNameInput.editText?.setText(user.firstName)
                        binding.lastNameInput.editText?.setText(user.lastName)
                    }
                }
        }
    }

    private fun loadAppSettings() {
        // Язык
        when (sharedPreferences.getString("app_language", "system")) {
            "ru" -> binding.languageRadioGroup.check(R.id.russianRadio)
            "en" -> binding.languageRadioGroup.check(R.id.englishRadio)
        }

        // Тема
        when (sharedPreferences.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.themeRadioGroup.check(R.id.lightThemeRadio)
            AppCompatDelegate.MODE_NIGHT_YES -> binding.themeRadioGroup.check(R.id.darkThemeRadio)
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.themeRadioGroup.check(R.id.systemThemeRadio)
        }
    }

    private fun setupListeners() {
        binding.savePersonalDataButton.setOnClickListener {
            savePersonalData()
        }

        binding.saveAppSettingsButton.setOnClickListener {
            saveAppSettings()
        }
    }

    private fun savePersonalData() {
        val firstName = binding.firstNameInput.editText?.text.toString().trim()
        val lastName = binding.lastNameInput.editText?.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId)
                .update(mapOf(
                    "first_name" to firstName,
                    "last_name" to lastName
                ))
                .addOnSuccessListener {
                    Toast.makeText(this, R.string.personal_data_saved, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.error_saving_data, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveAppSettings() {
        // Сохраняем язык
        val language = when (binding.languageRadioGroup.checkedRadioButtonId) {
            R.id.russianRadio -> "ru"
            R.id.englishRadio -> "en"
            else -> "system"
        }
        sharedPreferences.edit().putString("app_language", language).apply()

        // Сохраняем тему
        val theme = when (binding.themeRadioGroup.checkedRadioButtonId) {
            R.id.lightThemeRadio -> AppCompatDelegate.MODE_NIGHT_NO
            R.id.darkThemeRadio -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        sharedPreferences.edit().putInt("app_theme", theme).apply()
        AppCompatDelegate.setDefaultNightMode(theme)

        // Применяем язык
        if (language != "system") {
            setAppLocale(language)
        }

        Toast.makeText(this, R.string.settings_saved_restart, Toast.LENGTH_SHORT).show()
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)

        // Перезапускаем активити для применения изменений
        val intent = Intent(this, SettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}