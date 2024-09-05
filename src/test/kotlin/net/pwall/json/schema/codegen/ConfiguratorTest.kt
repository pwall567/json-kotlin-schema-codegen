/*
 * @(#) ConfiguratorTest.kt
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
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.expect

import io.kjson.JSON
import io.kjson.JSONTypeException

import net.pwall.json.schema.JSONSchemaException

class ConfiguratorTest {

    @Test fun `should accept configuration title, version and description`() {
        val generator = CodeGenerator()
        generator.configure(JSON.parseObject("""{"title":"Test config"}"""))
        generator.configure(JSON.parseObject("""{"version":"v0.1"}"""))
        generator.configure(JSON.parseObject("""{"description":"Dummy description"}"""))
    }

    @Test fun `should reject invalid configuration title, version and description`() {
        val generator = CodeGenerator()
        assertNull(generator.basePackageName)
        JSON.parseObject("""{"title":8}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was 8, at /title") { it.message }
            }
        }
        JSON.parseObject("""{"version":true}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was true, at /version") { it.message }
            }
        }
        JSON.parseObject("""{"description":null}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was null, at /description") { it.message }
            }
        }
    }

    @Test fun `should apply packageName configuration`() {
        val generator = CodeGenerator()
        assertNull(generator.basePackageName)
        generator.configure(JSON.parseObject("""{"packageName":"com.example.testing"}"""))
        expect("com.example.testing") { generator.basePackageName }
        generator.configure(JSON.parseObject("""{"packageName":null}"""))
        assertNull(generator.basePackageName)
    }

    @Test fun `should reject invalid packageName configuration`() {
        val generator = CodeGenerator()
        assertNull(generator.basePackageName)
        JSON.parseObject("""{"packageName":123}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString?), was 123, at /packageName") { it.message }
            }
        }
        JSON.parseObject("""{"packageName":""}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Config item not correct type (non-empty string), was \"\", at /packageName") { it.message }
            }
        }
    }

    @Test fun `should apply generatorComment configuration`() {
        val generator = CodeGenerator()
        assertNull(generator.generatorComment)
        generator.configure(JSON.parseObject("""{"generatorComment":"Test comment"}"""))
        expect("Test comment") { generator.generatorComment }
        generator.configure(JSON.parseObject("""{"generatorComment":null}"""))
        assertNull(generator.generatorComment)
    }

    @Test fun `should reject invalid generatorComment configuration`() {
        val generator = CodeGenerator()
        assertNull(generator.generatorComment)
        JSON.parseObject("""{"generatorComment":123}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString?), was 123, at /generatorComment") { it.message }
            }
        }
        JSON.parseObject("""{"generatorComment":""}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Config item not correct type (non-empty string), was \"\", at /generatorComment") { it.message }
            }
        }
    }

    @Test fun `should apply targetLanguage configuration`() {
        val generator = CodeGenerator()
        expect(TargetLanguage.KOTLIN) { generator.targetLanguage }
        val json = JSON.parseObject("""{"targetLanguage":"java"}""")
        generator.configure(json)
        expect(TargetLanguage.JAVA) { generator.targetLanguage }
    }

    @Test fun `should reject invalid targetLanguage configuration`() {
        val generator = CodeGenerator()
        JSON.parseObject("""{"targetLanguage":null}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was null, at /targetLanguage") { it.message }
            }
        }
        JSON.parseObject("""{"targetLanguage":123}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was 123, at /targetLanguage") { it.message }
            }
        }
        JSON.parseObject("""{"targetLanguage":""}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config item /targetLanguage invalid - \"\"") { it.message }
            }
        }
        JSON.parseObject("""{"targetLanguage":"fortran"}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config item /targetLanguage invalid - \"fortran\"") { it.message }
            }
        }
    }

    @Test fun `should apply nestedClassNameOption configuration`() {
        val generator = CodeGenerator()
        expect(CodeGenerator.NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA) { generator.nestedClassNameOption }
        val json = JSON.parseObject("""{"nestedClassNameOption":"property"}""")
        generator.configure(json)
        expect(CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY) { generator.nestedClassNameOption }
    }

    @Test fun `should reject invalid nestedClassNameOption configuration`() {
        val generator = CodeGenerator()
        JSON.parseObject("""{"nestedClassNameOption":null}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was null, at /nestedClassNameOption") { it.message }
            }
        }
        JSON.parseObject("""{"nestedClassNameOption":88}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was 88, at /nestedClassNameOption") { it.message }
            }
        }
        JSON.parseObject("""{"nestedClassNameOption":""}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config item /nestedClassNameOption invalid - \"\"") { it.message }
            }
        }
        JSON.parseObject("""{"nestedClassNameOption":"unknown"}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config item /nestedClassNameOption invalid - \"unknown\"") { it.message }
            }
        }
    }

    @Test fun `should reject invalid customClasses configuration`() {
        val generator = CodeGenerator()
        JSON.parseObject("""{"customClasses":null}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONObject), was null, at /customClasses") { it.message }
            }
        }
        JSON.parseObject("""{"customClasses":"abc"}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONObject), was \"abc\", at /customClasses") { it.message }
            }
        }
        JSON.parseObject("""{"customClasses":{"form":{"money":true}}}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config item /customClasses/form unrecognised mapping type") { it.message }
            }
        }
        JSON.parseObject("""{"customClasses":{"format":{"money":true}}}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was true, at /customClasses/format/money") { it.message }
            }
        }
        JSON.parseObject("""{"customClasses":{"format":{"money":""}}}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Config item not correct type (non-empty string), was \"\", at /customClasses/format/money") {
                    it.message
                }
            }
        }
        JSON.parseObject("""{"customClasses":{"uri":{"urn:test:test":123}}}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString), was 123, at /customClasses/uri/urn:test:test") {
                    it.message
                }
            }
        }
    }

    @Test fun `should reject invalid annotations configuration`() {
        val generator = CodeGenerator()
        JSON.parseObject("""{"annotations":null}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONObject), was null, at /annotations") { it.message }
            }
        }
        JSON.parseObject("""{"annotations":"abc"}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONObject), was \"abc\", at /annotations") { it.message }
            }
        }
        JSON.parseObject("""{"annotations":{"classes":null}}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONObject), was null, at /annotations/classes") { it.message }
            }
        }
        JSON.parseObject("""{"annotations":{"classes":"abc"}}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONObject), was \"abc\", at /annotations/classes") { it.message }
            }
        }
        JSON.parseObject("""{"annotations":{"classes":{"123":null}}}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Not a valid annotation class name - \"123\"") { it.message }
            }
        }
        JSON.parseObject("""{"annotations":{"classes":{"abc":true}}}""").let { json ->
            assertFailsWith<JSONTypeException> { generator.configure(json) }.let {
                expect("Child not correct type (JSONString?), was true, at /annotations/classes/abc") { it.message }
            }
        }
        JSON.parseObject("""{"annotations":{"other":{"abc":true}}}""").let { json ->
            assertFailsWith<JSONSchemaException> { generator.configure(json) }.let {
                expect("Config item /annotations/other unrecognised annotation type") { it.message }
            }
        }
    }

}
