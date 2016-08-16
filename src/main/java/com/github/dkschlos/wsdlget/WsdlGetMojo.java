package com.github.dkschlos.wsdlget;

import com.github.dkschlos.wsdlget.internal.WsdlDownloader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "wsdlget", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class WsdlGetMojo extends AbstractMojo {

	@Parameter(required = true)
	private List<WsdlDefinition> wsdls;

	@Parameter(defaultValue = "${project.basedir}/src/main/resources/wsdl")
	private String outputPath;

	@Parameter(defaultValue = "false")
	private boolean clearOutputDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Path root = Paths.get(outputPath);
		if (clearOutputDirectory) {
			clearDirectory(root);
		}
		for (WsdlDefinition wsdl : wsdls) {
			WsdlDownloader downloader = new WsdlDownloader(root, wsdl);
			downloader.download();
		}
	}

	private static void clearDirectory(Path dir) {
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			throw new RuntimeException("Can not clear output directory", ex);
		}
	}
}
