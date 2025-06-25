package com.anonlab.voidlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class DockAppsAdapter(private val apps: List<AppInfo>) :
    RecyclerView.Adapter<DockAppsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dock_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.appIcon.setImageDrawable(app.icon)

        holder.itemView.setOnClickListener { clickedView ->

            val context = clickedView.context
            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)

            if (launchIntent == null) return@setOnClickListener

            // Használjuk a rendszer animációját (ikonból kibővül teljes képernyőre)
            val options = android.app.ActivityOptions.makeScaleUpAnimation(
                clickedView,
                0, 0,
                clickedView.width,
                clickedView.height
            )

            context.startActivity(launchIntent, options.toBundle())
        }
    }

    override fun getItemCount() = apps.size
}