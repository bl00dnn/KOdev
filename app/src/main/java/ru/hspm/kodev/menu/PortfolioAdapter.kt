package ru.hspm.kodev.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ru.hspm.kodev.R
import ru.hspm.kodev.models.ArtStationProject

class PortfolioAdapter(
    private var projects: List<ArtStationProject>,
    private val onItemClick: (ArtStationProject) -> Unit
) : RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder>() {

    inner class PortfolioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.portfolioImage)
        val titleView: TextView = itemView.findViewById(R.id.portfolioTitle)
        val likesView: TextView = itemView.findViewById(R.id.likesCount)
        val viewsView: TextView = itemView.findViewById(R.id.viewsCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_portfolio, parent, false)
        return PortfolioViewHolder(view)
    }

    override fun onBindViewHolder(holder: PortfolioViewHolder, position: Int) {
        val project = projects[position]

        // Загрузка изображения (с проверкой на null)
        project.cover_url?.let { url ->
            Glide.with(holder.itemView.context)
                .load(optimizeArtStationImageUrl(url))
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .placeholder(R.drawable.placeholder_dark)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.imageView)
        } ?: run {
            // Если cover_url == null, загружаем заглушку
            Glide.with(holder.itemView.context)
                .load(R.drawable.ic_placeholder)
                .into(holder.imageView)
        }

        // Установка текстовых данных (тоже с защитой от null)
        holder.titleView.text = project.title ?: ""
        holder.likesView.text = project.likes_count?.toString() ?: "0"
        holder.viewsView.text = project.views_count?.toString() ?: "0"

        holder.itemView.setOnClickListener { onItemClick(project) }
    }

    override fun getItemCount() = projects.size

    fun updateProjects(newProjects: List<ArtStationProject>) {
        projects = newProjects
        notifyDataSetChanged()
    }

    private fun optimizeArtStationImageUrl(url: String): String {
        return try {
            when {
                url.contains("artstation.com", ignoreCase = true) && !url.contains("?") -> {
                    url.replace("/large/", "/medium/", ignoreCase = true) + "?quality=85&width=800"
                }
                else -> url
            }
        } catch (e: Exception) {
            url // В случае ошибки возвращаем оригинальный URL
        }
    }
}