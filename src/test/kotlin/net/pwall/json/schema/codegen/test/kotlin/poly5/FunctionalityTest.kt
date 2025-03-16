/*
 * @(#) poly5/FunctionalityTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2025 Peter Wall
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

package net.pwall.json.schema.codegen.test.kotlin.poly5

import kotlin.test.Test
import io.kstuff.test.shouldBe
import io.kstuff.test.shouldBeType
import net.pwall.json.schema.codegen.test.kotlin.TestPolymorphicGroup5

class FunctionalityTest {

    @Test fun `should create poly5 classes`() {
        val testPhone: TestPolymorphicGroup5 = PhoneContact("EXTRA", "PHONE", "+61", "98765432")
        testPhone.extraProperty shouldBe "EXTRA"
        testPhone.contactType shouldBe "PHONE"
        testPhone.shouldBeType<PhoneContact>().countryCode shouldBe "+61"
        testPhone.shouldBeType<PhoneContact>().localNumber shouldBe "98765432"
    }

}
