package ru.hspm.kodev.menu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.hspm.kodev.R
import ru.hspm.kodev.api.ArtStationApi
import ru.hspm.kodev.databinding.ActivityMainBinding
import ru.hspm.kodev.models.ArtStationApiResponse
import ru.hspm.kodev.models.ArtStationProject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var adapter: PortfolioAdapter
    private val YOUR_ARTSTATION_USERNAME = "bl00d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadPortfolio()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        adapter = PortfolioAdapter(emptyList()) { project ->
            openProjectInBrowser(project)
        }
        binding.portfolioRecyclerView.adapter = adapter
        binding.portfolioRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun loadPortfolio() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.artstation.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ArtStationApi::class.java)
        Log.d("ArtStation", "Запрос отправлен для пользователя: $YOUR_ARTSTATION_USERNAME")

        api.getUserProjects(YOUR_ARTSTATION_USERNAME).enqueue(object : Callback<ArtStationApiResponse> {
            override fun onResponse(
                call: Call<ArtStationApiResponse>,
                response: Response<ArtStationApiResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        Log.d("ArtStation", "Получено проектов: ${apiResponse.data.size}")
                        adapter.updateProjects(apiResponse.data)
                    }
                } else {
                    Log.e("ArtStation", "Ошибка сервера: ${response.errorBody()?.string()}")
                    // Здесь можно добавить обработку ошибки для пользователя
                }
            }

            override fun onFailure(call: Call<ArtStationApiResponse>, t: Throwable) {
                Log.e("ArtStation", "Ошибка сети", t)
                // Здесь можно показать сообщение об ошибке соединения
            }
        })
    }

    private fun openProjectInBrowser(project: ArtStationProject) {
        val url = "https://www.artstation.com/artwork/${project.hash_id}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        bottomNav = binding.bottomNavigation
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_order -> navigateTo(OrderActivity::class.java)
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

    private fun optimizeArtStationImageUrl(url: String): String {
        return when {
            url.contains("artstation.com") && !url.contains("?") -> {
                // Оптимизация URL для ArtStation
                url.replace("/large/", "/medium/") +
                        "?quality=85&width=800"
            }
            else -> url
        }
    }
}