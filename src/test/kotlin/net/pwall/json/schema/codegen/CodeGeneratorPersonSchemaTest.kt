/*
 * @(#) CodeGeneratorPersonSchemaTest.kt
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

class CodeGeneratorPersonSchemaTest {

    @Test fun `should output simple data class`() {
        val input = File("src/test/resources/simple")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestPerson", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestPerson.kt") + expected1
    }

    @Test fun `should output simple data class to Java`() {
        val input = File("src/test/resources/simple")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestPerson", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestPerson.java") + expectedJava
    }

    @Test fun `should output simple data class to TypeScript`() {
        val input = File("src/test/resources/simple")
        val codeGenerator = CodeGenerator(TargetLanguage.TYPESCRIPT)
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestPerson", "ts"), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestPerson.ts") + expectedTypeScript
    }

    companion object {

        const val expected1 =
"""package com.example

/**
 * A test class.
 */
data class TestPerson(
    val name: String,
    val nickname: String? = null,
    val age: Int
) {

    init {
        require(name.length in 1..80) { "name length not in range 1..80 - ${'$'}{name.length}" }
        require(age in 0..120) { "age not in range 0..120 - ${'$'}age" }
    }

}
"""

        const val expectedJava =
"""package com.example;

/**
 * A test class.
 */
public class TestPerson {

    private final String name;
    private final String nickname;
    private final int age;

    public TestPerson(
            String name,
            String nickname,
            int age
    ) {
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        if (name.length() < 1 || name.length() > 80)
            throw new IllegalArgumentException("name length not in range 1..80 - " + name.length());
        this.name = name;
        this.nickname = nickname;
        if (age < 0 || age > 120)
            throw new IllegalArgumentException("age not in range 0..120 - " + age);
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestPerson))
            return false;
        TestPerson cg_typedOther = (TestPerson)cg_other;
        if (!name.equals(cg_typedOther.name))
            return false;
        if (nickname == null ? cg_typedOther.nickname != null : !nickname.equals(cg_typedOther.nickname))
            return false;
        return age == cg_typedOther.age;
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        hash ^= (nickname != null ? nickname.hashCode() : 0);
        return hash ^ age;
    }

    public static class Builder {

        private String name;
        private String nickname;
        private int age;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder withAge(int age) {
            this.age = age;
            return this;
        }

        public TestPerson build() {
            return new TestPerson(
                    name,
                    nickname,
                    age
            );
        }

    }

}
"""

        const val expectedTypeScript =
"""
/**
 * A test class.
 */
export interface TestPerson {
    name: string;
    nickname?: string;
    age: number;
}
"""

    }

}
