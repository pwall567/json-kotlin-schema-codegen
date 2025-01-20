/*
 * @(#) CodeGeneratorFindClassDescriptorTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2024 Peter Wall
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
import io.kstuff.test.shouldBeNonNull

import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
import net.pwall.json.schema.subschema.PropertiesSchema

class CodeGeneratorFindClassDescriptorTest {

    @Test fun `should find class descriptor`() {
        val inputA = File("src/test/resources/json-schema-org/product.schema.json")
        val inputB = File("src/test/resources/json-schema-org/geographical-location.schema.json")
        val outputA = File("src/test/kotlin/net/pwall/json/schema/generated/Product.kt")
        val outputB = File("src/test/kotlin/net/pwall/json/schema/generated/GeographicalLocation.kt")
        val packageName = "net.pwall.json.schema.generated"
        val dirs = packageName.split('.')
        val outputDetailsA = OutputDetails(TargetFileName("Product", "kt", dirs))
        val outputDetailsB = OutputDetails(TargetFileName("GeographicalLocation", "kt", dirs))
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = packageName
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        outputDetailsA.output() shouldBe outputA.reader().readText()
        outputDetailsB.output() shouldBe outputB.reader().readText()
        val schemaA = codeGenerator.schemaParser.parse(inputA)
        val productClassDescriptor = codeGenerator.findClassDescriptorInTargets(schemaA)
        productClassDescriptor.shouldBeNonNull()
        productClassDescriptor.className shouldBe "Product"
        val propertiesSchema = (schemaA as JSONSchema.General).children.filterIsInstance<PropertiesSchema>().first()
        val dimensionsSchema = propertiesSchema.properties.find { it.first == "dimensions" }?.second
        dimensionsSchema.shouldBeNonNull()
        val dimensionsClassDescriptor = codeGenerator.findClassDescriptorInTargets(dimensionsSchema)
        dimensionsClassDescriptor.shouldBeNonNull()
        dimensionsClassDescriptor.className shouldBe "Dimensions"
        dimensionsClassDescriptor.fullClassName shouldBe "Product.Dimensions"
    }

}
