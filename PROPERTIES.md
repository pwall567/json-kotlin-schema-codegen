# `additionalProperties` and `paternProperties`

## Background

The simple examples used to illustrate code generation from JSON Schema show Kotlin classes that are very easy to read.
The JSON Schema:
```json
{
  "$schema": "http://json-schema.org/draft/2020-12/schema",
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
This means that there are cases where the property names are not known in advance (and may not be valid Kotlin names,
even when enclosed in backticks), and therefore the style of data class shown above will not work.
What&rsquo;s more, the default for `additionalProperties` is `true`, meaning that any object covered by a schema which
does not include `additionalProperties: false` could potentially have additional properties, and the classes generated
by the code generator would have no way of handling them.

If the code that uses the generated classes does not need to access these additional properties, their presence need not
be an issue, since most JSON deserialization libraries may be configured to ignore unexpected properties.
But this still does not provide a means of accessing such properties, if that is what the application requires.

## `additionalProperties`

Starting with version 0.100 of the code generator (February 2024), the generation process may be configured to output
classes that will handle `additionalProperties: true`, or the more complex case of `additionalProperties` with a defined
schema specifying validations to be applied to any additional properties.

The generated code produced with this feature enabled is much more complex than before; it makes use of the Kotlin
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

## `patternProperties`

The `patternProperties` validation presents the same problem &ndash; properties names that are not known in advance and
and potentially including characters not allowed in Kotlin names.

Take this schema for an object containing a set of currency names:
```json
{
  "$schema": "http://json-schema.org/draft/2020-12/schema",
  "$id": "http://example.com/schema/currencies",
  "description": "Currency names.",
  "type": "object",
  "patternProperties": {
    "^[A-Z]{3}$": {
      "type": "string"
    }
  },
  "additionalProperties": false
}
```
The data might look something like:
```json
{
  "USD": "US Dollar",
  "AUD": "Australian Dollar",
  "EUR": "Euro"
}
```

The generated code for this class is:
```kotlin
class Currencies(
    private val cg_map: Map<String, Any?>
) : Map<String, Any?> by cg_map {

    init {
        cg_map.entries.forEach { (key, value) ->
            if (cg_regex0.matches(key))
                require(value is String) { "$key is not the correct type, expecting String" }
        }
        cg_map.keys.forEach { key ->
            if (!cg_regex0.matches(key))
                throw IllegalArgumentException("Unexpected field $key")
        }
    }

    // toString, equals and hashCode omitted for clarity

    companion object {
        private val cg_regex0 = Regex("^[A-Z]{3}\$")
    }

}
```

The `init` block checks that each property that matches the pattern is of the correct type, and because
`additionalProperties` was set to `false`, it then checks that there are no properties that do not match the pattern.

And to access the data:
```kotlin
    currencies.entries.forEach { (code, name) ->
        println("Code $code: $name")
    }
```

## `minProperties` and `maxProperties`

Because `required` applies only to named properties, we need another mechanism for confirming that additional properties
or pattern properties are present.
The `minProperties` and `maxProperties` validations allow the minimum (and sometimes the maximum) number of properties
to be specified.

## Combinations

All of these types of properties &ndash; `properties`, `additionalProperties` and `patternProperties` &ndash; may be
combined in a single object, although in practice, complex combinations are unlikely to be useful.

The code generator will attempt to produce viable code for all combinations, but some JSON deserialization functions may
struggle with the more complex generated classes.
The [`kjson`](https://github.com/pwall567/kjson) library will deserialize all but the most complex cases without the
need for additional configuration, but even it will require custom deserialization for the more complex cases.

## Configuration Setting `additionalPropertiesOption`

As stated earlier, the default for `additionalProperties` is `true`, but many existing schema files will not include
`additionalProperties: false`, even if that is what the schema authors intended.
If the form of code generation that includes `additionalProperties` and `patternProperties` were enabled as standard,
the generated code would be very much less clear and comprehensible than the ideal set out at the start of this
document.

Therefore, there is a configuration option that must be set to enable this capability.
In the [Configuration File](CONFIG.md), add:
```json
{
  "additionalPropertiesOption": "strict"
}
```
Or in YAML:
```yaml
additionalPropertiesOption: strict
```
The keyword to ignore `additionalProperties` and `patternProperties` is `ignore`, but that is the default so it will
rarely be needed.

And for those invoking the code generator programmatically:
```kotlin
    val generator = CodeGenerator().apply {
        additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT // or IGNORE
    }
```

2024-02-08
