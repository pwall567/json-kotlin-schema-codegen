/*
 * @(#) NameTest.kt
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

import kotlin.test.Test

import io.kstuff.test.shouldBe

import net.pwall.util.Name.Companion.capitalise
import net.pwall.util.Name.Companion.isDigit
import net.pwall.util.Name.Companion.isLower
import net.pwall.util.Name.Companion.isUpper
import net.pwall.util.Name.Companion.toLower
import net.pwall.util.Name.Companion.toUpper
import net.pwall.util.Name.Companion.uncapitalise

class NameTest {

    @Test fun `should handle simple name`() {
        with(Name("alpha")) {
            defaultPrefix shouldBe "gen"
            numberPrefix shouldBe "n"
            toString() shouldBe "alpha"
            size shouldBe 1
            this[0] shouldBe "alpha"
            lowerCamelCase shouldBe "alpha"
            camelCase shouldBe "alpha"
            UpperCamelCase shouldBe "Alpha"
            CamelCase shouldBe "Alpha"
            lower_snake_case shouldBe "alpha"
            snake_case shouldBe "alpha"
            UPPER_SNAKE_CASE shouldBe "ALPHA"
            SNAKE_CASE shouldBe "ALPHA"
            Capital_Snake_Case shouldBe "Alpha"
            Snake_Case shouldBe "Alpha"
            `lower-kebab-case` shouldBe "alpha"
            `kebab-case` shouldBe "alpha"
            `UPPER-KEBAB-CASE` shouldBe "ALPHA"
            `KEBAB-CASE` shouldBe "ALPHA"
            `Capital-Kebab-Case` shouldBe "Alpha"
            `Kebab-Case` shouldBe "Alpha"
        }
    }

    @Test fun `should handle compound name`() {
        with(Name("alpha-beta-gamma")) {
            toString() shouldBe "alpha-beta-gamma"
            size shouldBe 3
            this[0] shouldBe "alpha"
            this[1] shouldBe "beta"
            this[2] shouldBe "gamma"
            lowerCamelCase shouldBe "alphaBetaGamma"
            UpperCamelCase shouldBe "AlphaBetaGamma"
            lower_snake_case shouldBe "alpha_beta_gamma"
            UPPER_SNAKE_CASE shouldBe "ALPHA_BETA_GAMMA"
            Capital_Snake_Case shouldBe "Alpha_Beta_Gamma"
            `lower-kebab-case` shouldBe "alpha-beta-gamma"
            `UPPER-KEBAB-CASE` shouldBe "ALPHA-BETA-GAMMA"
            `Capital-Kebab-Case` shouldBe "Alpha-Beta-Gamma"
        }
    }

    @Test fun `should handle compound name with upper case acronym`() {
        with(Name("UUID-label")) {
            size shouldBe 2
            this[0] shouldBe "UUID"
            this[1] shouldBe "label"
            lowerCamelCase shouldBe "uuidLabel"
            UpperCamelCase shouldBe "UUIDLabel"
            lower_snake_case shouldBe "uuid_label"
            UPPER_SNAKE_CASE shouldBe "UUID_LABEL"
            Capital_Snake_Case shouldBe "UUID_Label"
            `lower-kebab-case` shouldBe "uuid-label"
            `UPPER-KEBAB-CASE` shouldBe "UUID-LABEL"
            `Capital-Kebab-Case` shouldBe "UUID-Label"
        }
    }

    @Test fun `should handle compound name input as camelCase`() {
        with(Name("alphaBetaGamma")) {
            size shouldBe 3
            this[0] shouldBe "alpha"
            this[1] shouldBe "Beta"
            this[2] shouldBe "Gamma"
            lowerCamelCase shouldBe "alphaBetaGamma"
            UpperCamelCase shouldBe "AlphaBetaGamma"
            lower_snake_case shouldBe "alpha_beta_gamma"
            UPPER_SNAKE_CASE shouldBe "ALPHA_BETA_GAMMA"
            Capital_Snake_Case shouldBe "Alpha_Beta_Gamma"
            `lower-kebab-case` shouldBe "alpha-beta-gamma"
            `UPPER-KEBAB-CASE` shouldBe "ALPHA-BETA-GAMMA"
            `Capital-Kebab-Case` shouldBe "Alpha-Beta-Gamma"
        }
    }

    @Test fun `should handle compound name including acronym`() {
        with(Name("JSONValue")) {
            size shouldBe 2
            this[0] shouldBe "JSON"
            this[1] shouldBe "Value"
            lowerCamelCase shouldBe "jsonValue"
            UpperCamelCase shouldBe "JSONValue"
            lower_snake_case shouldBe "json_value"
            UPPER_SNAKE_CASE shouldBe "JSON_VALUE"
            Capital_Snake_Case shouldBe "JSON_Value"
            `lower-kebab-case` shouldBe "json-value"
            `Capital-Kebab-Case` shouldBe "JSON-Value"
        }
    }

    @Test fun `should handle compound name with number`() {
        with(Name("alpha-27-delta")) {
            size shouldBe 3
            this[0] shouldBe "alpha"
            this[1] shouldBe "27"
            this[2] shouldBe "delta"
            lowerCamelCase shouldBe "alpha27Delta"
            UpperCamelCase shouldBe "Alpha27Delta"
            lower_snake_case shouldBe "alpha_27_delta"
            UPPER_SNAKE_CASE shouldBe "ALPHA_27_DELTA"
            Capital_Snake_Case shouldBe "Alpha_27_Delta"
            `lower-kebab-case` shouldBe "alpha-27-delta"
            `UPPER-KEBAB-CASE` shouldBe "ALPHA-27-DELTA"
            `Capital-Kebab-Case` shouldBe "Alpha-27-Delta"
        }
    }

    @Test fun `should handle empty name`() {
        with(Name("")) {
            size shouldBe 0
            Regex("^gen[0-9]+$").containsMatchIn(lowerCamelCase) shouldBe true
            Regex("^Gen[0-9]+$").containsMatchIn(UpperCamelCase) shouldBe true
            Regex("^gen[0-9]+$").containsMatchIn(lower_snake_case) shouldBe true
            Regex("^GEN[0-9]+$").containsMatchIn(UPPER_SNAKE_CASE) shouldBe true
            Regex("^Gen[0-9]+$").containsMatchIn(Capital_Snake_Case) shouldBe true
            Regex("^gen[0-9]+$").containsMatchIn(`lower-kebab-case`) shouldBe true
            Regex("^GEN[0-9]+$").containsMatchIn(`UPPER-KEBAB-CASE`) shouldBe true
            Regex("^Gen[0-9]+$").containsMatchIn(`Capital-Kebab-Case`) shouldBe true
        }
    }

    @Test fun `should handle empty name with specified prefix`() {
        with(Name("", defaultPrefix = "cg_")) {
            defaultPrefix shouldBe "cg_"
            size shouldBe 0
            Regex("^cg_[0-9]+$").containsMatchIn(lowerCamelCase) shouldBe true
            Regex("^Cg_[0-9]+$").containsMatchIn(UpperCamelCase) shouldBe true
            Regex("^cg_[0-9]+$").containsMatchIn(lower_snake_case) shouldBe true
            Regex("^CG_[0-9]+$").containsMatchIn(UPPER_SNAKE_CASE) shouldBe true
            Regex("^Cg_[0-9]+$").containsMatchIn(Capital_Snake_Case) shouldBe true
            Regex("^cg_[0-9]+$").containsMatchIn(`lower-kebab-case`) shouldBe true
            Regex("^CG_[0-9]+$").containsMatchIn(`UPPER-KEBAB-CASE`) shouldBe true
            Regex("^Cg_[0-9]+$").containsMatchIn(`Capital-Kebab-Case`) shouldBe true
        }
    }

    @Test fun `should handle name starting with digit`() {
        with(Name("99luftballons")) {
            size shouldBe 1
            this[0] shouldBe "99luftballons"
            lowerCamelCase shouldBe "n99luftballons"
            UpperCamelCase shouldBe "N99luftballons"
            lower_snake_case shouldBe "n99luftballons"
            UPPER_SNAKE_CASE shouldBe "N99LUFTBALLONS"
            Capital_Snake_Case shouldBe "N99luftballons"
            `lower-kebab-case` shouldBe "n99luftballons"
            `UPPER-KEBAB-CASE` shouldBe "N99LUFTBALLONS"
            `Capital-Kebab-Case` shouldBe "N99luftballons"
        }
    }

    @Test fun `should handle name starting with digit using specified prefix`() {
        with(Name("99luftballons", numberPrefix = "no_")) {
            size shouldBe 1
            this[0] shouldBe "99luftballons"
            lowerCamelCase shouldBe "no_99luftballons"
            UpperCamelCase shouldBe "No_99luftballons"
            lower_snake_case shouldBe "no_99luftballons"
            UPPER_SNAKE_CASE shouldBe "NO_99LUFTBALLONS"
            Capital_Snake_Case shouldBe "No_99luftballons"
            `lower-kebab-case` shouldBe "no_99luftballons"
            `UPPER-KEBAB-CASE` shouldBe "NO_99LUFTBALLONS"
            `Capital-Kebab-Case` shouldBe "No_99luftballons"
        }
    }

    @Test fun `should create other patterns`() {
        with(Name("alpha-beta-gamma")) {
            separatedCase('.') { capitalise() } shouldBe "Alpha.Beta.Gamma"
        }
    }

    @Test fun `should correctly test isDigit`() {
        for (i in 0 until '0'.code)
            i.toChar().isDigit() shouldBe false
        for (i in '0'.code..'9'.code)
            i.toChar().isDigit() shouldBe true
        for (i in ('9'.code + 1)..Char.MAX_VALUE.code)
            i.toChar().isDigit() shouldBe false
    }

    @Test fun `should correctly test isUpper`() {
        for (i in 0 until 'A'.code)
            i.toChar().isUpper() shouldBe false
        for (i in 'A'.code..'Z'.code)
            i.toChar().isUpper() shouldBe true
        for (i in ('Z'.code + 1)..Char.MAX_VALUE.code)
            i.toChar().isUpper() shouldBe false
    }

    @Test fun `should correctly test isLower`() {
        for (i in 0 until 'a'.code)
            i.toChar().isLower() shouldBe false
        for (i in 'a'.code..'z'.code)
            i.toChar().isLower() shouldBe true
        for (i in ('z'.code + 1)..Char.MAX_VALUE.code)
            i.toChar().isLower() shouldBe false
    }

    @Test fun `should convert to upper case`() {
        for (i in LOWER.indices)
            LOWER[i].toUpper() shouldBe UPPER[i]
    }

    @Test fun `should convert to lower case`() {
        for (i in UPPER.indices)
            UPPER[i].toLower() shouldBe LOWER[i]
    }

    @Test fun `should capitalise string`() {
        "alpha".capitalise() shouldBe "Alpha"
        "Fred".capitalise() shouldBe "Fred"
    }

    @Test fun `should uncapitalise string`() {
        "Alpha".uncapitalise() shouldBe "alpha"
        "gamma".uncapitalise() shouldBe "gamma"
    }

    @Test fun `should uncapitalise all upper case as all lower case`() {
        "JSON".uncapitalise() shouldBe "json"
        "ISO8601".uncapitalise() shouldBe "iso8601"
    }

    companion object {
        const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    }

}
