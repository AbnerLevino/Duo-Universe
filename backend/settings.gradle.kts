// Permite o Gradle baixar sozinho um JDK que o sistema não tenha (ex.: o Fedora não empacota mais
// o Java 21). Versão confirmada no Gradle Plugin Portal em 2026-11-03.
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "backend"
