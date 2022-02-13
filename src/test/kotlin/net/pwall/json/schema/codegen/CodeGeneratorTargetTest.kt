package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorTargetTest {

    @Test fun `should add to target list`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        val parser = Parser()
        codeGenerator.addTarget(parser.parse(input), "Test")
        expect(1) { codeGenerator.numTargets }
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        expect(createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample) { outputDetails.output() }
    }

}
