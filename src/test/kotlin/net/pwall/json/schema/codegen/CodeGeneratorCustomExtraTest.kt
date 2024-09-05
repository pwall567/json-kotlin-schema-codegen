/*
 * @(#) CodeGeneratorCustomExtraTest.kt
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

import io.kjson.JSON
import io.kjson.JSONValue
import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorCustomExtraTest {

    @Test fun `should generate correct classes for custom generation`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter1 = StringWriter()
        val outputDetails1 = OutputDetails(TargetFileName("Test", "kt", dirs), stringWriter1)
        val stringWriter2 = StringWriter()
        val outputDetails2 = OutputDetails(TargetFileName("Stock", "kt", dirs), stringWriter2)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails1, outputDetails2)
        codeGenerator.addCustomClassByURI(URI("#/properties/stock"), "com.example.Stock")
        val schema: JSONValue = JSON.parseNonNull(input.readText())
        val parser = JSONSchema.parser
        codeGenerator.generateClasses(listOf(
                parser.parseSchema(schema, JSONPointer.root, input.toURI()) to "Test",
                parser.parseSchema(schema, JSONPointer("/properties/stock"), input.toURI()) to "Stock"
        ))
        expect(createHeader("Test.kt") + expectedExample1) { stringWriter1.toString() }
        expect(createHeader("Stock.kt") + expectedExample2) { stringWriter2.toString() }
    }

    @Test fun `should generate correct classes for custom generation in Java`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter1 = StringWriter()
        val outputDetails1 = OutputDetails(TargetFileName("Test", "java", dirs), stringWriter1)
        val stringWriter2 = StringWriter()
        val outputDetails2 = OutputDetails(TargetFileName("StockEntry", "java", dirs), stringWriter2)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetails1, outputDetails2)
        codeGenerator.addCustomClassByURI(URI("#/properties/stock"), "com.example.StockEntry")
        val schema: JSONValue = JSON.parseNonNull(input.readText())
        val parser = JSONSchema.parser
        codeGenerator.generateClasses(listOf(
                parser.parseSchema(schema, JSONPointer.root, input.toURI()) to "Test",
                parser.parseSchema(schema, JSONPointer("/properties/stock"), input.toURI()) to "StockEntry"
        ))
        expect(createHeader("Test.java") + expectedExample1Java) { stringWriter1.toString() }
        expect(createHeader("StockEntry.java") + expectedExample2Java) { stringWriter2.toString() }
    }

    companion object {

        const val expectedExample1 =
"""package com.example

import java.math.BigDecimal

data class Test(
    /** Product identifier */
    val id: BigDecimal,
    /** Name of the product */
    val name: String,
    val price: BigDecimal,
    val tags: List<String>? = null,
    val stock: Stock? = null
) {

    init {
        require(price >= BigDecimal.ZERO) { "price < minimum 0 - ${'$'}price" }
    }

}
"""

        const val expectedExample2 =
"""package com.example

import java.math.BigDecimal

data class Stock(
    val warehouse: BigDecimal? = null,
    val retail: BigDecimal? = null
)
"""

        const val expectedExample1Java =
"""package com.example;

import java.util.List;
import java.math.BigDecimal;

public class Test {

    private final BigDecimal id;
    private final String name;
    private final BigDecimal price;
    private final List<String> tags;
    private final StockEntry stock;

    public Test(
            BigDecimal id,
            String name,
            BigDecimal price,
            List<String> tags,
            StockEntry stock
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
    public BigDecimal getId() {
        return id;
    }

    /**
     * Name of the product
     */
    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public List<String> getTags() {
        return tags;
    }

    public StockEntry getStock() {
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
        private StockEntry stock;

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

        public Builder withStock(StockEntry stock) {
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

}
"""

        const val expectedExample2Java =
"""package com.example;

import java.math.BigDecimal;

public class StockEntry {

    private final BigDecimal warehouse;
    private final BigDecimal retail;

    public StockEntry(
            BigDecimal warehouse,
            BigDecimal retail
    ) {
        this.warehouse = warehouse;
        this.retail = retail;
    }

    public BigDecimal getWarehouse() {
        return warehouse;
    }

    public BigDecimal getRetail() {
        return retail;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof StockEntry))
            return false;
        StockEntry cg_typedOther = (StockEntry)cg_other;
        if (warehouse == null ? cg_typedOther.warehouse != null : !warehouse.equals(cg_typedOther.warehouse))
            return false;
        return retail == null ? cg_typedOther.retail == null : retail.equals(cg_typedOther.retail);
    }

    @Override
    public int hashCode() {
        int hash = (warehouse != null ? warehouse.hashCode() : 0);
        return hash ^ (retail != null ? retail.hashCode() : 0);
    }

    public static class Builder {

        private BigDecimal warehouse;
        private BigDecimal retail;

        public Builder withWarehouse(BigDecimal warehouse) {
            this.warehouse = warehouse;
            return this;
        }

        public Builder withRetail(BigDecimal retail) {
            this.retail = retail;
            return this;
        }

        public StockEntry build() {
            return new StockEntry(
                    warehouse,
                    retail
            );
        }

    }

}
"""

    }

}
