package com.anonlab.voidlauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.WindowCompat
import com.anonlab.voidlauncher.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var gestureDetector: GestureDetectorCompat

    companion object {
        const val PREFS_NAME = "LauncherPrefs"
        const val KEY_IS_FIRST_LAUNCH = "isFirstLaunch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. LÉPÉS: Ellenőrizzük, hogy ez az első indítás-e
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)

        if (!isFirstLaunch) {
            // Ha nem az első, azonnal indítjuk a főképernyőt és bezárjuk ezt.
            launchMainActivity()
            return // Fontos, hogy a return itt legyen, hogy a többi kód ne fusson le.
        }

        // --- Ha az első indítás, akkor a szokásos módon folytatjuk ---

        // Teljes képernyő beállítása
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Indítsuk el a nap ikon forgó animációját
        startSunRotationAnimation()

        // 2. LÉPÉS: Gesztusérzékelő beállítása
        setupGestureDetector()
    }

    private fun startSunRotationAnimation() {
        // Hozzuk létre a forgó animációt
        val rotateAnimation = RotateAnimation(
            0f,
            360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 50000
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
        }
        // Indítsuk el az animációt közvetlenül a sunContainer-en
        binding.sunContainer.startAnimation(rotateAnimation)
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            // A fling (suhintás) eseményt figyeljük
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                // Ellenőrizzük, hogy a suhintás felfelé történt-e
                // A negatív velocityY jelenti a felfelé mozgást.
                // A abs(velocityY) > abs(velocityX) biztosítja, hogy inkább függőleges, mint vízszintes mozgás volt.
                if (e1 != null && e1.y > e2.y && kotlin.math.abs(velocityY) > kotlin.math.abs(velocityX)) {
                    // A suhintás felfelé történt
                    completeSetup()
                    return true
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
    }

    /**
     * Befejezi a beállítást: elmenti a jelzőt és elindítja a MainActivity-t.
     */
    private fun completeSetup() {
        // 3. LÉPÉS: Elmentjük, hogy a beállítás befejeződött
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
            apply() // apply() aszinkron, ami itt tökéletes
        }

        // 4. LÉPÉS: Elindítjuk a főképernyőt
        launchMainActivity()
    }

    /**
     * Segédfüggvény a MainActivity elindítására és a SetupActivity bezárására.
     */
    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Bezárjuk a SetupActivity-t, hogy ne lehessen visszanavigálni ide.
    }

    // 5. LÉPÉS: Az érintési eseményeket átadjuk a gesztusérzékelőnek
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }
}
