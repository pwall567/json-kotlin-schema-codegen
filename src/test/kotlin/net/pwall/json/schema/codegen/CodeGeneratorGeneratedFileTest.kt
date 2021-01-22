/*
 * @(#) CodeGeneratorGeneratedFileTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020 Peter Wall
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

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorGeneratedFileTest {

    @AfterTest fun `clean up generated sources directory`() {
        File("target/generated-test-sources/json-kotlin-schema").deleteAll()
    }

    private fun File.deleteAll() {
        when {
            isDirectory -> {
                listFiles()?.forEach { it.deleteAll() }
                delete()
            }
            isFile -> delete()
        }
    }

    @Test fun `should process test directory and write to generated-sources`() {
        val input = File("src/test/resources/test1")
        val outputDirectory = "target/generated-test-sources/json-kotlin-schema/net/pwall/json/schema/test"
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = outputDirectory
        codeGenerator.basePackageName = "net.pwall.json.schema.test"
        codeGenerator.generate(input)
        expect(createHeader("Person") + expected2) { File("$outputDirectory/person/Person.kt").readText() }
        codeGenerator.log.debug { "File $outputDirectory/person/Person.kt will be deleted" }
    }

    @Test fun `should output test class to Java`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy1", listOf("person"), "Person", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(createHeader("Person") + expected2Java) { stringWriter.toString() }
    }

    @Test fun `should use custom templates`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator()
        codeGenerator.setTemplateDirectory(File("src/test/resources/dummy-template"))
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy", listOf("person"), "Person", "kt", stringWriter)
        codeGenerator.basePackageName = "net.pwall.json.schema.test"
        codeGenerator.generate(input)
        expect("// Dummy\n") { stringWriter.toString() }
    }

    companion object {

        const val expected2 =
"""package net.pwall.json.schema.test.person

import java.util.UUID

/**
 * A class to represent a person
 */
data class Person(
    /** Id of the person */
    val id: UUID,
    /** Name of the person */
    val name: String
)
"""

        const val expected2Java =
"""package com.example.person;

import java.util.UUID;

/**
 * A class to represent a person
 */
public class Person {

    private final UUID id;
    private final String name;

    public Person(
            UUID id,
            String name
    ) {
        if (id == null)
            throw new IllegalArgumentException("Must not be null - id");
        this.id = id;
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        this.name = name;
    }

    /**
     * Id of the person
     */
    public UUID getId() {
        return id;
    }

    /**
     * Name of the person
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Person))
            return false;
        Person typedOther = (Person)other;
        if (!id.equals(typedOther.id))
            return false;
        return name.equals(typedOther.name);
    }

    @Override
    public int hashCode() {
        int hash = id.hashCode();
        return hash ^ name.hashCode();
    }

}
"""

    }

}
