package ru.hspm.kodev.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.hspm.kodev.databinding.ActivityRegisterBinding
import ru.hspm.kodev.menu.ProfileActivity
import ru.hspm.kodev.models.User
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private var isLoading = false

    companion object {
        private val EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
        )
        private const val TAG = "RegisterActivity"
        private const val USERS_COLLECTION = "users"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        if (savedInstanceState?.getBoolean("isLoading") == true) {
            isLoading = true
            binding.progressBar.visibility = View.VISIBLE
            binding.registerButton.isEnabled = false
        }

        binding.registerButton.setOnClickListener {
            val firstName = binding.firstNameInput.text.toString().trim()
            val lastName = binding.lastNameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            if (validateInputs(firstName, lastName, email, password, confirmPassword)) {
                registerUser(firstName, lastName, email, password)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isLoading", isLoading)
    }

    private fun validateInputs(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        binding.firstNameInput.error = null
        binding.lastNameInput.error = null
        binding.emailInput.error = null
        binding.passwordInput.error = null
        binding.confirmPasswordInput.error = null

        when {
            firstName.isEmpty() -> {
                binding.firstNameInput.error = "Введите имя"
                isValid = false
            }
            firstName.length < 2 -> {
                binding.firstNameInput.error = "Имя слишком короткое"
                isValid = false
            }
        }

        when {
            lastName.isEmpty() -> {
                binding.lastNameInput.error = "Введите фамилию"
                isValid = false
            }
            lastName.length < 2 -> {
                binding.lastNameInput.error = "Фамилия слишком короткая"
                isValid = false
            }
        }

        when {
            email.isEmpty() -> {
                binding.emailInput.error = "Введите email"
                isValid = false
            }
            !isValidEmail(email) -> {
                binding.emailInput.error = "Введите корректный email"
                isValid = false
            }
        }

        when {
            password.isEmpty() -> {
                binding.passwordInput.error = "Введите пароль"
                isValid = false
            }
            password.length < 6 -> {
                binding.passwordInput.error = "Пароль должен содержать минимум 6 символов"
                isValid = false
            }
            !password.matches(".*[A-Z].*".toRegex()) -> {
                binding.passwordInput.error = "Пароль должен содержать хотя бы одну заглавную букву"
                isValid = false
            }
            !password.matches(".*[0-9].*".toRegex()) -> {
                binding.passwordInput.error = "Пароль должен содержать хотя бы одну цифру"
                isValid = false
            }
        }

        when {
            confirmPassword.isEmpty() -> {
                binding.confirmPasswordInput.error = "Подтвердите пароль"
                isValid = false
            }
            password != confirmPassword -> {
                binding.confirmPasswordInput.error = "Пароли не совпадают"
                isValid = false
            }
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    private fun registerUser(firstName: String, lastName: String, email: String, password: String) {
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        binding.registerButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: run {
                        handleRegistrationError(Exception("Не удалось получить ID пользователя"))
                        return@addOnCompleteListener
                    }

                    val user = User(
                        id = userId,
                        firstName = firstName,
                        lastName = lastName,
                        email = email
                    )

                    db.collection(USERS_COLLECTION)
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Регистрация прошла успешно!",
                                Toast.LENGTH_SHORT
                            ).show()
                            startProfileActivity()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Ошибка сохранения данных пользователя", e)
                            handleRegistrationError(e)
                        }
                } else {
                    handleRegistrationError(task.exception)
                }
            }
            .addOnFailureListener { e ->
                handleRegistrationError(e)
            }
            .addOnCompleteListener {
                isLoading = false
                binding.progressBar.visibility = View.GONE
                binding.registerButton.isEnabled = true
            }
    }

    private fun startProfileActivity() {
        startActivity(Intent(this, ProfileActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun handleRegistrationError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthUserCollisionException -> "Этот email уже зарегистрирован"
            is FirebaseAuthWeakPasswordException -> "Пароль слишком слабый. ${exception.message}"
            is FirebaseAuthInvalidCredentialsException -> "Некорректный email"
            else -> "Ошибка регистрации: ${exception?.message ?: "Неизвестная ошибка"}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        Log.e(TAG, "Ошибка регистрации", exception)
    }
}