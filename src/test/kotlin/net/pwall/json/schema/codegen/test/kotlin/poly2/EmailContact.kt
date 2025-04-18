/*
 * EmailContact.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin.poly2

import io.jstuff.json.validation.JSONValidation

import net.pwall.json.schema.codegen.test.kotlin.TestPolymorphicGroup2

data class EmailContact(
    override val base: String,
    val contactType: String,
    val emailAddress: String
) : TestPolymorphicGroup2 {

    init {
        require(base.isNotEmpty()) { "base length < minimum 1 - ${base.length}" }
        require(contactType == cg_str0) { "contactType not constant value $cg_str0 - $contactType" }
        require(JSONValidation.isEmail(emailAddress)) { "emailAddress does not match format email - $emailAddress" }
    }

    companion object {
        private const val cg_str0 = "EMAIL"
    }

}
