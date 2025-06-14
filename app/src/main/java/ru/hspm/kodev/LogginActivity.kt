package ru.hspm.kodev

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ru.hspm.kodev.databinding.ActivityLogginBinding
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

class LoggingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private var currentLanguage = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Восстановление темы и языка перед setContentView
        sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isDarkTheme = sharedPref.getBoolean("isDarkTheme", false)
        currentLanguage = sharedPref.getString("app_language", "ru") ?: "ru"

        setAppTheme(isDarkTheme)
        setAppLocale(currentLanguage)


        super.onCreate(savedInstanceState)
        binding = ActivityLogginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация Firebase Auth
        auth = Firebase.auth

        // Инициализация кнопок
        initThemeButton()
        initLanguageButton()

        // Обработка входа
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        // Переход к регистрации
        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Восстановление пароля
        binding.forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun initThemeButton() {
        val isDarkTheme = sharedPref.getBoolean("isDarkTheme", false)
        updateThemeButton(isDarkTheme)

        binding.themeToggleButton.setOnClickListener {
            val newThemeState = !isDarkTheme
            sharedPref.edit().putBoolean("isDarkTheme", newThemeState).apply()
            setAppTheme(newThemeState)
            recreate()
        }
    }

    private fun initLanguageButton() {
        updateLanguageButton()

        binding.languageToggleButton.setOnClickListener {
            currentLanguage = if (currentLanguage == "ru") "en" else "ru"
            sharedPref.edit().putString("app_language", currentLanguage).apply()
            setAppLocale(currentLanguage)
            recreate()
        }
    }

    private fun setAppTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun updateThemeButton(isDarkTheme: Boolean) {
        binding.themeToggleButton.apply {
            text = if (isDarkTheme) getString(R.string.light_theme)
            else getString(R.string.dark_theme)
            setIconResource(if (isDarkTheme) R.drawable.ic_sun
            else R.drawable.ic_moon)
        }
    }

    private fun updateLanguageButton() {
        binding.languageToggleButton.text = if (currentLanguage == "ru") "EN" else "RU"
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.emailInput.error = "Введите email"
            return false
        }

        if (password.isEmpty()) {
            binding.passwordInput.error = "Введите пароль"
            return false
        }

        if (password.length < 6) {
            binding.passwordInput.error = "Пароль должен содержать минимум 6 символов"
            return false
        }

        return true
    }

    private fun loginUser(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true

                if (task.isSuccessful) {
                    // Вход успешен
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                } else {
                    // Ошибка входа
                    Toast.makeText(
                        this,
                        "Ошибка: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        // Проверяем, вошел ли пользователь
        if (auth.currentUser != null) {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}