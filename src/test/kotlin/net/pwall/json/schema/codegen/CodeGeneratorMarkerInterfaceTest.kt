package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorMarkerInterfaceTest {

    @Test fun `should output marker interface`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy", emptyList(), "TestEmpty", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.markerInterface = "com.example.Marker"
        codeGenerator.generate(input)
        expect(createHeader("TestEmpty") + expected) { stringWriter.toString() }
    }

    @Test fun `should output marker interface in Java`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy", emptyList(), "TestEmpty", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.markerInterface = "com.example.Marker"
        codeGenerator.generate(input)
        expect(createHeader("TestEmpty") + expectedJava) { stringWriter.toString() }
    }

    @Test fun `should output marker interface in different package`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy", emptyList(), "TestEmpty", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.markerInterface = "com.example.other.Marker"
        codeGenerator.generate(input)
        expect(createHeader("TestEmpty") + expectedExternal) { stringWriter.toString() }
    }

    @Test fun `should output marker interface in different package in Java`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy", emptyList(), "TestEmpty", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.markerInterface = "com.example.other.Marker"
        codeGenerator.generate(input)
        expect(createHeader("TestEmpty") + expectedExternalJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test empty object.
 */
open class TestEmpty : Marker
"""

        const val expectedJava =
"""package com.example;

/**
 * Test empty object.
 */
public class TestEmpty implements Marker {

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestEmpty))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
"""

        const val expectedExternal =
"""package com.example

import com.example.other.Marker

/**
 * Test empty object.
 */
open class TestEmpty : Marker
"""

        const val expectedExternalJava =
"""package com.example;

import com.example.other.Marker;

/**
 * Test empty object.
 */
public class TestEmpty implements Marker {

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestEmpty))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
"""

    }

}
