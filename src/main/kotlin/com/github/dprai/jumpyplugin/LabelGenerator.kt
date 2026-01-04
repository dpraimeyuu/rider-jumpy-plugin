package com.github.dprai.jumpyplugin

/**
 * Generates two-letter label combinations for jump positions.
 * Produces labels in the format: aa, ab, ac, ..., az, ba, bb, ..., zz
 * This supports up to 676 unique positions (26 * 26).
 */
object LabelGenerator {
    private const val ALPHABET = "abcdefghijklmnopqrstuvwxyz"

    /**
     * Generates a sequence of two-letter labels.
     *
     * @param count The number of labels to generate (max 676)
     * @return List of two-letter label strings
     */
    fun generateLabels(count: Int): List<String> {
        require(count >= 0) { "Count must be non-negative" }
        require(count <= 676) { "Cannot generate more than 676 labels (26 * 26)" }

        return buildList {
            var generated = 0
            for (first in ALPHABET) {
                for (second in ALPHABET) {
                    if (generated >= count) return@buildList
                    add("$first$second")
                    generated++
                }
            }
        }
    }

    /**
     * Checks if a given string is a valid two-letter label.
     *
     * @param label The label to validate
     * @return true if the label consists of two lowercase letters
     */
    fun isValidLabel(label: String): Boolean {
        return label.length == 2 &&
               label[0] in ALPHABET &&
               label[1] in ALPHABET
    }
}
