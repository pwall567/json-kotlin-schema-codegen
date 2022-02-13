/*
 * @(#) JSONSchemaOrgTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021, 2022 Peter Wall
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
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

/**
 * This class runs the code generator on the example JSON Schema that is detailed in the
 * [Getting Started Step-By-Step](https://json-schema.org/learn/getting-started-step-by-step) of the
 * [JSON Schema](https://json-schema.org/) website.
 *
 * The expected results are at:
 * - [Product.kt](https://github.com/pwall567/json-kotlin-schema-codegen/blob/main/src/test/kotlin/net/pwall/json/schema/generated/Product.kt)
 * - [GeographicalLocation.kt](https://github.com/pwall567/json-kotlin-schema-codegen/blob/main/src/test/kotlin/net/pwall/json/schema/generated/GeographicalLocation.kt)
 *
 * @author  Peter Wall
 */
class JSONSchemaOrgTest {

    @Test fun `should generate classes for example in JSON Schema website`() {
        val inputA = File("src/test/resources/json-schema-org/product.schema.json")
        val inputB = File("src/test/resources/json-schema-org/geographical-location.schema.json")
        val outputA = File("src/test/kotlin/net/pwall/json/schema/generated/Product.kt")
        val outputB = File("src/test/kotlin/net/pwall/json/schema/generated/GeographicalLocation.kt")
        val packageName = "net.pwall.json.schema.generated"
        val dirs = packageName.split('.')
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("Product", "kt", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("GeographicalLocation", "kt", dirs), stringWriterB)
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = packageName
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        expect(outputA.reader().readText()) { stringWriterA.toString() }
        expect(outputB.reader().readText()) { stringWriterB.toString() }
    }

}
