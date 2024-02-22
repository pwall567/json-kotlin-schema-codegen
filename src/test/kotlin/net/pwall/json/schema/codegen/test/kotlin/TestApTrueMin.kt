/*
 * TestApTrueMin.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin

/**
 * Test use of additionalProperties true with minimum.
 */
class TestApTrueMin(
    private val cg_map: Map<String, Any?>
) : Map<String, Any?> by cg_map {

    init {
        require(cg_map.size >= 2) { "Number of properties < minimum 2 - ${cg_map.size}" }
    }

    override fun toString(): String = "TestApTrueMin(${cg_map.entries.joinToString { "${it.key}=${it.value}" }})"

    override fun equals(other: Any?): Boolean = this === other || other is TestApTrueMin && cg_map == other.cg_map

    override fun hashCode(): Int = cg_map.hashCode()

}