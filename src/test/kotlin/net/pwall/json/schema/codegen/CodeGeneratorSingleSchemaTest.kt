package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.json.schema.JSONSchema

class CodeGeneratorSingleSchemaTest {

    @Test fun `should generate from pre-loaded schema`() {
        val input = File("src/test/resources/example.schema.json")
        val schema = JSONSchema.parse(input)
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "Test", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generateClass(schema, "Test")
        expect(CodeGeneratorExampleTest.expectedExample) { stringWriter.toString() }
    }

}
