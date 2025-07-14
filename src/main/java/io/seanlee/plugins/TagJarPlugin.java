package io.seanlee.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;


public class TagJarPlugin implements Plugin<Project> {

    private final ExecOperations execOperations;

    @Inject
    public TagJarPlugin(ExecOperations execOperations) {
        this.execOperations = execOperations;
    }

    @Override
    public void apply(@NotNull Project project) {

        String version = getVersion(project);
        String env = getEnv(project);

        project.getTasks().withType(Jar.class).configureEach(jar -> {
            jar.doFirst(task -> {
                String banner = "**********************************\n" +
                        "  application: " + project.getName() + "\n" +
                        "      version: " + version + "\n" +
                        "  environment: " + env + "\n" +
                        "**********************************";

                File bannerFile = project.file("build/resources/main/banner.txt");
                bannerFile.getParentFile().mkdirs();
                try {
                    java.nio.file.Files.write(bannerFile.toPath(), banner.getBytes());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            jar.getArchiveFileName().set(project.getName() + "-" + version + "-" + env + ".jar");
        });
    }

    private String getVersion(Project project) {
        String version = null;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try {
            execOperations.exec(execSpec -> {
                execSpec.commandLine("git", "describe", "--tags", "--always");
                execSpec.setStandardOutput(stdout);
                execSpec.setIgnoreExitValue(true);
            });
            String gitTag = stdout.toString().trim();
            if (!gitTag.isEmpty()) {
                version = gitTag;
            }
        } catch (Exception ignored) {

        }

        String ver = project.getVersion().toString();
        return null == version ? null == ver || ver.isEmpty() ? "0.0.1.SNAPSHOT" : ver : version;
    }

    private String getEnv(Project project) {
        String ENV = "env";
        if (project.hasProperty(ENV)) {
            Object ep = project.getProperties().get(ENV);
            if (null != ep) {
                String env = ((String) ep).trim();
                if (!env.isEmpty()) {
                    return env;
                }
            }
        }
        return "generic";
    }

}
