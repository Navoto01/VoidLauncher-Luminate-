package com.anonlab.voidlauncher

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.anonlab.voidlauncher.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pagerAdapter: ViewPagerAdapter

    private val pageSize = 20
    private var isSearchExpanding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAndCacheAllApps()
        setupViewPager()
        setupDock()
        setupSearch()
        setupPageIndicators()
        setupKeyboardAnimation()

        binding.searchResultsRecyclerView.layoutManager = GridLayoutManager(this, 4)
    }

    private fun setupKeyboardAnimation() {
        val dockMargin = (5 * resources.displayMetrics.density).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            val targetTranslationY = if (imeVisible) {
                -(imeHeight.toFloat() + dockMargin)
            } else {
                // VÁLTOZÁS: Ha a billentyűzet eltűnik, de a kereső még fókuszban van,
                // akkor vegyük el a fókuszt. Ez automatikusan elindítja a
                // bezáró animációkat a setOnFocusChangeListener-en keresztül.
                if (binding.searchEditText.hasFocus()) {
                    binding.searchEditText.clearFocus()
                }
                0f
            }

            binding.dockContainer.animate().translationY(targetTranslationY).setDuration(250).start()
            binding.searchContainer.animate().translationY(targetTranslationY).setDuration(250).start()
            binding.pageIndicator.animate().translationY(targetTranslationY).setDuration(250).start()

            // Itt indítjuk a magasság-animációt, mert itt már tudjuk a billentyűzet méretét.
            if (isSearchExpanding && imeVisible) {
                animateSearchExpansion()
            }

            insets
        }
    }

    private fun setupViewPager() {
        val allAppPackageNames = AppCache.allApps.map { it.packageName }
        val appPages = allAppPackageNames.chunked(pageSize)
        pagerAdapter = ViewPagerAdapter(this, appPages)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = 2
        updatePageIndicators(0)
    }

    private fun setupDock() {
        val dockAppsPackages = listOf(
            "com.android.settings", "com.android.chrome",
            "com.anonlab.voidlauncher", "com.google.android.gm"
        )
        val dockApps = dockAppsPackages.mapNotNull { AppCache.getApp(it) }
        binding.dockRecyclerView.layoutManager = GridLayoutManager(this, 4)
        binding.dockRecyclerView.adapter = DockAppsAdapter(dockApps)
    }

    private fun setupSearch() {
        binding.searchBarContent.setOnClickListener {
            binding.searchEditText.requestFocus()
        }

        binding.voiceSearchIcon.setOnClickListener {
            Toast.makeText(this, "Voice Search (not implemented)", Toast.LENGTH_SHORT).show()
        }

        binding.otherIcon.setOnClickListener {
            Toast.makeText(this, "Other Action (not implemented)", Toast.LENGTH_SHORT).show()
        }

        binding.searchContainer.setOnClickListener {
            binding.searchEditText.clearFocus()
        }

        binding.searchEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                isSearchExpanding = true
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            } else {
                isSearchExpanding = false
                collapseSearchContainer()
                // A billentyűzet elrejtése itt már nem feltétlenül szükséges,
                // mert a WindowInsetsListener is kezeli, de ártani nem árt.
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterAppsForSearch(s.toString().lowercase().trim())
            }
        })
    }

    private fun filterAppsForSearch(query: String) {
        val allApps = AppCache.getApps()
        val filteredList = if (query.isEmpty()) {
            emptyList()
        } else {
            allApps.filter { it.label.contains(query, ignoreCase = true) }
        }
        binding.searchResultsRecyclerView.adapter = AppsGridAdapter(filteredList)
    }

    private fun animateSearchExpansion() {
        val searchContainer = binding.searchContainer

        val collapsedHeight = resources.getDimensionPixelSize(R.dimen.search_bar_height)
        if (searchContainer.height > collapsedHeight * 1.5) return // Már ki van nyitva

        val insets = ViewCompat.getRootWindowInsets(binding.root) ?: return
        val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

        // Define margins in pixels
        val topMargin = (5 * resources.displayMetrics.density)
        val bottomMargin = (16 * resources.displayMetrics.density)
        val keyboardMargin = (5 * resources.displayMetrics.density)

        val targetTopY = binding.statusBarSpacer.bottom + topMargin
        val upwardShift = imeHeight + keyboardMargin
        val finalDockTopY = binding.dockContainer.top - upwardShift
        val targetBottomY = finalDockTopY - bottomMargin
        val targetHeight = (targetBottomY - targetTopY).toInt()

        if (targetHeight < collapsedHeight) return

        val initialHeight = searchContainer.height
        val valueAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)
        valueAnimator.duration = 300
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Int
            searchContainer.layoutParams.height = value
            searchContainer.requestLayout()
        }
        valueAnimator.start()

        binding.viewPager.animate().alpha(0f).setDuration(300).start()

        binding.searchResultsRecyclerView.visibility = View.VISIBLE
        fadeAndSwitchSearchIcon("", 1.3f)
    }

    private fun collapseSearchContainer() {
        // VÁLTOZÁS: A szöveg törlése a bezáró animáció kezdetekor.
        binding.searchEditText.text.clear()

        val searchContainer = binding.searchContainer
        val initialHeight = searchContainer.height
        val targetHeight = resources.getDimensionPixelSize(R.dimen.search_bar_height)

        if (initialHeight == targetHeight) return

        val valueAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)
        valueAnimator.duration = 300
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Int
            searchContainer.layoutParams.height = value
            searchContainer.requestLayout()
        }
        valueAnimator.start()

        binding.viewPager.animate().alpha(1f).setDuration(300).start()

        binding.searchResultsRecyclerView.visibility = View.GONE
        fadeAndSwitchSearchIcon("", 1.0f)
    }

    private fun fadeAndSwitchSearchIcon(newIcon: String, newScale: Float) {
        binding.searchIcon.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                binding.searchIcon.text = newIcon
                binding.searchIcon.scaleX = newScale
                binding.searchIcon.scaleY = newScale
                binding.searchIcon.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun loadAndCacheAllApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val allAppsInfo = mutableListOf<AppInfo>()
        val resolveInfos = pm.queryIntentActivities(intent, 0)

        for (resolveInfo in resolveInfos) {
            allAppsInfo.add(
                AppInfo(
                    label = resolveInfo.loadLabel(pm).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    icon = resolveInfo.loadIcon(pm)
                )
            )
        }
        allAppsInfo.sortBy { it.label.lowercase() }
        AppCache.allApps = allAppsInfo
    }

    private fun setupPageIndicators() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePageIndicators(position)
            }
        })
    }

    private fun updatePageIndicators(currentPage: Int) {
        binding.pageIndicator.removeAllViews()
        val pageCount = pagerAdapter.itemCount
        if (pageCount <= 1) return

        for (i in 0 until pageCount) {
            val dot = ImageView(this)
            val layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.page_indicator_size),
                resources.getDimensionPixelSize(R.dimen.page_indicator_size)
            )
            layoutParams.setMargins(8, 0, 8, 0)
            dot.layoutParams = layoutParams
            dot.setImageResource(if (i == currentPage) R.drawable.page_indicator_dot_active else R.drawable.page_indicator_dot)
            binding.pageIndicator.addView(dot)
        }
    }
}
