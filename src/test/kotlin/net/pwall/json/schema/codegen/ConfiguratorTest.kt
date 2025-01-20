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

import io.kstuff.test.shouldBe
import io.kstuff.test.shouldThrow

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
        generator.basePackageName shouldBe null
        JSON.parseObject("""{"title":8}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was 8, at /title") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"version":true}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was true, at /version") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"description":null}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was null, at /description") {
                generator.configure(json)
            }
        }
    }

    @Test fun `should apply packageName configuration`() {
        val generator = CodeGenerator()
        generator.basePackageName shouldBe null
        generator.configure(JSON.parseObject("""{"packageName":"com.example.testing"}"""))
        generator.basePackageName shouldBe "com.example.testing"
        generator.configure(JSON.parseObject("""{"packageName":null}"""))
        generator.basePackageName shouldBe null
    }

    @Test fun `should reject invalid packageName configuration`() {
        val generator = CodeGenerator()
        generator.basePackageName shouldBe null
        JSON.parseObject("""{"packageName":123}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString?), was 123, at /packageName") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"packageName":""}""").let { json ->
            shouldThrow<JSONTypeException>("Config item not correct type (non-empty string), was \"\", at /packageName") {
                generator.configure(json)
            }
        }
    }

    @Test fun `should apply generatorComment configuration`() {
        val generator = CodeGenerator()
        generator.generatorComment shouldBe null
        generator.configure(JSON.parseObject("""{"generatorComment":"Test comment"}"""))
        generator.generatorComment shouldBe "Test comment"
        generator.configure(JSON.parseObject("""{"generatorComment":null}"""))
        generator.generatorComment shouldBe null
    }

    @Test fun `should reject invalid generatorComment configuration`() {
        val generator = CodeGenerator()
        generator.generatorComment shouldBe null
        JSON.parseObject("""{"generatorComment":123}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString?), was 123, at /generatorComment") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"generatorComment":""}""").let { json ->
            shouldThrow<JSONTypeException>("Config item not correct type (non-empty string), was \"\", at /generatorComment") {
                generator.configure(json)
            }
        }
    }

    @Test fun `should apply targetLanguage configuration`() {
        val generator = CodeGenerator()
        generator.targetLanguage shouldBe TargetLanguage.KOTLIN
        val json = JSON.parseObject("""{"targetLanguage":"java"}""")
        generator.configure(json)
        generator.targetLanguage shouldBe TargetLanguage.JAVA
    }

    @Test fun `should reject invalid targetLanguage configuration`() {
        val generator = CodeGenerator()
        JSON.parseObject("""{"targetLanguage":null}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was null, at /targetLanguage") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"targetLanguage":123}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was 123, at /targetLanguage") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"targetLanguage":""}""").let { json ->
            shouldThrow<JSONSchemaException>("Config item /targetLanguage invalid - \"\"") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"targetLanguage":"fortran"}""").let { json ->
            shouldThrow<JSONSchemaException>("Config item /targetLanguage invalid - \"fortran\"") {
                generator.configure(json)
            }
        }
    }

    @Test fun `should apply nestedClassNameOption configuration`() {
        val generator = CodeGenerator()
        generator.nestedClassNameOption shouldBe CodeGenerator.NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA
        val json = JSON.parseObject("""{"nestedClassNameOption":"property"}""")
        generator.configure(json)
        generator.nestedClassNameOption shouldBe CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY
    }

    @Test fun `should reject invalid nestedClassNameOption configuration`() {
        val generator = CodeGenerator()
        JSON.parseObject("""{"nestedClassNameOption":null}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was null, at /nestedClassNameOption") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"nestedClassNameOption":88}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was 88, at /nestedClassNameOption") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"nestedClassNameOption":""}""").let { json ->
            shouldThrow<JSONSchemaException>("Config item /nestedClassNameOption invalid - \"\"") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"nestedClassNameOption":"unknown"}""").let { json ->
            shouldThrow<JSONSchemaException>("Config item /nestedClassNameOption invalid - \"unknown\"") {
                generator.configure(json)
            }
        }
    }

    @Test fun `should reject invalid customClasses configuration`() {
        val generator = CodeGenerator()
        JSON.parseObject("""{"customClasses":null}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONObject), was null, at /customClasses") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"customClasses":"abc"}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONObject), was \"abc\", at /customClasses") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"customClasses":{"form":{"money":true}}}""").let { json ->
            shouldThrow<JSONSchemaException>("Config item /customClasses/form unrecognised mapping type") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"customClasses":{"format":{"money":true}}}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was true, at /customClasses/format/money") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"customClasses":{"format":{"money":""}}}""").let { json ->
            shouldThrow<JSONTypeException>("Config item not correct type (non-empty string), was \"\", at /customClasses/format/money") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"customClasses":{"uri":{"urn:test:test":123}}}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString), was 123, at /customClasses/uri/urn:test:test") {
                generator.configure(json)
            }
        }
    }

    @Test fun `should reject invalid annotations configuration`() {
        val generator = CodeGenerator()
        JSON.parseObject("""{"annotations":null}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONObject), was null, at /annotations") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"annotations":"abc"}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONObject), was \"abc\", at /annotations") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"annotations":{"classes":null}}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONObject), was null, at /annotations/classes") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"annotations":{"classes":"abc"}}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONObject), was \"abc\", at /annotations/classes") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"annotations":{"classes":{"123":null}}}""").let { json ->
            shouldThrow<JSONSchemaException>("Not a valid annotation class name - \"123\"") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"annotations":{"classes":{"abc":true}}}""").let { json ->
            shouldThrow<JSONTypeException>("Child not correct type (JSONString?), was true, at /annotations/classes/abc") {
                generator.configure(json)
            }
        }
        JSON.parseObject("""{"annotations":{"other":{"abc":true}}}""").let { json ->
            shouldThrow<JSONSchemaException>("Config item /annotations/other unrecognised annotation type") {
                generator.configure(json)
            }
        }
    }

}
