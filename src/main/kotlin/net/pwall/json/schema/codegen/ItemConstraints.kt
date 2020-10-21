package net.pwall.json.schema.codegen

import net.pwall.json.schema.JSONSchema

class ItemConstraints(schema: JSONSchema, val name: String) : Constraints(schema) {

    @Suppress("unused")
    val propertyName: String = "it"

    @Suppress("unused")
    override val displayName: String
        get() = "$name item"

    @Suppress("unused")
    val itemIndent = "    "

}
