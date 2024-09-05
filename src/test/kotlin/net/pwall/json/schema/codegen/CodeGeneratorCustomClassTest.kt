/*
 * @(#) CodeGeneratorCustomClassTest.kt
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
import java.nio.file.FileSystems

import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.validation.FormatValidator
import net.pwall.json.schema.validation.PatternValidator

class CodeGeneratorCustomClassTest {

    @Test fun `should use specified custom class`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("Person", "kt", dirs + "person"), stringWriter)
        codeGenerator.addCustomClassByURI(URI("http://pwall.net/test/schema/utility#/\$defs/personId"),
                "com.example.person.PersonId")
        codeGenerator.addCustomClassByURI(URI("#/properties/name"), "com.example.person.PersonName")
        codeGenerator.generate(input)
        expect(createHeader("Person.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class when specified as name and package`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("Person", "kt", dirs + "person"), stringWriter)
        codeGenerator.addCustomClassByURI(URI("http://pwall.net/test/schema/utility#/\$defs/personId"),
                "com.example.person.PersonId")
        codeGenerator.addCustomClassByURI(URI("#/properties/name"), "PersonName", "com.example.person")
        codeGenerator.generate(input)
        expect(createHeader("Person.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class when specified as ClassName`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("Person", "kt", dirs + "person"), stringWriter)
        codeGenerator.addCustomClassByURI(URI("http://pwall.net/test/schema/utility#/\$defs/personId"),
                "com.example.person.PersonId")
        codeGenerator.addCustomClassByURI(URI("#/properties/name"), ClassName("PersonName", "com.example.person"))
        codeGenerator.generate(input)
        expect(createHeader("Person.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class when specified in config file`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(File("src/test/resources/config/custom-class-uri-config.json"))
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("Person", "kt", dirs + "person"), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("Person.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class in Java`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("Person", "java", dirs + "person"), stringWriter)
        codeGenerator.addCustomClassByURI(URI("http://pwall.net/test/schema/utility#/${'$'}defs/personId"),
                "com.example.person.PersonId")
        codeGenerator.addCustomClassByURI(URI("http://pwall.net/test/schema/person#/properties/name"),
                "PersonName", "com.example.person")
        codeGenerator.generate(input)
        expect(createHeader("Person.java") + expectedJava) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for extension`() {
        val input = File("src/test/resources/test-custom-class.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.addCustomClassByExtension("x-test", "money", "com.example.util.Money")
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for extension when specified as name and package`() {
        val input = File("src/test/resources/test-custom-class.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.addCustomClassByExtension("x-test", "money", "Money", "com.example.util")
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for extension when specified as ClassName`() {
        val input = File("src/test/resources/test-custom-class.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.addCustomClassByExtension("x-test", "money", ClassName("Money", "com.example.util"))
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for extension when specified in config file`() {
        val input = File("src/test/resources/test-custom-class.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(File("src/test/resources/config/custom-class-extension-config.json"))
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for extension in Java`() {
        val input = File("src/test/resources/test-custom-class.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "java", dirs), stringWriter)
        codeGenerator.addCustomClassByExtension("x-test", "money", "Money", "com.example.util")
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.java") + expectedJavaForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom validation for format`() {
        val input = File("src/test/resources/test-custom-class-format.schema.json")
        val parser = Parser()
        parser.nonstandardFormatHandler = { keyword ->
            if (keyword == "money")
                FormatValidator.DelegatingFormatChecker(keyword,
                    PatternValidator(null, JSONPointer.root, Regex("^[0-9]{1,16}\\.[0-9]{2}$")))
            else
                null
        }
        val codeGenerator = CodeGenerator()
        codeGenerator.schemaParser = parser
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForFormat) { stringWriter.toString() }
    }

    @Test fun `should use specified custom validation for format in Java`() {
        val input = File("src/test/resources/test-custom-class-format.schema.json")
        val parser = Parser()
        parser.nonstandardFormatHandler = { keyword ->
            if (keyword == "money")
                FormatValidator.DelegatingFormatChecker(keyword,
                    PatternValidator(null, JSONPointer.root, Regex("^[0-9]{1,16}\\.[0-9]{2}$")))
            else
                null
        }
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        codeGenerator.schemaParser = parser
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.java") + expectedJavaForFormat) { stringWriter.toString() }
    }

    @Test fun `should use specified custom validation for format when specified in config file by path`() {
        val input = File("src/test/resources/test-custom-class-format.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(
                FileSystems.getDefault().getPath("src/test/resources/config/nonstandard-format-config.json"))
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForFormat) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for format`() {
        val input = File("src/test/resources/test-custom-class-format.schema.json")
        val parser = Parser()
        parser.nonstandardFormatHandler = { keyword ->
            if (keyword == "money")
                FormatValidator.DelegatingFormatChecker(keyword,
                        PatternValidator(null, JSONPointer.root, Regex("^[0-9]{1,16}\\.[0-9]{2}$")))
            else
                null
        }
        val codeGenerator = CodeGenerator()
        codeGenerator.schemaParser = parser
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.addCustomClassByFormat("money", "com.example.util.Money")
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for format when specified as name and package`() {
        val input = File("src/test/resources/test-custom-class-format.schema.json")
        val parser = Parser()
        parser.nonstandardFormatHandler = { keyword ->
            if (keyword == "money")
                FormatValidator.DelegatingFormatChecker(keyword,
                        PatternValidator(null, JSONPointer.root, Regex("^[0-9]{1,16}\\.[0-9]{2}$")))
            else
                null
        }
        val codeGenerator = CodeGenerator()
        codeGenerator.schemaParser = parser
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.addCustomClassByFormat("money", "Money", "com.example.util")
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for format when specified as ClassName`() {
        val input = File("src/test/resources/test-custom-class-format.schema.json")
        val parser = Parser()
        parser.nonstandardFormatHandler = { keyword ->
            if (keyword == "money")
                FormatValidator.DelegatingFormatChecker(keyword,
                        PatternValidator(null, JSONPointer.root, Regex("^[0-9]{1,16}\\.[0-9]{2}$")))
            else
                null
        }
        val codeGenerator = CodeGenerator()
        codeGenerator.schemaParser = parser
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.addCustomClassByFormat("money", ClassName("Money", "com.example.util"))
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for format when specified in config file`() {
        val input = File("src/test/resources/test-custom-class-format.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(File("src/test/resources/config/custom-class-format-config.json"))
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.kt") + expectedForExtension) { stringWriter.toString() }
    }

    @Test fun `should use specified custom class for format in Java`() {
        val input = File("src/test/resources/test-custom-class-format.schema.json")
        val parser = Parser()
        parser.nonstandardFormatHandler = { keyword ->
            if (keyword == "money")
                FormatValidator.DelegatingFormatChecker(keyword,
                        PatternValidator(null, JSONPointer.root, Regex("^[0-9]{1,16}\\.[0-9]{2}$")))
            else
                null
        }
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        codeGenerator.schemaParser = parser
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "java", dirs), stringWriter)
        codeGenerator.addCustomClassByFormat("money", ClassName("Money", "com.example.util"))
        codeGenerator.generate(input)
        expect(createHeader("TestCustom.java") + expectedJavaForExtension) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example.person

/**
 * A class to represent a person
 */
data class Person(
    /** Id of the person */
    val id: PersonId,
    /** Name of the person */
    val name: PersonName
)
"""

        const val expectedJava =
"""package com.example.person;

/**
 * A class to represent a person
 */
public class Person {

    private final PersonId id;
    private final PersonName name;

    public Person(
            PersonId id,
            PersonName name
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
    public PersonId getId() {
        return id;
    }

    /**
     * Name of the person
     */
    public PersonName getName() {
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

        private PersonId id;
        private PersonName name;

        public Builder withId(PersonId id) {
            this.id = id;
            return this;
        }

        public Builder withName(PersonName name) {
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

        const val expectedForExtension =
"""package com.example

import com.example.util.Money

/**
 * Test custom class.
 */
data class TestCustom(
    val aaa: Money,
    val bbb: Money? = null
)
"""

        const val expectedJavaForExtension =
"""package com.example;

import com.example.util.Money;

/**
 * Test custom class.
 */
public class TestCustom {

    private final Money aaa;
    private final Money bbb;

    public TestCustom(
            Money aaa,
            Money bbb
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        this.aaa = aaa;
        this.bbb = bbb;
    }

    public Money getAaa() {
        return aaa;
    }

    public Money getBbb() {
        return bbb;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestCustom))
            return false;
        TestCustom cg_typedOther = (TestCustom)cg_other;
        if (!aaa.equals(cg_typedOther.aaa))
            return false;
        return bbb == null ? cg_typedOther.bbb == null : bbb.equals(cg_typedOther.bbb);
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        return hash ^ (bbb != null ? bbb.hashCode() : 0);
    }

    public static class Builder {

        private Money aaa;
        private Money bbb;

        public Builder withAaa(Money aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(Money bbb) {
            this.bbb = bbb;
            return this;
        }

        public TestCustom build() {
            return new TestCustom(
                    aaa,
                    bbb
            );
        }

    }

}
"""

        const val expectedForFormat =
"""package com.example

/**
 * Test custom class.
 */
data class TestCustom(
    val aaa: String,
    val bbb: String? = null
) {

    init {
        require(cg_regex0.containsMatchIn(aaa)) { "aaa does not match pattern ${'$'}cg_regex0 - ${'$'}aaa" }
        if (bbb != null)
            require(cg_regex0.containsMatchIn(bbb)) { "bbb does not match pattern ${'$'}cg_regex0 - ${'$'}bbb" }
    }

    companion object {
        private val cg_regex0 = Regex("^[0-9]{1,16}\\.[0-9]{2}\${'$'}")
    }

}
"""

        const val expectedJavaForFormat =
"""package com.example;

import java.util.regex.Pattern;

/**
 * Test custom class.
 */
public class TestCustom {

    private static final Pattern cg_regex0 = Pattern.compile("^[0-9]{1,16}\\.[0-9]{2}${'$'}");

    private final String aaa;
    private final String bbb;

    public TestCustom(
            String aaa,
            String bbb
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (!cg_regex0.matcher(aaa).find())
            throw new IllegalArgumentException("aaa does not match pattern " + cg_regex0 + " - " + aaa);
        this.aaa = aaa;
        if (bbb != null && !cg_regex0.matcher(bbb).find())
            throw new IllegalArgumentException("bbb does not match pattern " + cg_regex0 + " - " + bbb);
        this.bbb = bbb;
    }

    public String getAaa() {
        return aaa;
    }

    public String getBbb() {
        return bbb;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestCustom))
            return false;
        TestCustom cg_typedOther = (TestCustom)cg_other;
        if (!aaa.equals(cg_typedOther.aaa))
            return false;
        return bbb == null ? cg_typedOther.bbb == null : bbb.equals(cg_typedOther.bbb);
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        return hash ^ (bbb != null ? bbb.hashCode() : 0);
    }

    public static class Builder {

        private String aaa;
        private String bbb;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(String bbb) {
            this.bbb = bbb;
            return this;
        }

        public TestCustom build() {
            return new TestCustom(
                    aaa,
                    bbb
            );
        }

    }

}
"""

    }

}
