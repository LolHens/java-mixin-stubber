# java-mixin-stubber
[![Release Notes](https://img.shields.io/github/release/LolHens/java-mixin-stubber.svg?maxAge=3600)](https://github.com/LolHens/java-mixin-stubber/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/de.lolhens/java-mixin-stubber)](https://search.maven.org/artifact/de.lolhens/java-mixin-stubber)
[![Apache License 2.0](https://img.shields.io/github/license/LolHens/java-mixin-stubber.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)

Removes everything from java source that is not needed for [Mixin's](https://github.com/SpongePowered/Mixin) RefMap generation. This currently includes comments, project specific imports and method bodies.

## Using this to generate a RefMap from src/main/scala in forgegradle
```gradle
buildscript {
    dependencies {
        classpath 'de.lolhens:java-mixin-stubber:0.0.2'
    }
}

def mixinstubsDir = file("$compileJava.temporaryDir/mixinstubs")

sourceSets.main.java.srcDirs += mixinstubsDir

def mixinstubs = task('mixinstubs') {
    doLast {
        delete(mixinstubsDir)
        de.lolhens.jstubber.Stubber.MIXIN.stubDirectory(sourceSets.main.scala.srcDirs[0].toPath(), mixinstubsDir.toPath())
    }
}

compileJava.dependsOn(mixinstubs)

compileJava.doLast {
    delete(compileJava.destinationDir)
    delete(mixinstubsDir)
}

compileScala.doFirst {
    compileScala.options.compilerArgs += compileJava.options.compilerArgs
}

mixin {
    add sourceSets.main, "${mod_id}.refmap.json"
}
```
