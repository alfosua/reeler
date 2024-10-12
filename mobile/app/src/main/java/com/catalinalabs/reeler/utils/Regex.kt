package com.catalinalabs.reeler.utils

object RegexExtensions {
    operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)
}
