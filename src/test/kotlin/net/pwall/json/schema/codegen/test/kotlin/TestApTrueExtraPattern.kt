/*
 * TestApTrueExtraPattern.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin

/**
 * Test use of additionalProperties true with extra fields and patternProperties.
 */
class TestApTrueExtraPattern(
    private val cg_map: Map<String, Any?>
) : Map<String, Any?> by cg_map {

    init {
        require(cg_map.containsKey("extra")) { "required property missing - extra" }
        require(cg_map["extra"] is String) { "extra is not the correct type, expecting String" }
        (cg_map["extra"] as String).let { extra ->
            require(extra.isNotEmpty()) { "extra length < minimum 1 - ${extra.length}" }
        }
        cg_map.entries.forEach { (key, value) ->
            if (cg_regex0.matches(key))
                require(value is Long || value is Int) { "$key is not the correct type, expecting Long" }
        }
    }

    val extra: String by cg_map

    override fun toString() = buildString {
        append("TestApTrueExtraPattern(")
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

    override fun equals(other: Any?): Boolean = this === other || other is TestApTrueExtraPattern && cg_map == other.cg_map

    override fun hashCode(): Int = cg_map.hashCode()

    companion object {
        private val cg_regex0 = Regex("^[A-Z]{3}\$")
    }

}