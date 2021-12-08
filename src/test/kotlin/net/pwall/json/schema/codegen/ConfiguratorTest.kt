/*
 * @(#) ConfiguratorTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021 Peter Wall
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
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.expect

import net.pwall.json.JSON
import net.pwall.json.schema.JSONSchemaException

class ConfiguratorTest {

    @Test fun `should accept configuration title, version and description`() {
        val generator = CodeGenerator()
        generator.configure(JSON.parse("""{"title":"Test config"}"""))
        generator.configure(JSON.parse("""{"version":"v0.1"}"""))
        generator.configure(JSON.parse("""{"description":"Dummy description"}"""))
    }

    @Test fun `should reject invalid configuration title, version and description`() {
        val generator = CodeGenerator()
        assertNull(generator.basePackageName)
        JSON.parse("""{"title":8}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /title incorrect type - 8") { it.message }
            }
        }
        JSON.parse("""{"version":true}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /version incorrect type - true") { it.message }
            }
        }
        JSON.parse("""{"description":null}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /description incorrect type - null") { it.message }
            }
        }
    }

    @Test fun `should apply packageName configuration`() {
        val generator = CodeGenerator()
        assertNull(generator.basePackageName)
        generator.configure(JSON.parse("""{"packageName":"com.example.testing"}"""))
        expect("com.example.testing") { generator.basePackageName }
        generator.configure(JSON.parse("""{"packageName":null}"""))
        assertNull(generator.basePackageName)
    }

    @Test fun `should reject invalid packageName configuration`() {
        val generator = CodeGenerator()
        assertNull(generator.basePackageName)
        JSON.parse("""{"packageName":123}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /packageName incorrect type - 123") { it.message }
            }
        }
        JSON.parse("""{"packageName":""}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /packageName must not be empty") { it.message }
            }
        }
    }

    @Test fun `should apply generatorComment configuration`() {
        val generator = CodeGenerator()
        assertNull(generator.generatorComment)
        generator.configure(JSON.parse("""{"generatorComment":"Test comment"}"""))
        expect("Test comment") { generator.generatorComment }
        generator.configure(JSON.parse("""{"generatorComment":null}"""))
        assertNull(generator.generatorComment)
    }

    @Test fun `should reject invalid generatorComment configuration`() {
        val generator = CodeGenerator()
        assertNull(generator.generatorComment)
        JSON.parse("""{"generatorComment":123}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /generatorComment incorrect type - 123") { it.message }
            }
        }
        JSON.parse("""{"generatorComment":""}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /generatorComment must not be empty") { it.message }
            }
        }
    }

    @Test fun `should apply targetLanguage configuration`() {
        val generator = CodeGenerator()
        expect(TargetLanguage.KOTLIN) { generator.targetLanguage }
        val json = JSON.parse("""{"targetLanguage":"java"}""")
        generator.configure(json)
        expect(TargetLanguage.JAVA) { generator.targetLanguage }
    }

    @Test fun `should reject invalid targetLanguage configuration`() {
        val generator = CodeGenerator()
        JSON.parse("""{"targetLanguage":null}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /targetLanguage incorrect type - null") { it.message }
            }
        }
        JSON.parse("""{"targetLanguage":123}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /targetLanguage incorrect type - 123") { it.message }
            }
        }
        JSON.parse("""{"targetLanguage":""}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /targetLanguage invalid - \"\"") { it.message }
            }
        }
        JSON.parse("""{"targetLanguage":"fortran"}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /targetLanguage invalid - \"fortran\"") { it.message }
            }
        }
    }

    @Test fun `should apply nestedClassNameOption configuration`() {
        val generator = CodeGenerator()
        expect(CodeGenerator.NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA) { generator.nestedClassNameOption }
        val json = JSON.parse("""{"nestedClassNameOption":"property"}""")
        generator.configure(json)
        expect(CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY) { generator.nestedClassNameOption }
    }

    @Test fun `should reject invalid nestedClassNameOption configuration`() {
        val generator = CodeGenerator()
        JSON.parse("""{"nestedClassNameOption":null}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /nestedClassNameOption incorrect type - null") { it.message }
            }
        }
        JSON.parse("""{"nestedClassNameOption":88}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /nestedClassNameOption incorrect type - 88") { it.message }
            }
        }
        JSON.parse("""{"nestedClassNameOption":""}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /nestedClassNameOption invalid - \"\"") { it.message }
            }
        }
        JSON.parse("""{"nestedClassNameOption":"unknown"}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /nestedClassNameOption invalid - \"unknown\"") { it.message }
            }
        }
    }

    @Test fun `should reject invalid customClasses configuration`() {
        val generator = CodeGenerator()
        JSON.parse("""{"customClasses":null}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /customClasses incorrect type - null") { it.message }
            }
        }
        JSON.parse("""{"customClasses":"abc"}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /customClasses incorrect type - \"abc\"") { it.message }
            }
        }
        JSON.parse("""{"customClasses":{"form":{"money":true}}}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /customClasses/form unrecognised mapping type") { it.message }
            }
        }
        JSON.parse("""{"customClasses":{"format":{"money":true}}}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /customClasses/format/money invalid - true") { it.message }
            }
        }
        JSON.parse("""{"customClasses":{"format":{"money":""}}}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /customClasses/format/money must not be empty") { it.message }
            }
        }
        JSON.parse("""{"customClasses":{"uri":{"urn:test:test":123}}}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config entry /customClasses/uri/urn:test:test invalid - 123") { it.message }
            }
        }
    }

}
