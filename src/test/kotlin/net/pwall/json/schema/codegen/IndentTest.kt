/*
 * @(#) IndentTest.kt
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

import java.io.StringReader

import io.kjson.mustache.Template

class IndentTest {

    @Test fun `should output no indent initially`() {
        val template = Template.parse(StringReader("[{{&indent}}]"))
        expect("[]") { template.render(Indent()) }
    }

    @Test fun `should output incremented indent`() {
        val template = Template.parse(StringReader("[{{#indent.increment}}{{&indent}}{{/indent.increment}}]"))
        expect("[    ]") { template.render(Indent()) }
    }

    @Test fun `should output doubly incremented indent`() {
        val template = Template.parse(StringReader(
                "[{{#indent.increment}}{{#indent.increment}}{{&indent}}{{/indent.increment}}{{/indent.increment}}]"))
        expect("[        ]") { template.render(Indent()) }
    }

}
