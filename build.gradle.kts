
plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
    // 添加 ShadowJar 插件
    id("com.github.johnrengelman.shadow") version "8.1.1" // 使用最新穩定版
}

application{
    mainClass.set("org.JScarlet.Main") // 應該是 org.JScarlet.Main
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.json:json:20230227")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.4")

    // JavaFX modules - 這些應該是你的應用程式實際使用的
    implementation(platform("org.openjfx:javafx-controls:20")) // 根據你的JDK內建JavaFX版本
    implementation("org.openjfx:javafx-fxml")
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "20"
    modules = listOf("javafx.controls", "javafx.fxml")


}

//tasks.jar {
//    manifest {
//        attributes["Main-Class"] = "org.JScarlet.Main"
//    }
//}

// 配置 ShadowJar 任務
tasks.shadowJar {
    archiveBaseName.set("AudioToText") // 生成的 JAR 文件名，例如 AudioToText.jar
    archiveClassifier.set("") // 清除默認的 classifier (例如 '-all')
    archiveVersion.set("") // 清除版本號在文件名中
    // 可選：如果你希望它包含 manifest 配置 (通常不需要，shadowJar 會自動處理)
    // manifest {
    //     attributes["Main-Class"] = "org.JScarlet.Main"
    // }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 使用 jpackage 打包為 .exe
// jpackage 配置

