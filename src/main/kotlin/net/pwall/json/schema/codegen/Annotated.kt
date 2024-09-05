/*
 * @(#) Annotated.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2022, 2023 Peter Wall
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

import io.kjson.mustache.Context
import io.kjson.mustache.Template

/**
 * Base class for output constructs that can have Java/Kotlin annotations.
 *
 * @author  Peter Wall
 */
open class Annotated {

    val annotations = mutableListOf<Annotation>()

    fun applyAnnotations(configuredAnnotations: List<Pair<ClassId, Template?>>, target: Target, contextObject: Any) {
        annotations.clear()
        if (configuredAnnotations.isNotEmpty()) {
            val extendedContext = Context(GeneratorContext).child(contextObject)
            for (annotation in configuredAnnotations) {
                val annotationString = annotation.second?.generateString(extendedContext)
                annotations.add(Annotation(annotation.first.className, annotationString))
                target.addImport(annotation.first)
            }
        }
    }

    private fun Template.generateString(context: Context): String = buildString {
        appendTo(this, context)
    }

    data class Annotation(val name: String, val content: String?)

}
