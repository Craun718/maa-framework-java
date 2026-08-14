import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless") version "8.9.0" apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<SpotlessExtension> {
        java {
            target("src/**/*.java")
            eclipse().configFile(rootProject.file("config/eclipse-java-format.xml"))
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
