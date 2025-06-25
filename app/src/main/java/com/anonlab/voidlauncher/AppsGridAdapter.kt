package com.anonlab.voidlauncher

// Nincs szükség az ActivityOptions-re, de a többi import marad
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsGridAdapter(private val apps: List<AppInfo>) :
    RecyclerView.Adapter<AppsGridAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appLabel: TextView = view.findViewById(R.id.app_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.appLabel.text = app.label
        holder.appIcon.setImageDrawable(app.icon)

        // --- INNEN VÁLTOZIK A KÓD ---

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

        // --- EDDIG VÁLTOZIK A KÓD ---
    }

    override fun getItemCount() = apps.size
}