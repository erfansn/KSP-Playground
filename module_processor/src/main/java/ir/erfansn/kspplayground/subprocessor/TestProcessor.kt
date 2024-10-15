package ir.erfansn.kspplayground.subprocessor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import ir.erfansn.kspplayground.annotation.KspTest
import java.util.Locale

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

  override fun process(resolver: Resolver): List<KSAnnotated> {
    // Until resolving: https://github.com/google/ksp/issues/1993
    if (counter++ != 0) return emptyList()

    resolver.getSymbolsWithAnnotation(KspTest::class.qualifiedName!!)
      .forEach {
        kspLogger.info("Sub-processor annotated declaration", it)

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
      val fileName = function.simpleName.asString()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

      val file = codeGenerator.createNewFile(
        Dependencies.ALL_FILES,
        "ir.erfansn.kspplayground",
        fileName
      )
      file.writer().use {
        it.appendLine("""
          package ir.erfansn.kspplayground
          
          import ir.erfansn.kspplayground.annotation.KspTest
          
          @KspTest("${data.value}")
          fun $fileName() {
            println("Hello from $fileName")
          }
        """.trimIndent())
      }
    }

    override fun defaultHandler(node: KSNode, data: KSValueArgument) = Unit
  }
}