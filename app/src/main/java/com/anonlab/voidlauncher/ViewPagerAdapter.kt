package com.anonlab.voidlauncher

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter a ViewPager2 számára. Felelős az oldalak (Fragmentek) létrehozásáért és frissítéséért.
 *
 * @param fa A szülő FragmentActivity.
 * @param appPages Az alkalmazások csomagneveinek listája oldalanként csoportosítva.
 */
class ViewPagerAdapter(fa: FragmentActivity, private var appPages: List<List<String>>) : FragmentStateAdapter(fa) {

    /**
     * Visszaadja az oldalak (fragmentek) teljes számát.
     */
    override fun getItemCount(): Int = appPages.size

    /**
     * Létrehoz egy új Fragment-et a megadott pozícióhoz (oldalhoz).
     * @param position Az aktuális oldal sorszáma.
     * @return Egy új AppsGridFragment példány az adott oldalhoz tartozó alkalmazások listájával.
     */
    override fun createFragment(position: Int): Fragment {
        return AppsGridFragment.newInstance(appPages[position])
    }

    /**
     * Frissíti az adapterben tárolt adatokat, és jelzi a ViewPager2-nek, hogy rajzolja újra magát.
     * Ezt a keresés funkció használja.
     * @param newPages Az új, oldalakra bontott alkalmazáslista.
     */
    fun updateData(newPages: List<List<String>>) {
        // Ellenőrzés: valóban más tartalom van-e (nem csak új listaobjektum)
        if (appPages.size == newPages.size && appPages.flatten() == newPages.flatten()) {
            Log.d("ViewPagerAdapter", "updateData skipped – data unchanged")
            return
        }

        Log.d("ViewPagerAdapter", "Adapter data updated. New page count: ${newPages.size}")
        appPages = newPages
        notifyDataSetChanged()
    }



}