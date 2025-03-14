package net.pwall.json.schema.codegen.test.kotlin.poly4

import kotlin.test.Test
import io.kstuff.test.shouldBe
import io.kstuff.test.shouldBeType
import net.pwall.json.schema.codegen.test.kotlin.TestPolymorphicGroup4

class FunctionalityTest {

    @Test fun `should create ploy4 classes`() {
        val testPhone: TestPolymorphicGroup4 = PhoneContact("PHONE", "+61", "98765432")
        testPhone.contactType shouldBe "PHONE"
        testPhone.shouldBeType<PhoneContact>().countryCode shouldBe "+61"
        testPhone.shouldBeType<PhoneContact>().localNumber shouldBe "98765432"
    }

}
