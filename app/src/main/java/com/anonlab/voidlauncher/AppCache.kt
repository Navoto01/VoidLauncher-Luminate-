package com.anonlab.voidlauncher

object AppCache {
    fun getApps(): List<AppInfo> {
        return allApps
    }

    // Gyorsítótár az összes alkalmazás számára.
    var allApps: List<AppInfo> = emptyList()

    // Ez egy "lusta" inicializálású térkép (map), ami a csomagnév alapján
    // azonnali hozzáférést biztosít egy alkalmazás adataihoz.
    // Csak akkor jön létre, amikor először hozzáférünk.
    private val appMap by lazy {
        allApps.associateBy { it.packageName }
    }

    /**
     * Visszaad egy AppInfo objektumot a gyorsítótárból csomagnév alapján.
     * @param packageName A keresett alkalmazás csomagneve.
     * @return Az AppInfo objektum, ha létezik, egyébként null.
     */
    fun getApp(packageName: String): AppInfo? {
        return appMap[packageName]
    }
}