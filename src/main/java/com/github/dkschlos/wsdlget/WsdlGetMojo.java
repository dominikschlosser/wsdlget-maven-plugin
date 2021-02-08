package com.github.dkschlos.wsdlget;

import com.github.dkschlos.wsdlget.internal.WsdlDownloader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

@Mojo(name = "wsdlget", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class WsdlGetMojo extends AbstractMojo {

    @Parameter(required = true)
    private List<WsdlDefinition> wsdls;

    @Parameter(defaultValue = "${project.basedir}/src/main/resources/wsdl")
    private String outputPath;

    @Parameter(defaultValue = "false")
    private boolean subfolderByServiceName;

    @Parameter(defaultValue = "false")
    private boolean clearOutputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File root = new File(outputPath);
        if (clearOutputDirectory) {
            deleteRecursively(root);
        }
        for (WsdlDefinition wsdl : wsdls) {
            WsdlDownloader downloader = new WsdlDownloader(root, wsdl, subfolderByServiceName);
            downloader.download();
        }
    }

    private static void deleteRecursively(File f) throws MojoExecutionException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursively(c);
            }
        }
        if (!f.delete()) {
            throw new MojoExecutionException("Failed to delete file: " + f);
        }
    }
}
