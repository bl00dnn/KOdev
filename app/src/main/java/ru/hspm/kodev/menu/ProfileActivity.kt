package ru.hspm.kodev.menu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.hspm.kodev.R
import ru.hspm.kodev.auth.UserValidator
import ru.hspm.kodev.databinding.ActivityProfileBinding
import ru.hspm.kodev.profile_button.PaymentMethodActivity
import ru.hspm.kodev.profile_button.SettingsActivity
import ru.hspm.kodev.auth.ResetPasswordActivity
import ru.hspm.kodev.models.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var userValidator: UserValidator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userValidator = UserValidator(this, binding.root)

        setupBottomNavigation()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        userValidator.validateUser(object : UserValidator.UserValidationListener {
            override fun onUserValid(user: User) {
                binding.userNameText.text = "${user.firstName} ${user.lastName}"
                binding.userEmailText.text = user.email
            }

            override fun onUserInvalid() {
                // Дополнительные действия при невалидном пользователе
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNav = binding.bottomNavigation
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> navigateTo(MainActivity::class.java)
                R.id.nav_order -> navigateTo(OrderActivity::class.java)
                R.id.nav_notifications -> navigateTo(NotificationsActivity::class.java)
                R.id.nav_profile -> true
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

    private fun setupButtons() {
        binding.paymentMethodButton.setOnClickListener {
            startActivity(Intent(this, PaymentMethodActivity::class.java))
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            userValidator.showLogoutConfirmationDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_profile
    }
}