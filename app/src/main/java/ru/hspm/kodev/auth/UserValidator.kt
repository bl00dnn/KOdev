package ru.hspm.kodev.auth

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.hspm.kodev.R
import ru.hspm.kodev.models.User

class UserValidator(private val context: Context, private val view: android.view.View) {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val TAG = "UserValidator"
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    interface UserValidationListener {
        fun onUserValid(user: User)
        fun onUserInvalid()
    }

    fun validateUser(listener: UserValidationListener? = null) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showErrorAndLogout(context.getString(R.string.user_not_authorized))
            listener?.onUserInvalid()
            return
        }

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        listener?.onUserValid(user)
                    } else {
                        Log.w(TAG, "User document is empty")
                        showErrorAndLogout(context.getString(R.string.error_user_data))
                        listener?.onUserInvalid()
                    }
                } else {
                    Log.w(TAG, "User document not found")
                    showErrorAndLogout(context.getString(R.string.error_account_not_found))
                    listener?.onUserInvalid()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user data", e)
                showErrorAndLogout(context.getString(R.string.error_load_data))
                listener?.onUserInvalid()
            }
    }

    private fun showErrorAndLogout(message: String) {
        Snackbar.make(view, "$message. ${context.getString(R.string.logout_message)}",
            Snackbar.LENGTH_LONG).show()

        Handler(Looper.getMainLooper()).postDelayed({
            forceLogout()
        }, 3000)
    }

    fun forceLogout() {
        auth.signOut()
        sharedPreferences.edit().clear().apply()

        val intent = Intent(context, LoggingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        if (context is AppCompatActivity) {
            context.finish()
            context.overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
    }

    fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.logout_title))
            .setMessage(context.getString(R.string.logout_confirmation))
            .setPositiveButton(context.getString(R.string.yes)) { _, _ -> forceLogout() }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }
}