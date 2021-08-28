import dev.bombinating.gradle.jooq.*
import org.jooq.meta.jaxb.SchemaMappingType

dependencies {
    jooqRuntime("mysql:mysql-connector-java:8.0.21")
}

plugins {
    // https://github.com/bombinating/jooq-gradle-plugin
    id("dev.bombinating.jooq-codegen") version "1.7.0"
}

jooq {
    version = "3.11.12" //if migrates to new spring-boot version, can use new code-generator version 3.13.+
    jdbc {
        driver = "com.mysql.cj.jdbc.Driver"
        url = "jdbc:mysql://127.0.0.1:3306/"
        username = "root"
        password = "root"
    }
    generator {
        database {
            name = "org.jooq.meta.mysql.MySQLDatabase"

            val funnyranks = SchemaMappingType()
            funnyranks.inputSchema = "funnyranks"

            val funnyranks_stats = SchemaMappingType()
            funnyranks_stats.inputSchema = "funnyranks_stats"

            val funnyranks_maxmind_country = SchemaMappingType()
            funnyranks_maxmind_country.inputSchema = "funnyranks_maxmind_country"

            schemata = listOf(funnyranks, funnyranks_stats, funnyranks_maxmind_country)

            forcedTypes {
                forcedType {
                    types = "(?i:TINYINT UNSIGNED)"
                    name = "BOOLEAN"
                }
                forcedType {
                    types = "(?i:JSON)"
                    name = "VARCHAR"
                }
            }
        }
        target {
            directory = "$projectDir/src/main/java"
            packageName = "com.github.mbto.funnyranks.common.model"
            encoding = "UTF-8"
            isClean = true
        }
        generate {
            isDaos = false
            isRoutines = true
            isPojos = true
            isPojosEqualsAndHashCode = true
            isValidationAnnotations = true
            isJavaTimeTypes = true
        }
    }
}
// Auto-generation works before of compileJava, start the generation task manually
// tasks.getByName("compileJava").dependsOn(tasks.getByName("jooq"))