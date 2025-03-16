/*
 * ContactBase.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin.poly6

open class ContactBase(
    open val contactType: String
) {

    override fun equals(other: Any?): Boolean = this === other || other is ContactBase &&
            contactType == other.contactType

    override fun hashCode(): Int =
            contactType.hashCode()

    override fun toString() = "ContactBase(contactType=$contactType)"

    open fun copy(
        contactType: String = this.contactType
    ) = ContactBase(contactType)

    operator fun component1() = contactType

}
