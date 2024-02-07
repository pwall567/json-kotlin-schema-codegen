# `additionalProperties` and `paternProperties`

## Background

The simple examples used to illustrate code generation from JSON Schema show Kotlin classes that are very easy to read.
The JSON Schema:
```json
{
  "$schema": "http://json-schema.org/draft/2019-09/schema",
  "$id": "http://example.com/schema/person",
  "type": "object",
  "properties": {
    "id": {
      "type": "integer"
    },
    "name": {
      "type": "string"
    },
    "active": {
      "type": "boolean"
    }
  },
  "required": [ "id", "name", "active" ]
}
```
will generate:
```kotlin
data class Person(
    val id: Long,
    val name: String,
    val active: Boolean
)
```

There is great virtue in being able to generate code that is easy to understand; it makes the task of the developer who
uses the generated class much easier if the code is uncluttered and is comprehensible at a glance.
This code generator was designed with this principle in mind, and in the vast majority of cases, the generated code
demonstrates those properties of clarity and comprehensibility.

But there is a problem.
The JSON Schema Specification includes the validation `additionalProperties`, which specifies how properties other than
those listed in the `properties` set (and `patternProperties`; more about that later) are to be handled.
This means that there are cases where the property names are not known in advance, and therefore the style of data class
shown above will not work.
What&rsquo;s more, the default for `additionalProperties` is `true`, meaning that any object covered by a schema which
does not include `additionalProperties: false` could potentially have additional properties, and the classes generated
by the code generator would have no way of handling them.

If the code that uses the generated classes does not need to access these additional properties, their presence need not
be an issue, since most JSON deserialization libraries may be configured to ignore unexpected properties.
But this still does not provide a means of accessing such properties, if that is what the schema requires.

## `additionalProperties`

Starting with version 0.100 of the code generator (February 2024), the generation process may be configured to output
classes that will handle `additionalProperties: true`, or the more complex case of `additionalProperties` with a defined
schema specifying validations to be applied to any additional properties.

The generated code with this feature enabled is much more complex than before; it makes use of the Kotlin
[Delegation](https://kotlinlang.org/docs/delegation.html) functionality to access properties from a `Map`.
```kotlin
class Person(
    private val cg_map: Map<String, Any?>
) : Map<String, Any?> by cg_map {

    init {
        require(cg_map.containsKey("id")) { "required property missing - id" }
        require(cg_map["id"] is Long) { "id is not the correct type, expecting Long" }
        require(cg_map.containsKey("name")) { "required property missing - name" }
        require(cg_map["name"] is String) { "name is not the correct type, expecting String" }
        require(cg_map.containsKey("active")) { "required property missing - active" }
        require(cg_map["active"] is Boolean) { "active is not the correct type, expecting Boolean" }
    }

    val id: Long by cg_map

    val name: String by cg_map

    val active: Boolean by cg_map

    override fun toString() = buildString {
        append("Person(")
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

    override fun equals(other: Any?): Boolean = this === other || other is Person && cg_map == other.cg_map

    override fun hashCode(): Int = cg_map.hashCode()

}
```
Because the `init` block checks the presence and the type of the named properties, they may safely be accessed using the
delegating property accessors.
Any additional properties may be accessed by the `Map` syntax: `person["propertyName"]`

In this case there is no schema information supplied for the additional properties; the absence of an
`additionalProperties` validation implies `additionalProperties: true`, meaning that any values are allowed.
If the `additionalProperties` validation included a schema, the `init` block would check all properties other than those
named for conformity to the schema.
And if there were no named `properties`, the `Map` would use the type derived from the schema (instead of `Any?`),
simplifying the use of the additional properties.

To be continued...
