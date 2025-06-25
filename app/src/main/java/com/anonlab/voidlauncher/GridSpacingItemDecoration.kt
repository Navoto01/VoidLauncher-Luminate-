package com.anonlab.voidlauncher

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Egyedi ItemDecoration rács elrendezésű RecyclerView-hoz,
 * ami egyenlő térközt hoz létre az elemek között.
 *
 * @param spanCount Az oszlopok száma a rácsban.
 * @param spacing A térköz mérete pixelben.
 * @param includeEdge Igaz, ha a rács szélein is szeretnénk térközt.
 */
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // Elem pozíciója
        val column = position % spanCount // Elem oszlopa

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) { // Felső él
                outRect.top = spacing
            }
            outRect.bottom = spacing // Alsó él
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing // Felső térköz
            }
        }
    }
}