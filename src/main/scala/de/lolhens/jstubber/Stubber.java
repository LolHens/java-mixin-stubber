package de.lolhens.jstubber;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Stubber {
    private final Predicate<ImportDeclaration> filterImports;
    private final Predicate<TypeDeclaration<?>> filterTypes;
    private final Predicate<MethodDeclaration> filterMethods;

    public Stubber(Predicate<ImportDeclaration> filterImports,
                   Predicate<TypeDeclaration<?>> filterTypes,
                   Predicate<MethodDeclaration> filterMethods) {
        this.filterImports = filterImports;
        this.filterTypes = filterTypes;
        this.filterMethods = filterMethods;
    }

    private static final BlockStmt unsupportedOperationBlock;

    static {
        unsupportedOperationBlock = new BlockStmt();
        unsupportedOperationBlock.addStatement(StaticJavaParser.parseStatement("throw new UnsupportedOperationException();"));
    }

    public CompilationUnit stub(CompilationUnit compilationUnit) {
        new ArrayList<>(compilationUnit.getAllComments()).forEach(Comment::remove);

        new ArrayList<>(compilationUnit.getTypes()).stream().filter(filterTypes.negate()).forEach(Node::remove);

        compilationUnit.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
            if (!filterMethods.test(methodDeclaration)) {
                methodDeclaration.remove();
            } else if (!methodDeclaration.isAbstract()) {
                methodDeclaration.setBody(unsupportedOperationBlock);
            }
        });

        new ArrayList<>(compilationUnit.getImports()).stream().filter(filterImports.negate()).forEach(Node::remove);

        return compilationUnit;
    }
}
