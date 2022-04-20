package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorOneOfRequiredTest {

    @Test fun `should not generate additional classes for oneOf with no properties`() {
        val input = File("src/test/resources/test-oneof-required.schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetails = OutputDetails(TargetFileName("TestOneofRequired", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        expect(createHeader("TestOneofRequired.kt") + expected) { outputDetails.output() }
    }

    companion object {
        const val expected =
"""package com.example

data class TestOneofRequired(
    val aaa: String? = null,
    val bbb: Long? = null,
    val ccc: Boolean? = null
)
"""

    }

}
