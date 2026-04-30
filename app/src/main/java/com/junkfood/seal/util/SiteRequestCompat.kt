package com.junkfood.seal.util

import com.yausername.youtubedl_android.YoutubeDLRequest

private const val DESKTOP_CHROME_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"

private val TRAILING_URL_CHARS = charArrayOf('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
private val TRAILING_CJK_URL_CHARS =
    charArrayOf(
        '\u3002',
        '\uff0c',
        '\uff1b',
        '\uff1a',
        '\uff01',
        '\uff1f',
        '\uff09',
        '\u3011',
        '\u300d',
        '\u300f',
    )

fun String.normalizeUrlForYtdlp(): String {
    val trimmed =
        trim()
            .trimEnd(*TRAILING_URL_CHARS)
            .trimEnd(*TRAILING_CJK_URL_CHARS)

    return trimmed.replaceFirst(
        Regex("^https?://(www\\.|mobile\\.)?x\\.com/", RegexOption.IGNORE_CASE),
        "https://twitter.com/",
    )
}

fun Iterable<String>.normalizeUrlsForYtdlp(): List<String> =
    map { it.normalizeUrlForYtdlp() }.filter { it.isNotBlank() }.distinct()

fun YoutubeDLRequest.applySiteRequestCompat(
    urls: Iterable<String>,
    userAgentString: String = "",
): YoutubeDLRequest =
    apply {
        val urlText = urls.joinToString(separator = "\n") { it.lowercase() }
        val headers = linkedMapOf<String, String>()

        fun addHeader(name: String, value: String) {
            if (!headers.containsKey(name)) {
                headers[name] = value
            }
        }

        if (urlText.contains("bilibili.com") || urlText.contains("b23.tv")) {
            addHeader("Referer", "https://www.bilibili.com/")
            addHeader("Origin", "https://www.bilibili.com")
        }

        if (
            urlText.contains("twitter.com") ||
                urlText.contains("x.com") ||
                urlText.contains("t.co/")
        ) {
            addHeader("Referer", "https://twitter.com/")
        }

        if (urlText.contains("douyin.com")) {
            addHeader("Referer", "https://www.douyin.com/")
            addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
        }

        if (urlText.contains("xiaohongshu.com") || urlText.contains("xhslink.com")) {
            addHeader("Referer", "https://www.xiaohongshu.com/")
            addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
        }

        if (
            userAgentString.isBlank() &&
                headers.isNotEmpty() &&
                headers.keys.none { it.equals("User-Agent", ignoreCase = true) }
        ) {
            addHeader("User-Agent", DESKTOP_CHROME_USER_AGENT)
        }

        headers.forEach { (name, value) -> addOption("--add-header", "$name:$value") }
    }
