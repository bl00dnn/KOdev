package ru.hspm.kodev.menu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import ru.hspm.kodev.R
import ru.hspm.kodev.profile_button.SettingsActivity
import ru.hspm.kodev.auth.LoggingActivity
import ru.hspm.kodev.auth.ResetPasswordActivity
import ru.hspm.kodev.databinding.ActivityProfileBinding
import ru.hspm.kodev.profile_button.PaymentMethodActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Настройка нижнего меню навигации
        bottomNav = binding.bottomNavigation
        bottomNav.selectedItemId = R.id.nav_profile // Выделяем текущий пункт меню

        // Обработчик навигации
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish() // Закрываем текущую активити
                    true
                }
                R.id.nav_order -> {
                    startActivity(Intent(this, OrderActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    // Мы уже на этом экране
                    true
                }
                else -> false
            }
        }

        // Обработчик кнопки способ оплаты
        binding.paymentMethodButton.setOnClickListener {
            startActivity(Intent(this, PaymentMethodActivity::class.java))
        }

        // Обработчик кнопки настроек
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Обработчик кнопки сброс пароля
        binding.changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        // Обработчик кнопки выхода
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun logoutUser() {
        // Выход из Firebase
        auth.signOut()

        // Очистка SharedPreferences
        sharedPreferences.edit().clear().apply()

        // Переход на экран входа с очисткой стека активностей
        val intent = Intent(this, LoggingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        // Анимация перехода
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onResume() {
        super.onResume()
        // При возвращении на экран снова выделяем текущий пункт меню
        bottomNav.selectedItemId = R.id.nav_profile
    }
}