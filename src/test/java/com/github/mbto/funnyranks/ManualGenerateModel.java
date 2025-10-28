package com.github.mbto.funnyranks;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generation model classes without compile project with unexisted model classes:
 * Execute gradle task: generateModel
 */
public class ManualGenerateModel {
    /**
     * invokes from gradle
     * */
    @SuppressWarnings("UnnecessaryModifier")
    public static void main(String[] args) throws Throwable {
        new ManualGenerateModel().generateModel();
    }

    private void generateModel() throws Throwable {
        if(System.getenv("GENERATE_MODEL") == null) {
            System.out.println("Skipped generating model, environment GENERATE_MODEL required");
            return;
        }
        String outputPath = System.getProperty("jooq.output.dir");
        if (outputPath == null) {
            throw new IllegalStateException("System property 'jooq.output.dir' is required for model generation");
        }
        String targetPackage = "com.github.mbto.funnyranks.common.model";
        Path targetPackagePath = Paths.get(outputPath).toAbsolutePath();
        System.out.println("targetPackage=" + targetPackage);
        System.out.println("targetPackagePath=" + targetPackagePath);
        Configuration configuration = new Configuration()
                .withJdbc(new Jdbc()
                        .withDriver("com.mysql.cj.jdbc.Driver")
                        .withUrl("jdbc:mysql://127.0.0.1:3306/")
                        .withUser("root")
                        .withPassword("root")
//                        .withProperties(
//                                new Property()
//                                        .withKey("user")
//                                        .withValue("root"),
//                                new Property()
//                                        .withKey("password")
//                                        .withValue("root")
//                        )
                )
                .withGenerator(new Generator()
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.mysql.MySQLDatabase")
                                .withIncludes(".*")
//                                .withExcludes("")
                                .withSchemata(
                                        new SchemaMappingType().withInputSchema("funnyranks")
                                        ,
                                        new SchemaMappingType().withInputSchema("funnyranks_stats")
                                )
                                .withForcedTypes(
                                        new ForcedType()
                                                .withTypes("(?i:TINYINT UNSIGNED)")
                                                .withName("BOOLEAN")
//                                        ,new ForcedType()
//                                                .withTypes("(?i:JSON)")
//                                                .withName("VARCHAR")
                                        , new ForcedType()
                                                .withTypes("(?i:JSON)")
                                                .withJsonConverter(true)
                                                .withIncludeExpression(".*\\.roles")
                                                .withUserType("java.util.TreeSet<java.lang.String>")
//                                                .withUserType("com.fasterxml.jackson.databind.JsonNode")
                                )
                        )
                        .withGenerate(new Generate()
                                .withDaos(false)
                                .withRoutines(true)
                                .withPojos(true)
                                .withPojosEqualsAndHashCode(true)
                                .withValidationAnnotations(true)
                                .withJavaTimeTypes(true)
//                                .withJpaAnnotations(true)
                        )
                        .withTarget(new Target()
                                .withDirectory(targetPackagePath.toString())
                                .withPackageName(targetPackage)
                                .withEncoding("UTF-8")
                                .withClean(true)
                        )
                );
        GenerationTool.generate(configuration);
        System.out.println("Successfully generated model with package " + targetPackage);
    }
}