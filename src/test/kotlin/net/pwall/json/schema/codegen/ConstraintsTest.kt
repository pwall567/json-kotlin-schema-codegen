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
import kotlin.test.expect

import java.io.File

import io.kjson.JSONArray
import io.kjson.JSONInt
import io.kjson.JSONLong

import net.pwall.json.schema.JSONSchema

class ConstraintsTest {

    @Test fun `should recognise type string`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.STRING)
        expect(true) { constraints.isIdentifiableType }
        expect(true) { constraints.isString }
        expect(false) { constraints.isInt }
    }

    @Test fun `should recognise type integer when maximum and minimum added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
        constraints.minimum = 0
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
        constraints.maximum = 4000
        expect(true) { constraints.isIdentifiableType }
        expect(false) { constraints.isString }
        expect(true) { constraints.isInt }
        expect(false) { constraints.isLong }
    }

    @Test fun `should recognise type integer when const int added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
        constraints.constValue = JSONInt(5)
        expect(true) { constraints.isInt }
        expect(false) { constraints.isLong }
    }

    @Test fun `should recognise type integer when small const long added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
        constraints.constValue = JSONLong(999)
        expect(true) { constraints.isInt }
        expect(false) { constraints.isLong }
    }

    @Test fun `should not recognise type integer when const long added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
        constraints.constValue = JSONLong(123456789123456789)
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
    }

    @Test fun `should recognise type integer when enum of int added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
        constraints.enumValues = JSONArray(JSONInt(1), JSONInt(2), JSONInt(3), JSONInt(4))
        expect(true) { constraints.isInt }
        expect(false) { constraints.isLong }
    }

    @Test fun `should recognise type integer when enum of small long added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
        constraints.enumValues = JSONArray(JSONLong(1111), JSONLong(2222), JSONLong(3333), JSONLong(4444))
        expect(true) { constraints.isInt }
        expect(false) { constraints.isLong }
    }

    @Test fun `should not recognise type integer when enum of long added`() {
        val constraints = Constraints(dummySchema)
        constraints.types.add(JSONSchema.Type.INTEGER)
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
        constraints.enumValues = JSONArray(JSONLong(0L), JSONLong(123456789123456789))
        expect(false) { constraints.isInt }
        expect(true) { constraints.isLong }
    }

    companion object {
        val dummySchema = JSONSchema.parse(File("src/test/resources/empty.schema.json"))
    }

}
