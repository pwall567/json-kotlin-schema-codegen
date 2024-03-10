/*
 * @(#) Name.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2024 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.util

/**
 * A class to represent a Name in a software context.  The name may be created with any form of punctuation and
 * capitalisation, and then extracted in forms required in various settings, _e.g._ `camelCase`, `snake_case` _etc._
 *
 * @author  Peter Wall
 */
class Name(
    private val original: String,
    val defaultPrefix: String = "gen",
    val numberPrefix: String = "n",
) : List<String> by original.split() {

    val generated = generator++

    val lowerCamelCase: String by DefaultValue {
        when (size) {
            0 -> "$defaultPrefix$generator".uncapitalise()
            1 -> this[0].forceFirstNotDigit().uncapitalise()
            else -> buildString {
                append(this@Name[0].forceFirstNotDigit().uncapitalise())
                for (i in 1 until this@Name.size)
                    append(this@Name[i].capitalise())
            }
        }
    }

    @get:JvmName("getcamelCase")
    val camelCase: String
        get() = lowerCamelCase

    @Suppress("PropertyName")
    val UpperCamelCase: String by DefaultValue {
        when (size) {
            0 -> "$defaultPrefix$generator".capitalise()
            1 -> this[0].forceFirstNotDigit().capitalise()
            else -> buildString {
                append(this@Name[0].forceFirstNotDigit().capitalise())
                for (i in 1 until this@Name.size)
                    append(this@Name[i].capitalise())
            }
        }
    }

    @Suppress("PropertyName")
    val CamelCase: String
        get() = UpperCamelCase

    @Suppress("PropertyName")
    val lower_snake_case: String by DefaultValue {
        separatedCase('_') { toLower() }
    }

    @Suppress("PropertyName")
    val snake_case: String
        get() = lower_snake_case

    @Suppress("PropertyName")
    val UPPER_SNAKE_CASE: String by DefaultValue {
        separatedCase('_') { toUpper() }
    }

    @Suppress("PropertyName")
    val SNAKE_CASE: String
        get() = UPPER_SNAKE_CASE

    @Suppress("PropertyName")
    val Capital_Snake_Case: String by DefaultValue {
        separatedCase('_') { capitalise() }
    }

    @Suppress("PropertyName")
    val Snake_Case: String
        get() = Capital_Snake_Case

    @Suppress("PropertyName")
    val `lower-kebab-case`: String by DefaultValue {
        separatedCase('-') { toLower() }
    }

    @Suppress("PropertyName")
    val `kebab-case`: String
        get() = `lower-kebab-case`

    @Suppress("PropertyName")
    val `UPPER-KEBAB-CASE`: String by DefaultValue {
        separatedCase('-') { toUpper() }
    }

    @Suppress("PropertyName")
    val `KEBAB-CASE`: String
        get() = `UPPER-KEBAB-CASE`

    @Suppress("PropertyName")
    val `Capital-Kebab-Case`: String by DefaultValue {
        separatedCase('-') { capitalise() }
    }

    @Suppress("PropertyName")
    val `Kebab-Case`: String
        get() = `Capital-Kebab-Case`

    fun separatedCase(separator: Char, converter: String.() -> String): String = when (size) {
        0 -> "$defaultPrefix$generator".converter()
        1 -> this[0].forceFirstNotDigit().converter()
        else -> buildString {
            append(this@Name[0].forceFirstNotDigit().converter())
            for (i in 1 until this@Name.size) {
                append(separator)
                append(this@Name[i].converter())
            }
        }
    }

    private fun String.forceFirstNotDigit(): String = if (this[0].isDigit()) "$numberPrefix$this" else this

    override fun toString(): String = original

    override fun equals(other: Any?): Boolean = this === other || other is Name && original == other.original &&
            defaultPrefix == other.defaultPrefix && numberPrefix == other.numberPrefix

    override fun hashCode(): Int = original.hashCode() xor defaultPrefix.hashCode() xor numberPrefix.hashCode()

    companion object {

        private const val CASE_OFFSET = 'a'.code - 'A'.code

        var generator = 0

        fun Char.isDigit() = this in '0'..'9'
        fun Char.isLower() = this in 'a'..'z'
        fun Char.isUpper() = this in 'A'..'Z'
        fun Char.toUpper() = (code - CASE_OFFSET).toChar()
        fun Char.toLower() = (code + CASE_OFFSET).toChar()

        fun String.capitalise(): String = this[0].let { if (it.isLower()) "${it.toUpper()}${substring(1)}" else this }

        fun String.uncapitalise(): String = if (this.all { !it.isLower() }) this.toLower() else
                this[0].let { if (it.isUpper()) "${it.toLower()}${substring(1)}" else this }

        fun String.toLower(): String = if (this.all { !it.isUpper() }) this else buildString {
            for (ch in this@toLower)
                append(if (ch.isUpper()) ch.toLower() else ch)
        }

        fun String.toUpper(): String = if (this.all { !it.isLower() }) this else buildString {
            for (ch in this@toUpper)
                append(if (ch.isLower()) ch.toUpper() else ch)
        }

        enum class State { INITIAL, UPPER_SEEN, MULTIPLE_UPPER_SEEN, LOWER_SEEN, DIGIT_SEEN }

        fun String.split(): List<String> {
            val result = mutableListOf<String>()
            val sb = StringBuilder()
            var state: State = State.INITIAL
            for (ch in this) {
                when (state) {
                    State.INITIAL -> when {
                        ch.isUpper() -> {
                            sb.append(ch)
                            state = State.UPPER_SEEN
                        }
                        ch.isLower() -> {
                            sb.append(ch)
                            state = State.LOWER_SEEN
                        }
                        ch.isDigit() -> {
                            sb.append(ch)
                            state = State.DIGIT_SEEN
                        }
                    }
                    State.UPPER_SEEN -> when {
                        ch.isUpper() -> {
                            sb.append(ch)
                            state = State.MULTIPLE_UPPER_SEEN
                        }
                        ch.isLower() -> {
                            sb.append(ch)
                            state = State.LOWER_SEEN
                        }
                        ch.isDigit() -> {
                            sb.append(ch)
                            state = State.DIGIT_SEEN
                        }
                        else -> {
                            result.add(sb.toString())
                            sb.setLength(0)
                            state = State.INITIAL
                        }
                    }
                    State.MULTIPLE_UPPER_SEEN -> when {
                        ch.isUpper() -> sb.append(ch)
                        ch.isLower() || ch.isDigit() -> {
                            val i = sb.length
                            val last = sb.last()
                            sb.setLength(i - 1)
                            result.add(sb.toString())
                            sb.setLength(0)
                            sb.append(last)
                            sb.append(ch)
                            state = if (ch.isLower()) State.LOWER_SEEN else State.DIGIT_SEEN
                        }
                        else -> {
                            result.add(sb.toString())
                            sb.setLength(0)
                            state = State.INITIAL
                        }
                    }
                    State.LOWER_SEEN -> when {
                        ch.isUpper() -> {
                            result.add(sb.toString())
                            sb.setLength(0)
                            sb.append(ch)
                            state = State.UPPER_SEEN
                        }
                        ch.isLower() -> sb.append(ch)
                        ch.isDigit() -> {
                            sb.append(ch)
                            state = State.DIGIT_SEEN
                        }
                        else -> {
                            result.add(sb.toString())
                            sb.setLength(0)
                            state = State.INITIAL
                        }
                    }
                    State.DIGIT_SEEN -> when {
                        ch.isUpper() -> {
                            result.add(sb.toString())
                            sb.setLength(0)
                            sb.append(ch)
                            state = State.UPPER_SEEN
                        }
                        ch.isLower() -> {
                            sb.append(ch)
                            state = State.LOWER_SEEN
                        }
                        ch.isDigit() -> sb.append(ch)
                        else -> {
                            result.add(sb.toString())
                            sb.setLength(0)
                            state = State.INITIAL
                        }
                    }
                }
            }
            if (sb.isNotEmpty())
                result.add(sb.toString())
            return result
        }

    }

}
