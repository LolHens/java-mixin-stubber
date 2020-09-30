package de.lolhens.jstubber

import com.github.javaparser.StaticJavaParser

object Main {
  def main(args: Array[String]): Unit = {
    val importWhitelist: Seq[String] = Seq(
      "net.minecraft.",
      "org.spongepowered.",
      "java."
    )

    val stubber = new Stubber(
      importDeclaration => importWhitelist.exists(importDeclaration.getNameAsString.startsWith),
      _.isAnnotationPresent("Mixin"),
      _.getAnnotations.isNonEmpty
    )

    //val compilationUnit = StaticJavaParser.parse(getClass.getClassLoader.getResourceAsStream("FlowableFluidMixin.java"))
    val compilationUnit = StaticJavaParser.parse(getClass.getClassLoader.getResourceAsStream("BiomeColorsMixin.java"))
    stubber.stub(compilationUnit)
    println(compilationUnit)
  }
}
