/*
 * @(#) CodeGeneratorExampleTest.kt
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

class CodeGeneratorExampleTest {

    @Test fun `should output example data class`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("Test", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("Test.kt") + expectedExample
    }

    @Test fun `should output example data class in Java`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("Test", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("Test.java") + expectedExampleJava
    }

    @Test fun `should output example data class in TypeScript`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.TYPESCRIPT)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("Test", "ts", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("Test.ts") + expectedExampleTypeScript
    }

    companion object {

        const val expectedExample =
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

    data class Stock(
        val warehouse: BigDecimal? = null,
        val retail: BigDecimal? = null
    )

}
"""

        const val expectedExampleJava =
"""package com.example;

import java.util.List;
import java.math.BigDecimal;

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

        const val expectedExampleTypeScript =
"""
export interface Test {
    /** Product identifier */
    id: number;
    /** Name of the product */
    name: string;
    price: number;
    tags?: string[];
    stock?: Stock;
}

export interface Stock {
    warehouse?: number;
    retail?: number;
}
"""

    }

}
