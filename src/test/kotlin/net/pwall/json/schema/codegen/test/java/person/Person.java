/*
 * Person.java
 *
 * This code was generated by json-kotlin-schema-codegen - JSON Schema Code Generator
 * See https://github.com/pwall567/json-kotlin-schema-codegen
 *
 * It is not advisable to modify generated code as any modifications will be lost
 * when the generation process is re-run.
 */
package net.pwall.json.schema.codegen.test.java.person;

import java.util.UUID;

import net.pwall.json.schema.codegen.test.annotation.DummyClassAnnotation;
import net.pwall.json.schema.codegen.test.annotation.DummyFieldAnnotation;

/**
 * A class to represent a person
 */
@DummyClassAnnotation
public class Person {

    private final UUID id;
    private final String name;

    public Person(
            UUID id,
            String name
    ) {
        if (id == null)
            throw new IllegalArgumentException("Must not be null - id");
        this.id = id;
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        this.name = name;
    }

    /**
     * Id of the person
     */
    @DummyFieldAnnotation("id")
    public UUID getId() {
        return id;
    }

    /**
     * Name of the person
     */
    @DummyFieldAnnotation("name")
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof Person))
            return false;
        Person cg_typedOther = (Person)cg_other;
        if (!id.equals(cg_typedOther.id))
            return false;
        return name.equals(cg_typedOther.name);
    }

    @Override
    public int hashCode() {
        int hash = id.hashCode();
        return hash ^ name.hashCode();
    }

    public static class Builder {

        private UUID id;
        private String name;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Person build() {
            return new Person(
                    id,
                    name
            );
        }

    }

}
