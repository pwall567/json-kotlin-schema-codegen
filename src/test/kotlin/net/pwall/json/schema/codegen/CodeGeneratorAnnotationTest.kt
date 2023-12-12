/*
 * @(#) CodeGeneratorAnnotationTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2022, 2023 Peter Wall
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

class CodeGeneratorAnnotationTest {

    @Test fun `should generate correct code for annotations`() {
        val input = File("src/test/resources/test1")
        val packageName = "net.pwall.json.schema.codegen.test.kotlin"
        val dirs = packageName.split('.')
        val outputDetails = OutputDetails(TargetFileName("Person", "kt", dirs + "person"))
        val expected = File("src/test/kotlin/${dirs.joinToString("/")}/person/Person.kt")
        CodeGenerator().apply {
            configure(configFile)
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(expected.readText()) { outputDetails.output() }
    }

    @Test fun `should generate correct code for annotations in Java`() {
        val input = File("src/test/resources/test1")
        val packageName = "net.pwall.json.schema.codegen.test.java"
        val dirs = packageName.split('.')
        val outputDetails = OutputDetails(TargetFileName("Person", "java", dirs + "person"))
        val expected = File("src/test/kotlin/${dirs.joinToString("/")}/person/Person.java")
        CodeGenerator(TargetLanguage.JAVA).apply {
            configure(configFile)
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(expected.readText()) { outputDetails.output() }
    }

    @Test fun `should generate correct code for annotations including nested classes`() {
        val input = File("src/test/resources/example.schema.json")
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        CodeGenerator().apply {
            configure(configFile)
            basePackageName = "com.example"
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(createHeader("Test.kt") + expected) { outputDetails.output() }
    }

    @Test fun `should generate correct code for annotations in Java including nested classes`() {
        val input = File("src/test/resources/example.schema.json")
        val outputDetails = OutputDetails(TargetFileName("Test", "java", dirs))
        CodeGenerator(TargetLanguage.JAVA).apply {
            configure(configFile)
            basePackageName = "com.example"
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(createHeader("Test.java") + expectedJava) { outputDetails.output() }
    }

    companion object {

        val configFile = File("src/test/resources/config/annotation-config.yaml")

        const val expected =
"""package com.example

import java.math.BigDecimal

import net.pwall.json.schema.codegen.test.annotation.DummyClassAnnotation
import net.pwall.json.schema.codegen.test.annotation.DummyFieldAnnotation

@DummyClassAnnotation
data class Test(
    /** Product identifier */
    @DummyFieldAnnotation("id")
    val id: BigDecimal,
    /** Name of the product */
    @DummyFieldAnnotation("name")
    val name: String,
    @DummyFieldAnnotation("price")
    val price: BigDecimal,
    @DummyFieldAnnotation("tags")
    val tags: List<String>? = null,
    @DummyFieldAnnotation("stock")
    val stock: Stock? = null
) {

    init {
        require(price >= BigDecimal.ZERO) { "price < minimum 0 - ${'$'}price" }
    }

    @DummyClassAnnotation
    data class Stock(
        @DummyFieldAnnotation("warehouse")
        val warehouse: BigDecimal? = null,
        @DummyFieldAnnotation("retail")
        val retail: BigDecimal? = null
    )

}
"""

        const val expectedJava =
"""package com.example;

import java.util.List;
import java.math.BigDecimal;

import net.pwall.json.schema.codegen.test.annotation.DummyClassAnnotation;
import net.pwall.json.schema.codegen.test.annotation.DummyFieldAnnotation;

@DummyClassAnnotation
public class Test {

    private final BigDecimal id;
    private final String name;
    private final BigDecimal price;
    private final List<String> tags;
    private final Stock stock;

    public Test(
            BigDecimal id,
            String name,
            BigDecimal price,
            List<String> tags,
            Stock stock
    ) {
        if (id == null)
            throw new IllegalArgumentException("Must not be null - id");
        this.id = id;
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        this.name = name;
        if (price == null)
            throw new IllegalArgumentException("Must not be null - price");
        if (price.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("price < minimum 0 - " + price);
        this.price = price;
        this.tags = tags;
        this.stock = stock;
    }

    /**
     * Product identifier
     */
    @DummyFieldAnnotation("id")
    public BigDecimal getId() {
        return id;
    }

    /**
     * Name of the product
     */
    @DummyFieldAnnotation("name")
    public String getName() {
        return name;
    }

    @DummyFieldAnnotation("price")
    public BigDecimal getPrice() {
        return price;
    }

    @DummyFieldAnnotation("tags")
    public List<String> getTags() {
        return tags;
    }

    @DummyFieldAnnotation("stock")
    public Stock getStock() {
        return stock;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof Test))
            return false;
        Test cg_typedOther = (Test)cg_other;
        if (!id.equals(cg_typedOther.id))
            return false;
        if (!name.equals(cg_typedOther.name))
            return false;
        if (!price.equals(cg_typedOther.price))
            return false;
        if (tags == null ? cg_typedOther.tags != null : !tags.equals(cg_typedOther.tags))
            return false;
        return stock == null ? cg_typedOther.stock == null : stock.equals(cg_typedOther.stock);
    }

    @Override
    public int hashCode() {
        int hash = id.hashCode();
        hash ^= name.hashCode();
        hash ^= price.hashCode();
        hash ^= (tags != null ? tags.hashCode() : 0);
        return hash ^ (stock != null ? stock.hashCode() : 0);
    }

    public static class Builder {

        private BigDecimal id;
        private String name;
        private BigDecimal price;
        private List<String> tags;
        private Stock stock;

        public Builder withId(BigDecimal id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder withStock(Stock stock) {
            this.stock = stock;
            return this;
        }

        public Test build() {
            return new Test(
                    id,
                    name,
                    price,
                    tags,
                    stock
            );
        }

    }

    public static class Stock {

        private final BigDecimal warehouse;
        private final BigDecimal retail;

        public Stock(
                BigDecimal warehouse,
                BigDecimal retail
        ) {
            this.warehouse = warehouse;
            this.retail = retail;
        }

            @DummyFieldAnnotation("warehouse")
    public BigDecimal getWarehouse() {
            return warehouse;
        }

            @DummyFieldAnnotation("retail")
    public BigDecimal getRetail() {
            return retail;
        }

        @Override
        public boolean equals(Object cg_other) {
            if (this == cg_other)
                return true;
            if (!(cg_other instanceof Stock))
                return false;
            Stock cg_typedOther = (Stock)cg_other;
            if (warehouse == null ? cg_typedOther.warehouse != null : !warehouse.equals(cg_typedOther.warehouse))
                return false;
            return retail == null ? cg_typedOther.retail == null : retail.equals(cg_typedOther.retail);
        }

        @Override
        public int hashCode() {
            int hash = (warehouse != null ? warehouse.hashCode() : 0);
            return hash ^ (retail != null ? retail.hashCode() : 0);
        }

    }

}
"""

    }

}
