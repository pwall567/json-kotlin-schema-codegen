/*
 * TestBaseEmptyDerived.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin.basederived

/**
 * Test base with empty derived class.
 */
class TestBaseEmptyDerived(
    aaa: String
) : TestBase(aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseEmptyDerived &&
            super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    override fun toString() = "TestBaseEmptyDerived(aaa=$aaa)"

    override fun copy(
        aaa: String
    ) = TestBaseEmptyDerived(aaa)

}
