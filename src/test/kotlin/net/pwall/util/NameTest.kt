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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.expect

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
            expect("gen") { defaultPrefix }
            expect("n") { numberPrefix }
            expect("alpha") { toString() }
            expect(1) { size }
            expect("alpha") { this[0] }
            expect("alpha") { lowerCamelCase }
            expect("alpha") { camelCase }
            expect("Alpha") { UpperCamelCase }
            expect("Alpha") { CamelCase }
            expect("alpha") { lower_snake_case }
            expect("alpha") { snake_case }
            expect("ALPHA") { UPPER_SNAKE_CASE }
            expect("ALPHA") { SNAKE_CASE }
            expect("Alpha") { Capital_Snake_Case }
            expect("Alpha") { Snake_Case }
            expect("alpha") { `lower-kebab-case` }
            expect("alpha") { `kebab-case` }
            expect("ALPHA") { `UPPER-KEBAB-CASE` }
            expect("ALPHA") { `KEBAB-CASE` }
            expect("Alpha") { `Capital-Kebab-Case` }
            expect("Alpha") { `Kebab-Case` }
        }
    }

    @Test fun `should handle compound name`() {
        with(Name("alpha-beta-gamma")) {
            expect("alpha-beta-gamma") { toString() }
            expect(3) { size }
            expect("alpha") { this[0] }
            expect("beta") { this[1] }
            expect("gamma") { this[2] }
            expect("alphaBetaGamma") { lowerCamelCase }
            expect("AlphaBetaGamma") { UpperCamelCase }
            expect("alpha_beta_gamma") { lower_snake_case }
            expect("ALPHA_BETA_GAMMA") { UPPER_SNAKE_CASE }
            expect("Alpha_Beta_Gamma") { Capital_Snake_Case }
            expect("alpha-beta-gamma") { `lower-kebab-case` }
            expect("ALPHA-BETA-GAMMA") { `UPPER-KEBAB-CASE` }
            expect("Alpha-Beta-Gamma") { `Capital-Kebab-Case` }
        }
    }

    @Test fun `should handle compound name with upper case acronym`() {
        with(Name("UUID-label")) {
            expect(2) { size }
            expect("UUID") { this[0] }
            expect("label") { this[1] }
            expect("uuidLabel") { lowerCamelCase }
            expect("UUIDLabel") { UpperCamelCase }
            expect("uuid_label") { lower_snake_case }
            expect("UUID_LABEL") { UPPER_SNAKE_CASE }
            expect("UUID_Label") { Capital_Snake_Case }
            expect("uuid-label") { `lower-kebab-case` }
            expect("UUID-LABEL") { `UPPER-KEBAB-CASE` }
            expect("UUID-Label") { `Capital-Kebab-Case` }
        }
    }

    @Test fun `should handle compound name input as camelCase`() {
        with(Name("alphaBetaGamma")) {
            expect(3) { size }
            expect("alpha") { this[0] }
            expect("Beta") { this[1] }
            expect("Gamma") { this[2] }
            expect("alphaBetaGamma") { lowerCamelCase }
            expect("AlphaBetaGamma") { UpperCamelCase }
            expect("alpha_beta_gamma") { lower_snake_case }
            expect("ALPHA_BETA_GAMMA") { UPPER_SNAKE_CASE }
            expect("Alpha_Beta_Gamma") { Capital_Snake_Case }
            expect("alpha-beta-gamma") { `lower-kebab-case` }
            expect("ALPHA-BETA-GAMMA") { `UPPER-KEBAB-CASE` }
            expect("Alpha-Beta-Gamma") { `Capital-Kebab-Case` }
        }
    }

    @Test fun `should handle compound name including acronym`() {
        with(Name("JSONValue")) {
            expect(2) { size }
            expect("JSON") { this[0] }
            expect("Value") { this[1] }
            expect("jsonValue") { lowerCamelCase }
            expect("JSONValue") { UpperCamelCase }
            expect("json_value") { lower_snake_case }
            expect("JSON_VALUE") { UPPER_SNAKE_CASE }
            expect("JSON_Value") { Capital_Snake_Case }
            expect("json-value") { `lower-kebab-case` }
            expect("JSON-Value") { `Capital-Kebab-Case` }
        }
    }

    @Test fun `should handle compound name with number`() {
        with(Name("alpha-27-delta")) {
            expect(3) { size }
            expect("alpha") { this[0] }
            expect("27") { this[1] }
            expect("delta") { this[2] }
            expect("alpha27Delta") { lowerCamelCase }
            expect("Alpha27Delta") { UpperCamelCase }
            expect("alpha_27_delta") { lower_snake_case }
            expect("ALPHA_27_DELTA") { UPPER_SNAKE_CASE }
            expect("Alpha_27_Delta") { Capital_Snake_Case }
            expect("alpha-27-delta") { `lower-kebab-case` }
            expect("ALPHA-27-DELTA") { `UPPER-KEBAB-CASE` }
            expect("Alpha-27-Delta") { `Capital-Kebab-Case` }
        }
    }

    @Test fun `should handle empty name`() {
        with(Name("")) {
            expect(0) { size }
            assertTrue(Regex("^gen[0-9]+$").containsMatchIn(lowerCamelCase))
            assertTrue(Regex("^Gen[0-9]+$").containsMatchIn(UpperCamelCase))
            assertTrue(Regex("^gen[0-9]+$").containsMatchIn(lower_snake_case))
            assertTrue(Regex("^GEN[0-9]+$").containsMatchIn(UPPER_SNAKE_CASE))
            assertTrue(Regex("^Gen[0-9]+$").containsMatchIn(Capital_Snake_Case))
            assertTrue(Regex("^gen[0-9]+$").containsMatchIn(`lower-kebab-case`))
            assertTrue(Regex("^GEN[0-9]+$").containsMatchIn(`UPPER-KEBAB-CASE`))
            assertTrue(Regex("^Gen[0-9]+$").containsMatchIn(`Capital-Kebab-Case`))
        }
    }

    @Test fun `should handle empty name with specified prefix`() {
        with(Name("", defaultPrefix = "cg_")) {
            expect("cg_") { defaultPrefix }
            expect(0) { size }
            assertTrue(Regex("^cg_[0-9]+$").containsMatchIn(lowerCamelCase))
            assertTrue(Regex("^Cg_[0-9]+$").containsMatchIn(UpperCamelCase))
            assertTrue(Regex("^cg_[0-9]+$").containsMatchIn(lower_snake_case))
            assertTrue(Regex("^CG_[0-9]+$").containsMatchIn(UPPER_SNAKE_CASE))
            assertTrue(Regex("^Cg_[0-9]+$").containsMatchIn(Capital_Snake_Case))
            assertTrue(Regex("^cg_[0-9]+$").containsMatchIn(`lower-kebab-case`))
            assertTrue(Regex("^CG_[0-9]+$").containsMatchIn(`UPPER-KEBAB-CASE`))
            assertTrue(Regex("^Cg_[0-9]+$").containsMatchIn(`Capital-Kebab-Case`))
        }
    }

    @Test fun `should handle name starting with digit`() {
        with(Name("99luftballons")) {
            expect(1) { size }
            expect("99luftballons") { this[0] }
            expect("n99luftballons") { lowerCamelCase }
            expect("N99luftballons") { UpperCamelCase }
            expect("n99luftballons") { lower_snake_case }
            expect("N99LUFTBALLONS") { UPPER_SNAKE_CASE }
            expect("N99luftballons") { Capital_Snake_Case }
            expect("n99luftballons") { `lower-kebab-case` }
            expect("N99LUFTBALLONS") { `UPPER-KEBAB-CASE` }
            expect("N99luftballons") { `Capital-Kebab-Case` }
        }
    }

    @Test fun `should handle name starting with digit using specified prefix`() {
        with(Name("99luftballons", numberPrefix = "no_")) {
            expect(1) { size }
            expect("99luftballons") { this[0] }
            expect("no_99luftballons") { lowerCamelCase }
            expect("No_99luftballons") { UpperCamelCase }
            expect("no_99luftballons") { lower_snake_case }
            expect("NO_99LUFTBALLONS") { UPPER_SNAKE_CASE }
            expect("No_99luftballons") { Capital_Snake_Case }
            expect("no_99luftballons") { `lower-kebab-case` }
            expect("NO_99LUFTBALLONS") { `UPPER-KEBAB-CASE` }
            expect("No_99luftballons") { `Capital-Kebab-Case` }
        }
    }

    @Test fun `should create other patterns`() {
        with(Name("alpha-beta-gamma")) {
            expect("Alpha.Beta.Gamma") { separatedCase('.') { capitalise() } }
        }
    }

    @Test fun `should correctly test isDigit`() {
        for (i in 0 until '0'.code)
            assertFalse(i.toChar().isDigit())
        for (i in '0'.code..'9'.code)
            assertTrue(i.toChar().isDigit())
        for (i in ('9'.code + 1)..Char.MAX_VALUE.code)
            assertFalse(i.toChar().isDigit())
    }

    @Test fun `should correctly test isUpper`() {
        for (i in 0 until 'A'.code)
            assertFalse(i.toChar().isUpper())
        for (i in 'A'.code..'Z'.code)
            assertTrue(i.toChar().isUpper())
        for (i in ('Z'.code + 1)..Char.MAX_VALUE.code)
            assertFalse(i.toChar().isUpper())
    }

    @Test fun `should correctly test isLower`() {
        for (i in 0 until 'a'.code)
            assertFalse(i.toChar().isLower())
        for (i in 'a'.code..'z'.code)
            assertTrue(i.toChar().isLower())
        for (i in ('z'.code + 1)..Char.MAX_VALUE.code)
            assertFalse(i.toChar().isLower())
    }

    @Test fun `should convert to upper case`() {
        for (i in LOWER.indices)
            expect(UPPER[i]) { LOWER[i].toUpper() }
    }

    @Test fun `should convert to lower case`() {
        for (i in UPPER.indices)
            expect(LOWER[i]) { UPPER[i].toLower() }
    }

    @Test fun `should capitalise string`() {
        expect("Alpha") { "alpha".capitalise() }
        expect("Fred") { "Fred".capitalise() }
    }

    @Test fun `should uncapitalise string`() {
        expect("alpha") { "Alpha".uncapitalise() }
        expect("gamma") { "gamma".uncapitalise() }
    }

    @Test fun `should uncapitalise all upper case as all lower case`() {
        expect("json") { "JSON".uncapitalise() }
        expect("iso8601") { "ISO8601".uncapitalise() }
    }

    companion object {
        const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    }

}
