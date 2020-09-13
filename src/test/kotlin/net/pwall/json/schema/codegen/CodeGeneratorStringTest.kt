package net.pwall.json.schema.codegen

import net.pwall.json.schema.codegen.log.ConsoleLog
import java.io.File
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.expect

class CodeGeneratorStringTest {

    @Test fun `should generate correct code for string validations`() {
        val input = File("src/test/resources/test-string.schema.json")
        val codeGenerator = CodeGenerator(log = ConsoleLog)
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "TestString", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for string validations in Java`() {
        val input = File("src/test/resources/test-string.schema.json")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java", log = ConsoleLog)
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "TestString", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

import net.pwall.json.validation.JSONValidation

data class TestString(
        val email1: String,
        val hostname1: String,
        val ipv4a: String,
        val ipv6a: String,
        val maxlen: String,
        val minlen: String,
        val name: String
) {

    init {
        require(JSONValidation.isEmail(email1)) { "email1 does not match format email - ${'$'}email1" }
        require(JSONValidation.isHostname(hostname1)) { "hostname1 does not match format hostname - ${'$'}hostname1" }
        require(JSONValidation.isIPV4(ipv4a)) { "ipv4a does not match format ipv4 - ${'$'}ipv4a" }
        require(JSONValidation.isIPV6(ipv6a)) { "ipv6a does not match format ipv6 - ${'$'}ipv6a" }
        require(maxlen.length <= 20) { "maxlen length > maximum 20 - ${'$'}{maxlen.length}" }
        require(minlen.length >= 1) { "minlen length < minimum 1 - ${'$'}{minlen.length}" }
        require(cg_regex0 matches name) { "name does not match pattern ${'$'}cg_regex0 - ${'$'}name" }
    }

    companion object {
        private val cg_regex0 = Regex("^[A-Z][A-Za-z]*\${'$'}")
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.util.regex.Pattern;
import net.pwall.json.validation.JSONValidation;

public class TestString {

    private static final Pattern cg_regex0 = Pattern.compile("^[A-Z][A-Za-z]*${'$'}");

    private final String email1;
    private final String hostname1;
    private final String ipv4a;
    private final String ipv6a;
    private final String maxlen;
    private final String minlen;
    private final String name;

    public TestString(
            String email1,
            String hostname1,
            String ipv4a,
            String ipv6a,
            String maxlen,
            String minlen,
            String name
    ) {
        if (email1 == null)
            throw new IllegalArgumentException("Must not be null - email1");
        if (!JSONValidation.isEmail(email1))
            throw new IllegalArgumentException("email1 does not match format email - " + email1);
        this.email1 = email1;
        if (hostname1 == null)
            throw new IllegalArgumentException("Must not be null - hostname1");
        if (!JSONValidation.isHostname(hostname1))
            throw new IllegalArgumentException("hostname1 does not match format hostname - " + hostname1);
        this.hostname1 = hostname1;
        if (ipv4a == null)
            throw new IllegalArgumentException("Must not be null - ipv4a");
        if (!JSONValidation.isIPV4(ipv4a))
            throw new IllegalArgumentException("ipv4a does not match format ipv4 - " + ipv4a);
        this.ipv4a = ipv4a;
        if (ipv6a == null)
            throw new IllegalArgumentException("Must not be null - ipv6a");
        if (!JSONValidation.isIPV6(ipv6a))
            throw new IllegalArgumentException("ipv6a does not match format ipv6 - " + ipv6a);
        this.ipv6a = ipv6a;
        if (maxlen == null)
            throw new IllegalArgumentException("Must not be null - maxlen");
        if (maxlen.length() > 20)
            throw new IllegalArgumentException("maxlen length > maximum 20 - " + maxlen.length());
        this.maxlen = maxlen;
        if (minlen == null)
            throw new IllegalArgumentException("Must not be null - minlen");
        if (minlen.length() < 1)
            throw new IllegalArgumentException("minlen length < minimum 1 - " + minlen.length());
        this.minlen = minlen;
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        if (!cg_regex0.matcher(name).matches())
            throw new IllegalArgumentException("name does not match pattern " + cg_regex0 + " - " + name);
        this.name = name;
    }

    public String getEmail1() {
        return email1;
    }

    public String getHostname1() {
        return hostname1;
    }

    public String getIpv4a() {
        return ipv4a;
    }

    public String getIpv6a() {
        return ipv6a;
    }

    public String getMaxlen() {
        return maxlen;
    }

    public String getMinlen() {
        return minlen;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestString))
            return false;
        TestString typedOther = (TestString)other;
        if (!email1.equals(typedOther.email1))
            return false;
        if (!hostname1.equals(typedOther.hostname1))
            return false;
        if (!ipv4a.equals(typedOther.ipv4a))
            return false;
        if (!ipv6a.equals(typedOther.ipv6a))
            return false;
        if (!maxlen.equals(typedOther.maxlen))
            return false;
        if (!minlen.equals(typedOther.minlen))
            return false;
        return name.equals(typedOther.name);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash ^= email1.hashCode();
        hash ^= hostname1.hashCode();
        hash ^= ipv4a.hashCode();
        hash ^= ipv6a.hashCode();
        hash ^= maxlen.hashCode();
        hash ^= minlen.hashCode();
        hash ^= name.hashCode();
        return hash;
    }

}
"""

    }

}
