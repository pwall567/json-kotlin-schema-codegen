/*
 * @(#) CodeGeneratorMultipleNestedClassTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2022 Peter Wall
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

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorMultipleNestedClassTest {

    @Test fun `should output nested class`() {
        val input = File("src/test/resources/test-nested-classes.schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetails = OutputDetails(TargetFileName("TestNestedClasses", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        expect(createHeader("TestNestedClasses.kt") + expected1) { outputDetails.output() }
    }

    @Test fun `should output nested class using property name`() {
        val input = File("src/test/resources/test-nested-classes.schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetails = OutputDetails(TargetFileName("TestNestedClasses", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.nestedClassNameOption = CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY
        codeGenerator.generate(input)
        expect(createHeader("TestNestedClasses.kt") + expected2) { outputDetails.output() }
    }

    @Test fun `should output nested class for array`() {
        val input = File("src/test/resources/test-nested-classes-array.schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetails = OutputDetails(TargetFileName("TestNestedClassesArray", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        expect(createHeader("TestNestedClassesArray.kt") + expected3) { outputDetails.output() }
    }

    @Test fun `should output nested class for array using property name`() {
        val input = File("src/test/resources/test-nested-classes-array.schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetails = OutputDetails(TargetFileName("TestNestedClassesArray", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.nestedClassNameOption = CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY
        codeGenerator.generate(input)
        expect(createHeader("TestNestedClassesArray.kt") + expected4) { outputDetails.output() }
    }

    companion object {

        const val expected1 =
"""package com.example

/**
 * Test nested classes.
 */
data class TestNestedClasses(
    val alpha: Nested,
    val beta: Nested
) {

    data class Nested(
        val first: String
    )

}
"""

        const val expected2 =
"""package com.example

/**
 * Test nested classes.
 */
data class TestNestedClasses(
    val alpha: Alpha,
    val beta: Alpha
) {

    data class Alpha(
        val first: String
    )

}
"""

        const val expected3 =
"""package com.example

/**
 * Test nested classes.
 */
data class TestNestedClassesArray(
    val alpha: List<Nested>,
    val beta: List<Nested>
) {

    data class Nested(
        val first: String
    )

}
"""

        const val expected4 =
"""package com.example

/**
 * Test nested classes.
 */
data class TestNestedClassesArray(
    val alpha: List<Alpha>,
    val beta: List<Alpha>
) {

    data class Alpha(
        val first: String
    )

}
"""

    }

}
