/*
 * PhoneContact.kt
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.kotlin.poly2

import net.pwall.json.schema.codegen.test.kotlin.TestPolymorphicGroup2

data class PhoneContact(
    override val base: String,
    val contactType: String,
    val countryCode: String? = null,
    val localNumber: String
) : TestPolymorphicGroup2 {

    init {
        require(base.isNotEmpty()) { "base length < minimum 1 - ${base.length}" }
        require(contactType == cg_str0) { "contactType not constant value $cg_str0 - $contactType" }
        if (countryCode != null)
            require(cg_regex1.containsMatchIn(countryCode)) { "countryCode does not match pattern $cg_regex1 - $countryCode" }
        require(cg_regex2.containsMatchIn(localNumber)) { "localNumber does not match pattern $cg_regex2 - $localNumber" }
    }

    companion object {
        private const val cg_str0 = "PHONE"
        private val cg_regex1 = Regex("^\\+[0-9]{1,4}\$")
        private val cg_regex2 = Regex("^[0-9]{2,12}\$")
    }

}
