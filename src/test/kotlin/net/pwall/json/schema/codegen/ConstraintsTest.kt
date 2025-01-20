/*
 * @(#) ConstraintsTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020 Peter Wall
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

import io.kstuff.test.shouldBe

import io.kjson.JSONArray
import io.kjson.JSONInt
import io.kjson.JSONLong

import net.pwall.json.schema.JSONSchema

class ConstraintsTest {

    @Test fun `should recognise type string`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.STRING)
        constraints.isIdentifiableType shouldBe true
        constraints.isString shouldBe true
        constraints.isInt shouldBe false
    }

    @Test fun `should recognise type integer when maximum and minimum added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
        constraints.minimum = 0
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
        constraints.maximum = 4000
        constraints.isIdentifiableType shouldBe true
        constraints.isString shouldBe false
        constraints.isInt shouldBe true
        constraints.isLong shouldBe false
    }

    @Test fun `should recognise type integer when const int added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
        constraints.constValue = JSONInt(5)
        constraints.isInt shouldBe true
        constraints.isLong shouldBe false
    }

    @Test fun `should recognise type integer when small const long added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
        constraints.constValue = JSONLong(999)
        constraints.isInt shouldBe true
        constraints.isLong shouldBe false
    }

    @Test fun `should not recognise type integer when const long added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
        constraints.constValue = JSONLong(123456789123456789)
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
    }

    @Test fun `should recognise type integer when enum of int added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
        constraints.enumValues = JSONArray(JSONInt(1), JSONInt(2), JSONInt(3), JSONInt(4))
        constraints.isInt shouldBe true
        constraints.isLong shouldBe false
    }

    @Test fun `should recognise type integer when enum of small long added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
        constraints.enumValues = JSONArray(JSONLong(1111), JSONLong(2222), JSONLong(3333), JSONLong(4444))
        constraints.isInt shouldBe true
        constraints.isLong shouldBe false
    }

    @Test fun `should not recognise type integer when enum of long added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
        constraints.enumValues = JSONArray(JSONLong(0L), JSONLong(123456789123456789))
        constraints.isInt shouldBe false
        constraints.isLong shouldBe true
    }

    companion object {
        val dummySchema = JSONSchema.parse(File("src/test/resources/empty.schema.json"))
    }

}
