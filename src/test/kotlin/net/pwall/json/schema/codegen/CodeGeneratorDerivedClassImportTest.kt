/*
 * @(#) CodeGeneratorDerivedClassTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2023 Peter Wall
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

class CodeGeneratorDerivedClassImportTest {

    @Test fun `should generate base class and derived class`() {
        val input = File("src/test/resources/test-derived-classes-complex")
        val outputDetailsBase = OutputDetails(TargetFileName("TestBaseClass", "kt", dirs))
        val outputDetailsEnum = OutputDetails(TargetFileName("TestEnum", "kt", dirs))
        val outputDetailsRef = OutputDetails(TargetFileName("TestRefClass", "kt", dirs))
        val outputDetailsDerived = OutputDetails(TargetFileName("TestDerivedClass", "kt", dirs + "derived"))
        CodeGenerator().apply {
            outputResolver = outputCapture(outputDetailsBase, outputDetailsEnum, outputDetailsRef, outputDetailsDerived)
            basePackageName = "com.example"
            generate(input)
        }
        expect(createHeader("TestBaseClass.kt") + expectedBase) { outputDetailsBase.output() }
        expect(createHeader("TestEnum.kt") + expectedEnum) { outputDetailsEnum.output() }
        expect(createHeader("TestRefClass.kt") + expectedNested) { outputDetailsRef.output() }
        expect(createHeader("TestDerivedClass.kt") + expectedDerived) { outputDetailsDerived.output() }
    }

    @Test fun `should generate base class and derived class in Java`() {
        val input = File("src/test/resources/test-derived-classes-complex")
        val outputDetailsBase = OutputDetails(TargetFileName("TestBaseClass", "java", dirs))
        val outputDetailsEnum = OutputDetails(TargetFileName("TestEnum", "java", dirs))
        val outputDetailsRef = OutputDetails(TargetFileName("TestRefClass", "java", dirs))
        val outputDetailsDerived = OutputDetails(TargetFileName("TestDerivedClass", "java", dirs + "derived"))
        CodeGenerator(TargetLanguage.JAVA).apply {
            basePackageName = "com.example"
            outputResolver = outputCapture(outputDetailsBase, outputDetailsEnum, outputDetailsRef, outputDetailsDerived)
            generate(input)
        }
        expect(createHeader("TestBaseClass.java") + expectedBaseJava) { outputDetailsBase.output() }
        expect(createHeader("TestEnum.java") + expectedEnumJava) { outputDetailsEnum.output() }
        expect(createHeader("TestRefClass.java") + expectedNestedJava) { outputDetailsRef.output() }
        expect(createHeader("TestDerivedClass.java") + expectedDerivedJava) { outputDetailsDerived.output() }
    }

    companion object {

        const val expectedBase =
"""package com.example

/**
 * Test base class.
 */
open class TestBaseClass(
    val value: TestEnum,
    val nested: TestRefClass
) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseClass &&
            value == other.value &&
            nested == other.nested

    override fun hashCode(): Int =
            value.hashCode() xor
            nested.hashCode()

    override fun toString() = "TestBaseClass(value=${'$'}value, nested=${'$'}nested)"

    open fun copy(
        value: TestEnum = this.value,
        nested: TestRefClass = this.nested
    ) = TestBaseClass(value, nested)

    operator fun component1() = value

    operator fun component2() = nested

}
"""

        const val expectedNested =
"""package com.example

/**
 * Test referenced class.
 */
data class TestRefClass(
    val value: String
)
"""

        const val expectedEnum =
"""package com.example

enum class TestEnum {
    ONE,
    TWO,
    THREE
}
"""

        const val expectedDerived =
"""package com.example.derived

import com.example.TestBaseClass
import com.example.TestEnum
import com.example.TestRefClass

/**
 * Test derived class.
 */
class TestDerivedClass(
    value: TestEnum,
    nested: TestRefClass,
    val name: String
) : TestBaseClass(value, nested) {

    override fun equals(other: Any?): Boolean = this === other || other is TestDerivedClass &&
            super.equals(other) &&
            name == other.name

    override fun hashCode(): Int = super.hashCode() xor
            name.hashCode()

    override fun toString() = "TestDerivedClass(value=${'$'}value, nested=${'$'}nested, name=${'$'}name)"

    fun copy(
        value: TestEnum = this.value,
        nested: TestRefClass = this.nested,
        name: String = this.name
    ) = TestDerivedClass(value, nested, name)

    operator fun component3() = name

}
"""

        const val expectedBaseJava =
"""package com.example;

/**
 * Test base class.
 */
public class TestBaseClass {

    private final TestEnum value;
    private final TestRefClass nested;

    public TestBaseClass(
            TestEnum value,
            TestRefClass nested
    ) {
        if (value == null)
            throw new IllegalArgumentException("Must not be null - value");
        this.value = value;
        if (nested == null)
            throw new IllegalArgumentException("Must not be null - nested");
        this.nested = nested;
    }

    public TestEnum getValue() {
        return value;
    }

    public TestRefClass getNested() {
        return nested;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestBaseClass))
            return false;
        TestBaseClass cg_typedOther = (TestBaseClass)cg_other;
        if (value != cg_typedOther.value)
            return false;
        return nested.equals(cg_typedOther.nested);
    }

    @Override
    public int hashCode() {
        int hash = value.hashCode();
        return hash ^ nested.hashCode();
    }

    public static class Builder {

        private TestEnum value;
        private TestRefClass nested;

        public Builder withValue(TestEnum value) {
            this.value = value;
            return this;
        }

        public Builder withNested(TestRefClass nested) {
            this.nested = nested;
            return this;
        }

        public TestBaseClass build() {
            return new TestBaseClass(
                    value,
                    nested
            );
        }

    }

}
"""

        const val expectedNestedJava =
"""package com.example;

/**
 * Test referenced class.
 */
public class TestRefClass {

    private final String value;

    public TestRefClass(
            String value
    ) {
        if (value == null)
            throw new IllegalArgumentException("Must not be null - value");
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestRefClass))
            return false;
        TestRefClass cg_typedOther = (TestRefClass)cg_other;
        return value.equals(cg_typedOther.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static class Builder {

        private String value;

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public TestRefClass build() {
            return new TestRefClass(
                    value
            );
        }

    }

}
"""

        const val expectedEnumJava =
"""package com.example;

public enum TestEnum {
    ONE,
    TWO,
    THREE
}
"""

        const val expectedDerivedJava =
"""package com.example.derived;

import com.example.TestBaseClass;
import com.example.TestEnum;
import com.example.TestRefClass;

/**
 * Test derived class.
 */
public class TestDerivedClass extends TestBaseClass {

    private final String name;

    public TestDerivedClass(
            TestEnum value,
            TestRefClass nested,
            String name
    ) {
        super(value, nested);
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestDerivedClass))
            return false;
        if (!super.equals(cg_other))
            return false;
        TestDerivedClass cg_typedOther = (TestDerivedClass)cg_other;
        return name.equals(cg_typedOther.name);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return hash ^ name.hashCode();
    }

    public static class Builder {

        private TestEnum value;
        private TestRefClass nested;
        private String name;

        public Builder withValue(TestEnum value) {
            this.value = value;
            return this;
        }

        public Builder withNested(TestRefClass nested) {
            this.nested = nested;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public TestDerivedClass build() {
            return new TestDerivedClass(
                    value,
                    nested,
                    name
            );
        }

    }

}
"""

    }

}
