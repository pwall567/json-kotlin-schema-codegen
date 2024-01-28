package net.pwall.json.schema.codegen.test.kotlin

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.expect

class TestAdditionalPropertiesFunctionality {

    val initialiser1 = mapOf(
        "field1" to "TEST STRING"
    )

    val initialiser1a = mapOf( // extra fields
        "field1" to "TEST STRING",
        "field2" to "EXTRA"
    )

    val initialiser2 = mapOf( // name misspelled
        "field10" to "TEST STRING"
    )

    val initialiser3 = mapOf( // wrong type
        "field1" to 123
    )

    @Test fun `should generate functional TestAdditionalPropertiesTrue`() {
        val testClass = TestAdditionalPropertiesTrue(initialiser1)
        expect("TEST STRING") { testClass["field1"] }
        assertNull(testClass["field2"])
        expect("TestAdditionalPropertiesTrue(field1=TEST STRING)") { testClass.toString() }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchema1`() {
        val testClass = TestAdditionalPropertiesSchema1(mapOf(
            "date1" to LocalDate.parse("2024-01-28"),
            "date2" to LocalDate.parse("2024-01-29"),
        ))
        expect(LocalDate.parse("2024-01-28")) { testClass["date1"] }
        assertNull(testClass["date99"])
        expect("TestAdditionalPropertiesSchema1(date1=2024-01-28, date2=2024-01-29)") { testClass.toString() }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaValid`() {
        val testClass = TestAdditionalPropertiesSchemaValid(mapOf(
            "field1" to "ABC",
            "field2" to "xxx",
        ))
        expect("ABC") { testClass["field1"] }
        assertNull(testClass["field99"])
        expect("TestAdditionalPropertiesSchemaValid(field1=ABC, field2=xxx)") { testClass.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaValid(mapOf(
                "field1" to "ABC",
                "field2" to "",
            ))
        }.let {
            expect("field2 length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesFalseExtra`() {
        val testClass = TestAdditionalPropertiesFalseExtra("content")
        expect("content") { testClass.extra }
        expect("TestAdditionalPropertiesFalseExtra(extra=content)") { testClass.toString() }
    }

    @Test fun `should generate functional TestAdditionalPropertiesTrueExtra`() {
        val testClass1 = TestAdditionalPropertiesTrueExtra(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass1.extra }
        assertNull(testClass1["whatever"])
        expect("TestAdditionalPropertiesTrueExtra(extra=content)") { testClass1.toString() }

        val testClass2 = TestAdditionalPropertiesTrueExtra(mapOf(
            "extra" to "content",
            "anything" to "another",
        ))
        expect("content") { testClass2.extra }
        expect("another") { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestAdditionalPropertiesTrueExtra(extra=content, anything=another)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesTrueExtra(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesTrueExtra(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesTrueExtra(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra`() {
        val testClass1 = TestAdditionalPropertiesSchemaExtra(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass1.extra }
        assertNull(testClass1["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra(extra=content)") { testClass1.toString() }

        val testClass2 = TestAdditionalPropertiesSchemaExtra(mapOf(
            "extra" to "content",
            "anything" to LocalDate.parse("2024-01-28"),
        ))
        expect("content") { testClass2.extra }
        expect(LocalDate.parse("2024-01-28")) { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra(extra=content, anything=2024-01-28)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra(mapOf(
                "extra" to "OK",
                "anything" to "another",
            ))
        }.let {
            expect("anything is not the correct type, expecting LocalDate") { it.message }
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra2`() {
        val testClass1 = TestAdditionalPropertiesSchemaExtra2(mapOf(
            "extra" to "content",
            "extra2" to 12,
        ))
        expect("content") { testClass1.extra }
        expect(12) { testClass1.extra2 }
        assertNull(testClass1["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra2(extra=content, extra2=12)") { testClass1.toString() }

        val testClass2 = TestAdditionalPropertiesSchemaExtra2(mapOf(
            "extra" to "content",
            "extra2" to 12,
            "anything" to 999,
        ))
        expect("content") { testClass2.extra }
        expect(12) { testClass2.extra2 }
        expect(999) { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra2(extra=content, extra2=12, anything=999)") {
            testClass2.toString()
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "OK",
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra2") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "OK",
                "extra2" to 999,
            ))
        }.let {
            expect("extra2 not in range 0..99 - 999") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to "wrong",
            ))
        }.let {
            expect("extra3 is not the correct type, expecting Int") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to 99999,
            ))
        }.let {
            expect("extra3 not in range 0..9999 - 99999") { it.message }
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra3`() {
        val testClass1 = TestAdditionalPropertiesSchemaExtra3(mapOf(
            "extra" to "content",
            "extra2" to 12,
        ))
        expect("content") { testClass1.extra }
        expect(12) { testClass1.extra2 }
        assertNull(testClass1["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra3(extra=content, extra2=12)") { testClass1.toString() }

        val testClass2 = TestAdditionalPropertiesSchemaExtra3(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass2.extra }
        assertNull(testClass2.extra2)
        assertNull(testClass2["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra3(extra=content)") { testClass2.toString() }

        val testClass3 = TestAdditionalPropertiesSchemaExtra3(mapOf(
            "extra" to "content",
            "extra2" to 12,
            "anything" to 999,
        ))
        expect("content") { testClass3.extra }
        expect(12) { testClass3.extra2 }
        expect(999) { testClass3["anything"] }
        assertNull(testClass3["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra3(extra=content, extra2=12, anything=999)") {
            testClass3.toString()
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "OK",
                "extra2" to 999,
            ))
        }.let {
            expect("extra2 not in range 0..99 - 999") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to "wrong",
            ))
        }.let {
            expect("extra3 is not the correct type, expecting Int") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to 99999,
            ))
        }.let {
            expect("extra3 not in range 0..9999 - 99999") { it.message }
        }
    }

}
