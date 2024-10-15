package ir.erfansn.kspplayground.processor

import com.google.devtools.ksp.KspExperimental
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
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import ir.erfansn.kspplayground.annotation.KspTest

class TestProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return TestProcessor(environment.codeGenerator, environment.logger)
  }
}

private class TestProcessor(
  val codeGenerator: CodeGenerator,
  val kspLogger: KSPLogger,
) : SymbolProcessor {

  var counter = 0

  @OptIn(KspExperimental::class)
  override fun process(resolver: Resolver): List<KSAnnotated> {
    // Until resolving: https://github.com/google/ksp/issues/1993
    if (counter++ != 0) return emptyList()

    resolver.getDeclarationsFromPackage("ir.erfansn.kspplayground")
      .filter {
        it.annotations.any {
          it.annotationType.resolve().declaration.qualifiedName?.asString() == KspTest::class.qualifiedName
        }
      }
      .forEach {
        kspLogger.info("Annotated declaration", it)

        it.accept(
          TestVisitor(),
          it.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == KspTest::class.qualifiedName
          }.arguments.first()
        )
      }
    return emptyList()
  }

  private inner class TestVisitor : KSDefaultVisitor<KSValueArgument, Unit>() {
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: KSValueArgument) {
      val fileName = "Generated${function.simpleName.asString()}"

      val file = codeGenerator.createNewFile(
        Dependencies.ALL_FILES,
        "ir.erfansn.kspplayground.generated",
        fileName
      )
      file.writer().use {
        it.appendLine("""
          package ir.erfansn.kspplayground.generated
          
          val Generated${function.simpleName.asString()} = "${data.value}"
        """.trimIndent())
      }
    }

    override fun defaultHandler(node: KSNode, data: KSValueArgument) = Unit
  }
}