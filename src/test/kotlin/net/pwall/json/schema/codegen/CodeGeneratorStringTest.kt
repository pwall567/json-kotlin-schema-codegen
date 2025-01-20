/*
 * @(#) CodeGeneratorStringTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021, 2022 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.json.schema.codegen

import kotlin.test.Test

import java.io.File
import java.io.StringWriter

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorStringTest {

    @Test fun `should generate correct code for string validations`() {
        val input = File("src/test/resources/test-string.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestString", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestString.kt") + expected
    }

    @Test fun `should generate correct code for string validations in Java`() {
        val input = File("src/test/resources/test-string.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestString", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestString.java") + expectedJava
    }

    companion object {

        const val expected =
"""package com.example

import java.net.URI
import net.pwall.json.validation.JSONValidation

/**
 * Test string validations.
 */
data class TestString(
    val email1: String,
    val hostname1: String,
    val ipv4a: String,
    val ipv6a: String,
    val duration1: String? = null,
    val pointer1: String? = null,
    val maxlen: String,
    val minlen: String,
    val minlen2: String? = null,
    val fixedLen: String? = null,
    val rangeLen: String? = null,
    val name: String,
    val description: String,
    val uri: URI,
    val status: Status = Status.OPEN
) {

    init {
        require(JSONValidation.isEmail(email1)) { "email1 does not match format email - ${'$'}email1" }
        require(JSONValidation.isHostname(hostname1)) { "hostname1 does not match format hostname - ${'$'}hostname1" }
        require(JSONValidation.isIPV4(ipv4a)) { "ipv4a does not match format ipv4 - ${'$'}ipv4a" }
        require(JSONValidation.isIPV6(ipv6a)) { "ipv6a does not match format ipv6 - ${'$'}ipv6a" }
        if (duration1 != null)
            require(JSONValidation.isDuration(duration1)) { "duration1 does not match format duration - ${'$'}duration1" }
        if (pointer1 != null)
            require(JSONValidation.isJSONPointer(pointer1)) { "pointer1 does not match format json-pointer - ${'$'}pointer1" }
        require(maxlen.length <= 20) { "maxlen length > maximum 20 - ${'$'}{maxlen.length}" }
        require(minlen.isNotEmpty()) { "minlen length < minimum 1 - ${'$'}{minlen.length}" }
        if (minlen2 != null)
            require(minlen2.isNotEmpty()) { "minlen2 length < minimum 1 - ${'$'}{minlen2.length}" }
        if (fixedLen != null)
            require(fixedLen.length == 3) { "fixedLen length != constant 3 - ${'$'}{fixedLen.length}" }
        if (rangeLen != null)
            require(rangeLen.length in 1..6) { "rangeLen length not in range 1..6 - ${'$'}{rangeLen.length}" }
        require(cg_regex0.containsMatchIn(name)) { "name does not match pattern ${'$'}cg_regex0 - ${'$'}name" }
        require(cg_regex1.containsMatchIn(description)) { "description does not match pattern ${'$'}cg_regex1 - ${'$'}description" }
        require(!cg_regex2.containsMatchIn(description)) { "description matches pattern ${'$'}cg_regex2 - ${'$'}description" }
    }

    enum class Status {
        OPEN,
        CLOSED
    }

    companion object {
        private val cg_regex0 = Regex("^[A-Z][A-Za-z]*\${'$'}")
        private val cg_regex1 = Regex("^[A-Za-z0-9 ]*\${'$'}")
        private val cg_regex2 = Regex("^\\s*\${'$'}")
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.net.URI;
import java.util.regex.Pattern;
import net.pwall.json.validation.JSONValidation;

/**
 * Test string validations.
 */
public class TestString {

    private static final Pattern cg_regex0 = Pattern.compile("^[A-Z][A-Za-z]*${'$'}");
    private static final Pattern cg_regex1 = Pattern.compile("^[A-Za-z0-9 ]*${'$'}");
    private static final Pattern cg_regex2 = Pattern.compile("^\\s*${'$'}");

    private final String email1;
    private final String hostname1;
    private final String ipv4a;
    private final String ipv6a;
    private final String duration1;
    private final String pointer1;
    private final String maxlen;
    private final String minlen;
    private final String minlen2;
    private final String fixedLen;
    private final String rangeLen;
    private final String name;
    private final String description;
    private final URI uri;
    private final Status status;

    public TestString(
            String email1,
            String hostname1,
            String ipv4a,
            String ipv6a,
            String duration1,
            String pointer1,
            String maxlen,
            String minlen,
            String minlen2,
            String fixedLen,
            String rangeLen,
            String name,
            String description,
            URI uri,
            Status status
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
        if (duration1 != null && !JSONValidation.isDuration(duration1))
            throw new IllegalArgumentException("duration1 does not match format duration - " + duration1);
        this.duration1 = duration1;
        if (pointer1 != null && !JSONValidation.isJSONPointer(pointer1))
            throw new IllegalArgumentException("pointer1 does not match format json-pointer - " + pointer1);
        this.pointer1 = pointer1;
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
        if (minlen2 != null && minlen2.length() < 1)
            throw new IllegalArgumentException("minlen2 length < minimum 1 - " + minlen2.length());
        this.minlen2 = minlen2;
        if (fixedLen != null && fixedLen.length() != 3)
            throw new IllegalArgumentException("fixedLen length != constant 3 - " + fixedLen.length());
        this.fixedLen = fixedLen;
        if (rangeLen != null && (rangeLen.length() < 1 || rangeLen.length() > 6))
            throw new IllegalArgumentException("rangeLen length not in range 1..6 - " + rangeLen.length());
        this.rangeLen = rangeLen;
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        if (!cg_regex0.matcher(name).find())
            throw new IllegalArgumentException("name does not match pattern " + cg_regex0 + " - " + name);
        this.name = name;
        if (description == null)
            throw new IllegalArgumentException("Must not be null - description");
        if (!cg_regex1.matcher(description).find())
            throw new IllegalArgumentException("description does not match pattern " + cg_regex1 + " - " + description);
        if (cg_regex2.matcher(description).find())
            throw new IllegalArgumentException("description matches pattern " + cg_regex2 + " - " + description);
        this.description = description;
        if (uri == null)
            throw new IllegalArgumentException("Must not be null - uri");
        this.uri = uri;
        if (status == null)
            throw new IllegalArgumentException("Must not be null - status");
        this.status = status;
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

    public String getDuration1() {
        return duration1;
    }

    public String getPointer1() {
        return pointer1;
    }

    public String getMaxlen() {
        return maxlen;
    }

    public String getMinlen() {
        return minlen;
    }

    public String getMinlen2() {
        return minlen2;
    }

    public String getFixedLen() {
        return fixedLen;
    }

    public String getRangeLen() {
        return rangeLen;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public URI getUri() {
        return uri;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestString))
            return false;
        TestString cg_typedOther = (TestString)cg_other;
        if (!email1.equals(cg_typedOther.email1))
            return false;
        if (!hostname1.equals(cg_typedOther.hostname1))
            return false;
        if (!ipv4a.equals(cg_typedOther.ipv4a))
            return false;
        if (!ipv6a.equals(cg_typedOther.ipv6a))
            return false;
        if (duration1 == null ? cg_typedOther.duration1 != null : !duration1.equals(cg_typedOther.duration1))
            return false;
        if (pointer1 == null ? cg_typedOther.pointer1 != null : !pointer1.equals(cg_typedOther.pointer1))
            return false;
        if (!maxlen.equals(cg_typedOther.maxlen))
            return false;
        if (!minlen.equals(cg_typedOther.minlen))
            return false;
        if (minlen2 == null ? cg_typedOther.minlen2 != null : !minlen2.equals(cg_typedOther.minlen2))
            return false;
        if (fixedLen == null ? cg_typedOther.fixedLen != null : !fixedLen.equals(cg_typedOther.fixedLen))
            return false;
        if (rangeLen == null ? cg_typedOther.rangeLen != null : !rangeLen.equals(cg_typedOther.rangeLen))
            return false;
        if (!name.equals(cg_typedOther.name))
            return false;
        if (!description.equals(cg_typedOther.description))
            return false;
        if (!uri.equals(cg_typedOther.uri))
            return false;
        return status == cg_typedOther.status;
    }

    @Override
    public int hashCode() {
        int hash = email1.hashCode();
        hash ^= hostname1.hashCode();
        hash ^= ipv4a.hashCode();
        hash ^= ipv6a.hashCode();
        hash ^= (duration1 != null ? duration1.hashCode() : 0);
        hash ^= (pointer1 != null ? pointer1.hashCode() : 0);
        hash ^= maxlen.hashCode();
        hash ^= minlen.hashCode();
        hash ^= (minlen2 != null ? minlen2.hashCode() : 0);
        hash ^= (fixedLen != null ? fixedLen.hashCode() : 0);
        hash ^= (rangeLen != null ? rangeLen.hashCode() : 0);
        hash ^= name.hashCode();
        hash ^= description.hashCode();
        hash ^= uri.hashCode();
        return hash ^ status.hashCode();
    }

    public static class Builder {

        private String email1;
        private String hostname1;
        private String ipv4a;
        private String ipv6a;
        private String duration1;
        private String pointer1;
        private String maxlen;
        private String minlen;
        private String minlen2;
        private String fixedLen;
        private String rangeLen;
        private String name;
        private String description;
        private URI uri;
        private Status status;

        public Builder withEmail1(String email1) {
            this.email1 = email1;
            return this;
        }

        public Builder withHostname1(String hostname1) {
            this.hostname1 = hostname1;
            return this;
        }

        public Builder withIpv4a(String ipv4a) {
            this.ipv4a = ipv4a;
            return this;
        }

        public Builder withIpv6a(String ipv6a) {
            this.ipv6a = ipv6a;
            return this;
        }

        public Builder withDuration1(String duration1) {
            this.duration1 = duration1;
            return this;
        }

        public Builder withPointer1(String pointer1) {
            this.pointer1 = pointer1;
            return this;
        }

        public Builder withMaxlen(String maxlen) {
            this.maxlen = maxlen;
            return this;
        }

        public Builder withMinlen(String minlen) {
            this.minlen = minlen;
            return this;
        }

        public Builder withMinlen2(String minlen2) {
            this.minlen2 = minlen2;
            return this;
        }

        public Builder withFixedLen(String fixedLen) {
            this.fixedLen = fixedLen;
            return this;
        }

        public Builder withRangeLen(String rangeLen) {
            this.rangeLen = rangeLen;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withUri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder withStatus(Status status) {
            this.status = status;
            return this;
        }

        public TestString build() {
            return new TestString(
                    email1,
                    hostname1,
                    ipv4a,
                    ipv6a,
                    duration1,
                    pointer1,
                    maxlen,
                    minlen,
                    minlen2,
                    fixedLen,
                    rangeLen,
                    name,
                    description,
                    uri,
                    status
            );
        }

    }

    public enum Status {
        OPEN,
        CLOSED
    }

}
"""

    }

}
