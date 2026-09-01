package com.wumboing.app.ui.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{source}/{slug}"
    const val READER = "reader/{source}/{slug}/{label}/{title}/{cover}"
    const val LIBRARY = "library"

    fun detail(source: String, slug: String) =
        "detail/$source/${enc(slug)}"

    fun reader(source: String, slug: String, label: String, title: String, cover: String) =
        "reader/$source/${enc(slug)}/${enc(label)}/${enc(title)}/${enc(cover)}"

    private fun enc(s: String) = Uri.encode(s)
}
