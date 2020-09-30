package de.lolhens.jstubber;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static final Stubber MIXIN = new Stubber(
            importDeclaration -> importDeclaration.getNameAsString().startsWith("java.") ||
                    importDeclaration.getNameAsString().startsWith("org.spongepowered.") ||
                    importDeclaration.getNameAsString().startsWith("net.minecraft."),
            typeDeclaration -> typeDeclaration.isAnnotationPresent("Mixin"),
            methodDeclaration -> methodDeclaration.getAnnotations().isNonEmpty()
    );

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

        if (compilationUnit.getTypes().isEmpty()) {
            return null;
        } else {
            return compilationUnit;
        }
    }

    public void stubFile(Path inputFile, Path outputFile) throws IOException {
        CompilationUnit inputCompilationUnit = StaticJavaParser.parse(inputFile);
        CompilationUnit outputCompilationUnit = stub(inputCompilationUnit);
        if (outputCompilationUnit != null) {
            String output = inputCompilationUnit.toString();
            Files.write(outputFile, output.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void stubDirectory(Path inputDir, Path outputDir) throws IOException {
        try {
            Files.walk(inputDir).forEach(inputFile -> {
                if (inputFile.getFileName().toString().endsWith(".java")) {
                    Path outputFile = outputDir.resolve(inputDir.relativize(inputFile));
                    try {
                        Files.createDirectories(outputFile.getParent());
                        stubFile(inputFile, outputFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw e;
            }
        }
    }
}
