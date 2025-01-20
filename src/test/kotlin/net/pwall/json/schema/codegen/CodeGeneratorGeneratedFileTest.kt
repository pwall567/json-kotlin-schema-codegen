/*
 * @(#) CodeGeneratorGeneratedFileTest.kt
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

import java.io.File
import java.io.StringWriter

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorGeneratedFileTest {

    @Test fun `should process test directory and write to generated-sources`() {
        val input = File("src/test/resources/test1")
        val outputDirectory = "target/generated-test-sources/kotlin"
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = outputDirectory
        codeGenerator.basePackageName = "net.pwall.json.schema.test"
        codeGenerator.generate(input)
        File("$outputDirectory/net/pwall/json/schema/test/person/Person.kt").readText() shouldBe
                createHeader("Person.kt") + expected2
        codeGenerator.log.debug { "File $outputDirectory/person/Person.kt will be deleted" }
    }

    @Test fun `should output test class to Java`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("Person", "java", dirs + "person"), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("Person.java") + expected2Java
    }

    @Test fun `should use custom templates`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator()
        codeGenerator.setTemplateDirectory(File("src/test/resources/dummy-template"))
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("Person", "kt",
            listOf("net", "pwall", "json", "schema", "test", "person")), stringWriter)
        codeGenerator.basePackageName = "net.pwall.json.schema.test"
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe "// Dummy\n"
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
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof Person))
            return false;
        Person cg_typedOther = (Person)cg_other;
        if (!id.equals(cg_typedOther.id))
            return false;
        return name.equals(cg_typedOther.name);
    }

    @Override
    public int hashCode() {
        int hash = id.hashCode();
        return hash ^ name.hashCode();
    }

    public static class Builder {

        private UUID id;
        private String name;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Person build() {
            return new Person(
                    id,
                    name
            );
        }

    }

}
"""

    }

}
