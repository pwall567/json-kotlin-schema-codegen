/*
 * @(#) CodeGeneratorExtendedRefTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021 Peter Wall
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
import net.pwall.json.JSON
import net.pwall.json.pointer.JSONPointer
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorExtendedRefTest {

    @Test fun `should generate classes for extended nested class`() {
        val input = File("src/test/resources/test-extended-ref.schema.json")
        val schemaDoc = JSON.parse(input)
        val codeGenerator = CodeGenerator()
        val stringWriter1 = StringWriter()
        val outputDetails1 = OutputDetails(TargetFileName("Main", "kt", dirs), stringWriter1)
        val stringWriter2 = StringWriter()
        val outputDetails2 = OutputDetails(TargetFileName("Nested", "kt", dirs), stringWriter2)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails1, outputDetails2)
        codeGenerator.generateAll(schemaDoc, JSONPointer("/\$defs"))
        expect(CodeGeneratorTestUtil.createHeader("Main.kt") + expected1) { stringWriter1.toString() }
        expect(CodeGeneratorTestUtil.createHeader("Nested.kt") + expected2) { stringWriter2.toString() }
    }

    @Test fun `should generate classes for extended nested class in Java`() {
        val input = File("src/test/resources/test-extended-ref.schema.json")
        val schemaDoc = JSON.parse(input)
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter1 = StringWriter()
        val outputDetails1 = OutputDetails(TargetFileName("Main", "java", dirs), stringWriter1)
        val stringWriter2 = StringWriter()
        val outputDetails2 = OutputDetails(TargetFileName("Nested", "java", dirs), stringWriter2)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails1, outputDetails2)
        codeGenerator.generateAll(schemaDoc, JSONPointer("/\$defs"))
        expect(CodeGeneratorTestUtil.createHeader("Main.java") + expected1Java) { stringWriter1.toString() }
        expect(CodeGeneratorTestUtil.createHeader("Nested.java") + expected2Java) { stringWriter2.toString() }
    }

    companion object {

        const val expected1 =
"""package com.example

data class Main(
    val name: String,
    val extended: Extended
) {

    init {
        require(name.isNotEmpty()) { "name length < minimum 1 - ${'$'}{name.length}" }
    }

    class Extended(
        count: Long,
        val extra: Boolean
    ) : Nested(count) {

        override fun equals(other: Any?): Boolean = this === other || other is Extended &&
                super.equals(other) &&
                extra == other.extra

        override fun hashCode(): Int = super.hashCode() xor
                extra.hashCode()

        override fun toString() = "Extended(count=${'$'}count, extra=${'$'}extra)"

        fun copy(
            count: Long = this.count,
            extra: Boolean = this.extra
        ) = Extended(count, extra)

        operator fun component2() = extra

    }

}
"""

        const val expected2 =
"""package com.example

open class Nested(
    val count: Long
) {

    override fun equals(other: Any?): Boolean = this === other || other is Nested &&
            count == other.count

    override fun hashCode(): Int =
            count.hashCode()

    override fun toString() = "Nested(count=${'$'}count)"

    open fun copy(
        count: Long = this.count
    ) = Nested(count)

    operator fun component1() = count

}
"""

        const val expected1Java =
"""package com.example;

public class Main {

    private final String name;
    private final Extended extended;

    public Main(
            String name,
            Extended extended
    ) {
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        if (name.length() < 1)
            throw new IllegalArgumentException("name length < minimum 1 - " + name.length());
        this.name = name;
        if (extended == null)
            throw new IllegalArgumentException("Must not be null - extended");
        this.extended = extended;
    }

    public String getName() {
        return name;
    }

    public Extended getExtended() {
        return extended;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Main))
            return false;
        Main typedOther = (Main)other;
        if (!name.equals(typedOther.name))
            return false;
        return extended.equals(typedOther.extended);
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        return hash ^ extended.hashCode();
    }

    public static class Extended extends Nested {

        private final boolean extra;

        public Extended(
                long count,
                boolean extra
        ) {
            super(count);
            this.extra = extra;
        }

        public boolean getExtra() {
            return extra;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof Extended))
                return false;
            if (!super.equals(other))
                return false;
            Extended typedOther = (Extended)other;
            return extra == typedOther.extra;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            return hash ^ (extra ? 1 : 0);
        }

    }

}
"""

        const val expected2Java =
"""package com.example;

public class Nested {

    private final long count;

    public Nested(
            long count
    ) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Nested))
            return false;
        Nested typedOther = (Nested)other;
        return count == typedOther.count;
    }

    @Override
    public int hashCode() {
        return (int)count;
    }

}
"""

    }

}
