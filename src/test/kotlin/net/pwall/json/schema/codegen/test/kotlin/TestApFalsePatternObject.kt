/*
 * TestApFalsePatternObject.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin

/**
 * Test use of additionalProperties false with patternProperties specifying object.
 */
class TestApFalsePatternObject(
    private val cg_map: Map<String, Any?>
) : Map<String, Any?> by cg_map {

    init {
        cg_map.entries.forEach { (key, value) ->
            if (cg_regex0.matches(key))
                require(value is PatternProperty) { "$key is not the correct type, expecting PatternProperty" }
        }
        cg_map.keys.forEach { key ->
            if (!cg_regex0.matches(key))
                throw IllegalArgumentException("Unexpected field $key")
        }
    }

    override fun toString(): String = "TestApFalsePatternObject(${cg_map.entries.joinToString { "${it.key}=${it.value}" }})"

    override fun equals(other: Any?): Boolean = this === other || other is TestApFalsePatternObject && cg_map == other.cg_map

    override fun hashCode(): Int = cg_map.hashCode()

    class PatternProperty {

        override fun toString(): String = "PatternProperty()"

        override fun equals(other: Any?): Boolean = this === other || other is PatternProperty

        override fun hashCode(): Int = 1

    }

    companion object {
        private val cg_regex0 = Regex("^[A-Z]{3}\$")
    }

}