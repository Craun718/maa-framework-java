plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("io.github.craun718.maafw.examples.QuickStart")
}

fun registerExample(name: String, className: String) {
    tasks.register<JavaExec>("run$name") {
        group = "examples"
        description = "Runs the $name example"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(className)
    }
}

registerExample("QuickStart", "io.github.craun718.maafw.examples.QuickStart")
registerExample("CustomRecognition", "io.github.craun718.maafw.examples.CustomRecognitionExample")
registerExample("CustomAction", "io.github.craun718.maafw.examples.CustomActionExample")
registerExample("AgentClient", "io.github.craun718.maafw.examples.AgentClientExample")
registerExample("AgentServer", "io.github.craun718.maafw.examples.AgentServerExample")
