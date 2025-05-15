/*
 * @(#) CodeGeneratorIndexTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021, 2023, 2025 Peter Wall
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

import io.kjson.JSON
import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorIndexTest {

    @Test fun `should generate index in TypeScript`() {
        val input = File("src/test/resources/test-index.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.TYPESCRIPT)
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "ts"))
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "ts"))
        val outputDetailsZ = OutputDetails(TargetFileName("TypeZ", "ts"))
        val outputDetailsIndex = OutputDetails(TargetFileName("index", "d.ts"))
        codeGenerator.indexFileName = TargetFileName("index", "d.ts")
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsZ, outputDetailsIndex)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        outputDetailsA.output() shouldBe createHeader("TypeA.ts") + expectedTypeATypeScript
        outputDetailsB.output() shouldBe createHeader("TypeB.ts") + expectedTypeBTypeScript
        outputDetailsZ.output() shouldBe createHeader("TypeZ.ts") + expectedTypeZTypeScript
        outputDetailsIndex.output() shouldBe createHeader("index.d.ts") + expectedIndexTypeScript
    }

    companion object {

        const val expectedTypeATypeScript =
"""
import { TypeB } from "./TypeB";
import { TypeZ } from "./TypeZ";

export interface TypeA {
    aaa: string;
    bbb: number;
    ccc: TypeB;
    ddd: TypeZ;
}
"""

        const val expectedTypeBTypeScript =
"""
export interface TypeB {
    xxx: string;
    yyy: boolean;
}
"""

        const val expectedTypeZTypeScript =
"""
export type TypeZ =
    "ABC" |
    "DEF" |
    "GHI";
"""

        const val expectedIndexTypeScript =
"""
import { TypeA } from "./TypeA";
import { TypeB } from "./TypeB";
import { TypeZ } from "./TypeZ";

export { TypeA };
export { TypeB };
export { TypeZ };
"""

    }

}
