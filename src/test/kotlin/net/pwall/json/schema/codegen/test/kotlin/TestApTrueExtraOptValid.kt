/*
 * TestApTrueExtraOptValid.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin

/**
 * Test use of additionalProperties true with optional extra fields with validation.
 */
class TestApTrueExtraOptValid(
    private val cg_map: Map<String, Any?>
) : Map<String, Any?> by cg_map {

    init {
        if (cg_map.containsKey("extra")) {
            require(cg_map["extra"] is String?) { "extra is not the correct type, expecting String?" }
            (cg_map["extra"] as String?)?.let { extra ->
                require(extra.isNotEmpty()) { "extra length < minimum 1 - ${extra.length}" }
            }
        }
    }

    val extra: String?
        get() = cg_map["extra"] as String?

    override fun toString(): String = "TestApTrueExtraOptValid(${cg_map.entries.joinToString { "${it.key}=${it.value}" }})"

    override fun equals(other: Any?): Boolean = this === other || other is TestApTrueExtraOptValid && cg_map == other.cg_map

    override fun hashCode(): Int = cg_map.hashCode()

}