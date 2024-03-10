/*
 * @(#) CodeGeneratorDefaultTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021 Peter Wall
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

package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter
import java.net.URI

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorDefaultTest {

    @Test fun `should output class with default value`() {
        val input = File("src/test/resources/test-default.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestDefault", "kt", dirs), stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.addCustomClassByURI(URI("http://pwall.net/test-default#/properties/jjj"),
                "com.example.util.TestString")
        codeGenerator.generate(listOf(input))
        expect(createHeader("TestDefault.kt") + expectedDefault) { stringWriter.toString() }
    }

    companion object {

        const val expectedDefault =
"""package com.example

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.net.URI
import java.util.UUID

import com.example.util.TestString

/**
 * Test default values.
 */
data class TestDefault(
    /** Integer value. */
    val aaa: Long = 0,
    /** Nullable string value. */
    val bbb: String? = null,
    /** String value. */
    val ccc: String = "CCC",
    /** Array value. */
    val ddd: List<Long> = listOf(123, 456),
    /** Date-time value. */
    val eee: OffsetDateTime = OffsetDateTime.parse("2021-09-22T10:26:09.123+10:00"),
    /** Date value. */
    val fff: LocalDate = LocalDate.parse("2021-09-22"),
    /** Time value. */
    val ggg: OffsetTime = OffsetTime.parse("10:27:26"),
    /** URI value. */
    val hhh: URI = URI("http://json-schema.org/draft/2019-09/schema"),
    /** UUID value. */
    val iii: UUID = UUID.fromString("9be4c4b6-1b37-11ec-b9d6-c71d791f2a0a"),
    /** String value. */
    val jjj: TestString = TestString("xyz"),
    /** Enum array value. */
    val kkk: List<Kkk> = listOf(Kkk.ABC)
) {

    /**
     * Enum value.
     */
    enum class Kkk {
        ABC,
        XYZ
    }

}
"""

    }

}
