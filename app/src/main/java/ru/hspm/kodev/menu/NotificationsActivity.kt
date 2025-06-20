package ru.hspm.kodev.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ru.hspm.kodev.R
import ru.hspm.kodev.databinding.ActivityNotificationsBinding
import ru.hspm.kodev.utils.NotificationManager

class NotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadNotifications()
        setupBottomNavigation()
        setupClearButton()

        // Проверяем новые уведомления из Intent
        if (intent?.hasExtra("new_notification_title") == true) {
            val title = intent.getStringExtra("new_notification_title") ?: ""
            val message = intent.getStringExtra("new_notification_message") ?: ""
            if (title.isNotEmpty() && message.isNotEmpty()) {
                NotificationManager.addNotification(this, title, message)
                loadNotifications()
            }
        }
    }

    private fun setupClearButton() {
        binding.clearNotificationsButton.setOnClickListener {
            showClearConfirmationDialog()
        }
    }

    private fun showClearConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Очистить историю")
            .setMessage("Вы уверены, что хотите удалить все уведомления?")
            .setPositiveButton("Очистить") { _, _ ->
                clearAllNotifications()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearAllNotifications() {
        NotificationManager.clearAllNotifications(this)
        loadNotifications()
        Toast.makeText(this, "Все уведомления удалены", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(NotificationManager.getNotifications(this)) { notification ->
            NotificationManager.markAsRead(this, notification.id)
            adapter.notifyItemChanged(NotificationManager.getNotifications(this).indexOf(notification))
        }
        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notificationsRecyclerView.adapter = adapter
    }

    private fun loadNotifications() {
        adapter.updateNotifications(NotificationManager.getNotifications(this))
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_notifications
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> navigateTo(MainActivity::class.java)
                R.id.nav_order -> navigateTo(OrderActivity::class.java)
                R.id.nav_notifications -> true
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
}