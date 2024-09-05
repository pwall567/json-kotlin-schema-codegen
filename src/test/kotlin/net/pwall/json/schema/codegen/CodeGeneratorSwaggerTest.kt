/*
 * @(#) CodeGeneratorSwaggerTest.kt
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

import io.kjson.pointer.JSONPointer
import io.kjson.yaml.YAML

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorSwaggerTest {

    @Test fun `should generate classes from Swagger file`() {
        val input = File("src/test/resources/test-swagger.yaml")
        val swaggerDoc = YAML.parse(input)
        val codeGenerator = CodeGenerator()
        val stringWriter1 = StringWriter()
        val outputDetails1 = OutputDetails(TargetFileName("QueryResponse", "kt", dirs), stringWriter1)
        val stringWriter2 = StringWriter()
        val outputDetails2 = OutputDetails(TargetFileName("Person", "kt", dirs), stringWriter2)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails1, outputDetails2)
        codeGenerator.generateAll(swaggerDoc.rootNode!!, JSONPointer("/definitions"))
        expect(createHeader("QueryResponse.kt") + expectedExample1) { stringWriter1.toString() }
        expect(createHeader("Person.kt") + expectedExample2) { stringWriter2.toString() }
    }

    @Test fun `should generate classes from Swagger file applying filter`() {
        val input = File("src/test/resources/test-swagger.yaml")
        val swaggerDoc = YAML.parse(input)
        val codeGenerator = CodeGenerator()
        val stringWriter1 = StringWriter()
        val outputDetails1 = OutputDetails(TargetFileName("QueryResponse", "kt", dirs), stringWriter1)
        val stringWriter2 = StringWriter()
        val outputDetails2 = OutputDetails(TargetFileName("Person", "kt", dirs), stringWriter2)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails1, outputDetails2)
        codeGenerator.generateAll(swaggerDoc.rootNode!!, JSONPointer("/definitions")) { it == "Person" }
        expect("") { stringWriter1.toString() }
        expect(createHeader("Person.kt") + expectedExample2) { stringWriter2.toString() }
    }

    @Test fun `should generate classes from Swagger file in Java`() {
        val input = File("src/test/resources/test-swagger.yaml")
        val swaggerDoc = YAML.parse(input)
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter1 = StringWriter()
        val outputDetails1 = OutputDetails(TargetFileName("QueryResponse", "java", dirs), stringWriter1)
        val stringWriter2 = StringWriter()
        val outputDetails2 = OutputDetails(TargetFileName("Person", "java", dirs), stringWriter2)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails1, outputDetails2)
        codeGenerator.generateAll(swaggerDoc.rootNode!!, JSONPointer("/definitions"))
        expect(createHeader("QueryResponse.java") + expectedExample1Java) { stringWriter1.toString() }
        expect(createHeader("Person.java") + expectedExample2Java) { stringWriter2.toString() }
    }

    companion object {

        const val expectedExample1 =
"""package com.example

data class QueryResponse(
    val data: Person,
    val message: String? = null
)
"""

        const val expectedExample2 =
"""package com.example

/**
 * Test Swagger definition.
 */
data class Person(
    val id: Int,
    val name: String
) {

    init {
        require(id in 1..9999) { "id not in range 1..9999 - ${'$'}id" }
        require(name.isNotEmpty()) { "name length < minimum 1 - ${'$'}{name.length}" }
    }

}
"""

        const val expectedExample1Java =
"""package com.example;

public class QueryResponse {

    private final Person data;
    private final String message;

    public QueryResponse(
            Person data,
            String message
    ) {
        if (data == null)
            throw new IllegalArgumentException("Must not be null - data");
        this.data = data;
        this.message = message;
    }

    public Person getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof QueryResponse))
            return false;
        QueryResponse cg_typedOther = (QueryResponse)cg_other;
        if (!data.equals(cg_typedOther.data))
            return false;
        return message == null ? cg_typedOther.message == null : message.equals(cg_typedOther.message);
    }

    @Override
    public int hashCode() {
        int hash = data.hashCode();
        return hash ^ (message != null ? message.hashCode() : 0);
    }

    public static class Builder {

        private Person data;
        private String message;

        public Builder withData(Person data) {
            this.data = data;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public QueryResponse build() {
            return new QueryResponse(
                    data,
                    message
            );
        }

    }

}
"""

        const val expectedExample2Java =
"""package com.example;

/**
 * Test Swagger definition.
 */
public class Person {

    private final int id;
    private final String name;

    public Person(
            int id,
            String name
    ) {
        if (id < 1 || id > 9999)
            throw new IllegalArgumentException("id not in range 1..9999 - " + id);
        this.id = id;
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        if (name.length() < 1)
            throw new IllegalArgumentException("name length < minimum 1 - " + name.length());
        this.name = name;
    }

    public int getId() {
        return id;
    }

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
        if (id != cg_typedOther.id)
            return false;
        return name.equals(cg_typedOther.name);
    }

    @Override
    public int hashCode() {
        int hash = id;
        return hash ^ name.hashCode();
    }

    public static class Builder {

        private int id;
        private String name;

        public Builder withId(int id) {
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
