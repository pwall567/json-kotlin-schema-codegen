package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import java.io.StringWriter
import java.net.URI
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorClassNameTest {

    @Test fun `should generate class with supplied name`() {
        val input = File("src/test/resources/test-class-name.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.addClassNameMapping(URI("urn:test:example"), "Supplied")
        codeGenerator.outputResolver = outputCapture(TargetFileName("Supplied", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("Supplied.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate class with supplied name in Java`() {
        val input = File("src/test/resources/test-class-name.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.addClassNameMapping(URI("urn:test:example"), "Supplied")
        codeGenerator.outputResolver = outputCapture(TargetFileName("Supplied", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("Supplied.java") + expectedJava) { stringWriter.toString() }
    }

    @Test fun `should generate class with supplied name using config file`() {
        val input = File("src/test/resources/test-class-name.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(File("src/test/resources/config/class-name-config.json"))
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("Supplied", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("Supplied.kt") + expected) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

data class Supplied(
    val id: String
)
"""

        const val expectedJava =
"""package com.example;

public class Supplied {

    private final String id;

    public Supplied(
            String id
    ) {
        if (id == null)
            throw new IllegalArgumentException("Must not be null - id");
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof Supplied))
            return false;
        Supplied cg_typedOther = (Supplied)cg_other;
        return id.equals(cg_typedOther.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class Builder {

        private String id;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Supplied build() {
            return new Supplied(
                    id
            );
        }

    }

}
"""

    }

}