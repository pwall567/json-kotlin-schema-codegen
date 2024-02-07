/*
 * TestApFalsePattern.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin

/**
 * Test use of additionalProperties false with patternProperties.
 */
class TestApFalsePattern(
    private val cg_map: Map<String, Any?>
) : Map<String, Any?> by cg_map {

    init {
        cg_map.entries.forEach { (key, value) ->
            if (cg_regex0.matches(key)) {
                require(value is Int) { "$key is not the correct type, expecting Int" }
                require(value in 0..99) { "$key not in range 0..99 - $value" }
            }
        }
        cg_map.keys.forEach { key ->
            if (!cg_regex0.matches(key))
                throw IllegalArgumentException("Unexpected field $key")
        }
    }

    override fun toString() = buildString {
        append("TestApFalsePattern(")
        if (cg_map.isNotEmpty()) {
            var count = 0
            cg_map.entries.forEach { (key, value) ->
                append(key)
                append('=')
                append(value)
                if (++count < cg_map.size)
                    append(", ")
            }
        }
        append(')')
    }

    override fun equals(other: Any?): Boolean = this === other || other is TestApFalsePattern && cg_map == other.cg_map

    override fun hashCode(): Int = cg_map.hashCode()

    companion object {
        private val cg_regex0 = Regex("^[A-Z]{3}\$")
    }

}
