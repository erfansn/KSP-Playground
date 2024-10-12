package ir.erfansn.kspplayground.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.visitor.KSDefaultVisitor

class TestProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return TestProcessor(environment.codeGenerator, environment.logger)
  }
}

class TestProcessor(
  val codeGenerator: CodeGenerator,
  val kspLogger: KSPLogger,
) : SymbolProcessor {
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val symbols = resolver.getSymbolsWithAnnotation("ir.erfansn.kspplayground.Test")
    symbols.forEach {
      it.accept(TestVisitor(), it.annotations.first().arguments.first())
    }
    return emptyList()
  }

  private inner class TestVisitor : KSDefaultVisitor<KSValueArgument, Unit>() {
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: KSValueArgument) {
      val fileName = "Generated${function.simpleName.asString()}"
      val file = codeGenerator.createNewFile(Dependencies.ALL_FILES, "ir.erfansn.kspplayground.generated", fileName)
      file.writer().use {
        it.appendLine("val Generated${function.simpleName.asString()} = \"${data.value}\"")
      }
      kspLogger.info("Function", function)
    }

    override fun defaultHandler(node: KSNode, data: KSValueArgument) = Unit
  }
}