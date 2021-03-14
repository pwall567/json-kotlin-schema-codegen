/*
 * @(#) TargetFileName.kt
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

import java.io.File

/**
 * A file name.  This class is structured so as to be easily accessible from Mustache templates, to output either a
 * class name (optionally fully qualified) or a file name (optionally including path).
 *
 * @author  Peter Wall
 */
data class TargetFileName(val name: String, val ext: String? = null, val dirs: List<String> = emptyList()) :
        TargetClass {

    override val className: String
        get() = name

    override val packageName: String?
        get() = if (dirs.isEmpty()) null else dirs.joinToString(separator = ".")

    override val qualifiedClassName: String
        get() = StringBuilder().apply {
            for (dir in dirs)
                append(dir).append('.')
            append(className)
        }.toString()

    fun resolve(parent: File? = null): File {
        var baseDir = parent
        for (dir in dirs)
            baseDir = File(baseDir, dir)
        val fullName = if (ext != null) "$name.$ext" else name
        return File(baseDir, fullName)
    }

    override fun toString(): String = StringBuilder().apply {
        dirs.forEach { append(it).append('/') }
        append(name)
        ext?.let { append('.').append(it) }
    }.toString()

}
