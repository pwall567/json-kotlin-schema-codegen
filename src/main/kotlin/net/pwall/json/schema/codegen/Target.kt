/*
 * @(#) Target.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021, 2022 Peter Wall
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

import net.pwall.json.JSONValue
import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.JSONSchemaException
import net.pwall.json.schema.codegen.CodeGenerator.Companion.addOnce
import net.pwall.json.schema.subschema.RefSchema
import net.pwall.mustache.Context
import net.pwall.mustache.Template

/**
 * A code generation target.  The class contains several properties that exist just for the purposes of template
 * expansion.
 *
 * @author  Peter Wall
 */
class Target(
    /** Schema to generate from */
    val schema: JSONSchema,
    /** [Constraints] describing target */
    constraints: Constraints,
    /** Target file */
    @Suppress("unused") val targetFile: TargetFileName,
    /** Source identifier (e.g. schema filename) */
    val source: String,
    /** Generator comment */
    val generatorComment: String?,
    /** Comment template */
    val commentTemplate: Template? = null,
    /** Original JSON (for template expansion) */
    val json: JSONValue? = null,
) : ClassDescriptor(constraints, NamedConstraints.checkJavaName(targetFile.className)), ClassId {

    @Suppress("unused")
    val commentLines: List<String>?
        get() = if (commentTemplate != null) {
            val childContext = Context(GeneratorContext).child(this).child(json)
            StringBuilder().apply { commentTemplate.appendTo(this, childContext) }.toString().split('\n')
        } else
            generatorComment?.split('\n')

    override val packageName: String?
        get() = targetFile.packageName

    @Suppress("unused")
    val indent = Indent()

    val systemClasses = mutableListOf<SystemClass>()
    val imports = mutableListOf<String>()
    @Suppress("unused")
    val localImports = mutableListOf<ClassId>()

    @Suppress("unused")
    val statics = mutableListOf<Static>()
    @Suppress("unused")
    val nestedClasses = mutableListOf<ClassDescriptor>()

    @Suppress("unused")
    val staticsPresent: Boolean
        get() = statics.isNotEmpty()

    @Suppress("unused")
    val nestedClassesPresent: Boolean
        get() = nestedClasses.isNotEmpty()

    @Suppress("unused")
    val validationsOrNestedClassesPresent: Boolean
        get() = validationsPresent || nestedClassesPresent

    @Suppress("unused")
    val validationsOrNestedClassesOrStaticsOrBaseClassWithPropertiesPresentOrIsBaseClass: Boolean
        get() = validationsPresent || nestedClassesPresent || staticsPresent || hasBaseClassWithPropertiesOrIsBaseClass

    fun addInterface(classId: ClassId) {
        interfaces.add(classId)
        addImport(classId)
    }

    fun addNestedClass(
        constraints: Constraints,
        comparisonSchema: JSONSchema?,
        innerClassName: String,
    ): ClassDescriptor {
        val safeInnerClassName = NamedConstraints.checkJavaName(innerClassName)
        var actualInnerClassName = safeInnerClassName
        nestedClasses.find {
            it.constraints.schema === comparisonSchema || sameReference(it.constraints, constraints)
        }?.let { return it }
        nestedClasses.find { it.className == safeInnerClassName }?.let {
            for (i in 1..1000) {
                if (i == 1000)
                    throw JSONSchemaException("Too many identically named inner classes - $safeInnerClassName")
                if (!nestedClasses.any { it.className == "$safeInnerClassName$i" }) {
                    actualInnerClassName = "$safeInnerClassName$i"
                    break
                }
            }
        }
        return ClassDescriptor(constraints, actualInnerClassName).also { nestedClasses.add(it) }
    }

    private fun sameReference(constraints1: Constraints, constraints2: Constraints): Boolean {
        getRefSchema(constraints1)?.let { if (it === getRefSchema(constraints2)) return true }
        return false
    }

    private fun getRefSchema(constraints: Constraints): JSONSchema? =
        (constraints.schema as? JSONSchema.General)?.let {
            (it.children.singleOrNull() as? RefSchema)?.target
        }

    fun addStatic(type: StaticType, prefix: String, value: Any): Static =
            statics.find { it.type == type && it.value == value } ?:
                    Static(type, "$prefix${statics.size}", value).also { statics.add(it) }

    fun addImport(classId: ClassId) {
        if (!samePackage(classId))
            imports.addOnce(classId.qualifiedClassName)
        localImports.addOnce(classId)
    }

    enum class StaticType { DECIMAL, STRING, PATTERN, STRING_ARRAY, INT_ARRAY }

    data class Static(val type: StaticType, val staticName: String, val value: Any) : ValidationValue

}
