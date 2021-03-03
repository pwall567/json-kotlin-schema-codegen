package net.pwall.json.schema.codegen

interface TargetClass {

    val packageName: String?

    val className: String

    val qualifiedClassName: String
        get() = packageName?.let { "$it.$className" } ?: className

}
