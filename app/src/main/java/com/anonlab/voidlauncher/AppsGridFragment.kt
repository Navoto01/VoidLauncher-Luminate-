package com.anonlab.voidlauncher

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.anonlab.voidlauncher.databinding.FragmentAppsGridBinding

class AppsGridFragment : Fragment() {

    private var _binding: FragmentAppsGridBinding? = null
    private val binding get() = _binding!!

    private var packageNames: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            packageNames = it.getStringArrayList(ARG_PACKAGE_NAMES) ?: emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appsOnThisPage = packageNames.mapNotNull { AppCache.getApp(it) }

        binding.appsRecyclerView.layoutManager = GridLayoutManager(context, 4)

        // Add this to avoid layout pass + flicker
        binding.appsRecyclerView.setItemViewCacheSize(100)

        // Add this line to force drawing with GPU (makes animation smoother)
        binding.appsRecyclerView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.ios_grid_spacing)
        binding.appsRecyclerView.addItemDecoration(GridSpacingItemDecoration(4, spacingInPixels, false))

        binding.appsRecyclerView.adapter = AppsGridAdapter(appsOnThisPage)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PACKAGE_NAMES = "package_names"

        @JvmStatic
        fun newInstance(packageNames: List<String>) =
            AppsGridFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_PACKAGE_NAMES, ArrayList(packageNames))
                }
                Log.d("AppsGridFragment", "onCreateView for page with ${packageNames.size} apps")

            }
    }
}