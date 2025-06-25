package com.anonlab.voidlauncher

import android.graphics.drawable.Drawable

/**
 * Egy egyszerű adat osztály, ami egy telepített alkalmazás
 * legfontosabb adatait tartalmazza a launcher számára.
 *
 * @param label Az alkalmazás neve (pl. "Gmail").
 * @param packageName Az alkalmazás csomagneve (pl. "com.google.android.gm").
 * @param icon Az alkalmazás ikonja.
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable
)
