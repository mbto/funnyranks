val joinFacesVersion: String by extra

//plugins {
//    id("org.joinfaces") version "4.0.12"
//}

dependencies {
// https://docs.joinfaces.org/current/reference/#_gradle
    compile("org.joinfaces:jsf-spring-boot-starter:$joinFacesVersion")
    compile("org.joinfaces:primefaces-spring-boot-starter:$joinFacesVersion")
    compile("org.primefaces.themes:glass-x:1.0.10")
//    compile("org.joinfaces:omnifaces1-spring-boot-starter:$joinFacesVersion")

    implementation("org.joinfaces:joinfaces-dependencies:$joinFacesVersion")

    compile(project(":service"))
}