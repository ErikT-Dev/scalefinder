package com.eriktrummal.scalefinder.helpers

object MusicalNotationFormatter {
    private val noteMap = mapOf(
        "Cb" to "C♭", "Db" to "D♭", "Eb" to "E♭",
        "Fb" to "F♭", "Gb" to "G♭", "Ab" to "A♭", "Bb" to "B♭",
        "C" to "C", "D" to "D", "E" to "E", "F" to "F",
        "G" to "G", "A" to "A", "B" to "B",
        "C#" to "C♯", "D#" to "D♯", "E#" to "E♯",
        "F#" to "F♯", "G#" to "G♯", "A#" to "A♯", "B#" to "B♯"
    )

    fun formatNoteName(name: String): String {
        return noteMap[name] ?: name
    }

    fun formatModeName(name: String): String {
        return name.replace(Regex("(^| )b"), "$1♭")
            .replace(Regex("(^| )s"), "$1♯")
            .replace(Regex("(^| )n"), "$1♮")
            .replace(Regex("(^| )ff"), "$1♭♭")
    }

    fun formatFullName(rootNote: String, modeName: String): String {
        return "${formatNoteName(rootNote)} ${formatModeName(modeName)}"
    }
}