package ingsist.engine

import java.io.File


fun readFile(path: String): List<String> {
    return File(path).readLines()
        .flatMap { line -> line.lowercase().split(" ") }
        .filter { it.isNotEmpty() }
}

fun getFrequencies(path: String): Map<String, Int> {
    val frequencies = mutableMapOf<String, Int>()
    for(word in readFile(path)){
        frequencies[word] = frequencies.getOrDefault(word, 0) + 1
    }
    return frequencies
}

fun orderByFrequency(frequencies: Map<String, Int>): List<Pair<String, Int>> {
    return frequencies.toList().sortedByDescending { it.second }
}

fun getMostFrequentWords(path: String, n: Int): List<Pair<String, Int>> {
    val frequencies = getFrequencies(path)
    val orderedFrequencies = orderByFrequency(frequencies)
    return orderedFrequencies.take(n)
}