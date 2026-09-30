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
    mainClass.set("top.natsuu.maafw.examples.QuickStart")
}

fun registerExample(name: String, className: String) {
    tasks.register<JavaExec>("run$name") {
        group = "examples"
        description = "Runs the $name example"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(className)
    }
}

registerExample("QuickStart", "top.natsuu.maafw.examples.QuickStart")
registerExample("CustomRecognition", "top.natsuu.maafw.examples.CustomRecognitionExample")
registerExample("CustomAction", "top.natsuu.maafw.examples.CustomActionExample")
registerExample("AgentClient", "top.natsuu.maafw.examples.AgentClientExample")
registerExample("AgentServer", "top.natsuu.maafw.examples.AgentServerExample")
