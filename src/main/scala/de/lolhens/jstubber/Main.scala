package de.lolhens.jstubber

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.BlockStmt
import scala.util.chaining._
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object Main {
  def main(args: Array[String]): Unit = {
    val compilationUnit = StaticJavaParser.parse(getClass.getClassLoader.getResourceAsStream("FlowableFluidMixin.java"))
    compilationUnit.getAllComments.asScala.foreach(_.remove())
    //compilationUnit.get
    compilationUnit.getTypes.iterator().asScala.toSeq.foreach {tpe =>
      println(tpe.getName)
      println(tpe.getAnnotations.iterator().asScala.toSeq)
      println(tpe.getMembers.iterator().asScala.toSeq.map {
        case methodDeclaration: MethodDeclaration =>
          methodDeclaration.getAnnotations.asScala.foreach {e =>
            e.removeComment()
          }
          if (!methodDeclaration.isAbstract) {
            methodDeclaration.setBody(new BlockStmt().tap {e =>
              e.addStatement(StaticJavaParser.parseStatement("throw new UnsupportedOperationException();"))
            })
          } else {
            methodDeclaration
          }

        case e => e
      })
    }
  }
}
