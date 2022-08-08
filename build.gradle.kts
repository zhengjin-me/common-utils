import java.io.ByteArrayOutputStream

plugins {
    // global version
    val kotlinVersion: String by System.getProperties()
    val dokkaVersion: String by System.getProperties()
    val ktlintVersion: String by System.getProperties()
    val nexusPublishVersion: String by System.getProperties()
    val springBootVersion: String by System.getProperties()
    val springDependencyManagementVersion: String by System.getProperties()

    idea
    `maven-publish`
    signing
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version springDependencyManagementVersion
    id("org.jetbrains.dokka") version dokkaVersion
    id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    id("io.github.gradle-nexus.publish-plugin") version nexusPublishVersion
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
}

// val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
// 使用nexusPublishing组件不能写完整路径
val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/")
val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")

val mavenUsername = (findProperty("MAVEN_CENTER_USERNAME") ?: System.getenv("MAVEN_CENTER_USERNAME")) as String?
val mavenPassword = (findProperty("MAVEN_CENTER_PASSWORD") ?: System.getenv("MAVEN_CENTER_PASSWORD")) as String?

val commonsTextVersion: String by project
val hutoolVersion: String by project

group = "me.zhengjin"
// 使用最新的tag名称作为版本号
// version = { ext["latestTagVersion"] }

/**
 * 源码JDK版本
 */
java.sourceCompatibility = JavaVersion.VERSION_1_8
/**
 * 编译后字节码可运行环境的版本
 */
java.targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    mavenCentral()
//    maven {
//        url = releasesRepoUrl
//        credentials {
//            username = mavenUsername
//            password = mavenPassword
//        }
//    }
//    maven {
//        url = snapshotsRepoUrl
//        credentials {
//            username = mavenUsername
//            password = mavenPassword
//        }
//    }
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.springframework.boot:spring-boot-starter-validation")
    // Apache commons
    api("org.apache.commons:commons-text:$commonsTextVersion")
    api("cn.hutool:hutool-core:$hutoolVersion")
    // JDK中捆绑的jaxb版本过旧, 旧版不支持使用namespacePrefixMapper自定义命名空间前缀
    // spring-data-jpa 2.2.5中引用的hibernate 5.4.12中已经加入最新jaxb依赖,如果使用最新版本的spring boot可忽略该依赖
    api("org.glassfish.jaxb:jaxb-runtime:2.3.5")
    api(kotlin("reflect"))
    api(kotlin("stdlib-jdk8"))
    testCompileOnly("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.name)
                url.set("https://github.com/zhengjin-me/common-utils")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://zhengjin.me/licenses/MIT-License.txt")
                    }
                }
                developers {
                    developer {
                        id.set("fangzhengjin")
                        name.set("fangzhengjin")
                        email.set("fangzhengjin@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/zhengjin-me/common-utils")
                }
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
    }
// 普通私有库发布
//    repositories {
//        maven {
//            url = if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl
//            credentials {
//                username = mavenUsername
//                password = mavenPassword
//            }
//        }
//    }
}

// maven center 发布, 发布后自动释放
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(releasesRepoUrl)
            snapshotRepositoryUrl.set(snapshotsRepoUrl)
            username.set(mavenUsername)
            password.set(mavenPassword)
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

tasks {
    register("getLatestTagVersion") {
        ext["latestTagVersionNumber"] = ByteArrayOutputStream().use {
            try {
                exec {
                    commandLine("git", "rev-list", "--tags", "--max-count=1")
                    standardOutput = it
                }
            } catch (e: Exception) {
                logger.error("Failed to get latest tag version number: [${e.message}]")
                return@use "unknown"
            }
            return@use it.toString().trim()
        }

        ext["latestTagVersion"] = ByteArrayOutputStream().use {
            try {
                exec {
                    commandLine("git", "describe", "--tags", ext["latestTagVersionNumber"])
                    standardOutput = it
                }
            } catch (e: Exception) {
                logger.error("Failed to get latest tag version: [${e.message}]")
                return@use "unknown"
            }
            val tagName = it.toString().trim()
            return@use Regex("^v?(?<version>\\d+\\.\\d+.\\d+(?:-SNAPSHOT|-snapshot)?)\$").matchEntire(tagName)?.groups?.get("version")?.value
                ?: throw IllegalStateException("Failed to get latest tag version, tagName: [$tagName]")
        }
        project.version = ext["latestTagVersion"]!!
        ext["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT", true)
        println("当前构建产物: [${project.group}:${project.name}:${project.version}]")
    }

    build {
        // 执行build之前 先获取版本号
        dependsOn("getLatestTagVersion")
    }

    publish {
        // 执行publish之前 先获取版本号
        dependsOn("getLatestTagVersion")
    }

    bootJar {
        enabled = false
    }

    jar {
        enabled = true
        classifier = ""
    }

    /**
     * 定义那些注解修饰的类自动开放
     */
    allOpen {
        annotations(
            "javax.persistence.Entity",
            "javax.persistence.MappedSuperclass",
            "javax.persistence.Embeddable"
        )
    }

    test {
        useJUnitPlatform()
    }

    /**
     * kotlin编译
     */
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }

    withType<Sign>().configureEach {
        onlyIf { ext["isReleaseVersion"] as Boolean }
    }
}
