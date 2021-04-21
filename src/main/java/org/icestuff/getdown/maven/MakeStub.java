package org.icestuff.getdown.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Make deployable Java apps.
 */
@Mojo(name = "stub")
public class MakeStub extends AbstractGetdownMojo {
	/**
	 * The URL from which the client is downloaded.
	 */
	@Parameter(required = true)
	private String appbase;

	/**
	 * The directory in which files will be stored prior to processing.
	 */
	@Parameter(defaultValue = "${project.build.directory}/getdown-stub", required = true)
	private File stubWorkDirectory;

	/**
	 * flag that disables copying of getdown.jar into stub directory.
	 */
	@Parameter(defaultValue = "false")
	private boolean excludeJarFromStub;

	public void execute() throws MojoExecutionException {
		getLog().debug("using work directory " + stubWorkDirectory);
		Util.makeDirectoryIfNecessary(stubWorkDirectory);
		try {
			copyUIResources();
			makeConfigFile();
			if (!excludeJarFromStub) {
				copyGetdownClient();
			}
		} catch (MojoExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new MojoExecutionException("Failure to run the plugin: ", e);
		}
	}

	protected File getWorkDirectory() {
		return stubWorkDirectory;
	}

	protected void copyGetdownClient() throws MojoExecutionException {
		getLog().info("Copying client jar");
        Artifact getdown = (Artifact) plugin.getArtifactMap().get("com.threerings.getdown:getdown-launcher");
        if (getdown != null) {
			Util.copyFile(getdown.getFile(), new File(stubWorkDirectory, "getdown.jar"));
		} else {
			getLog().error("Failed copying client jar");
		}
	}

	protected void makeConfigFile() throws FileNotFoundException {
		getLog().info("Making stub getdown.txt");
		PrintWriter writer = new PrintWriter(new File(stubWorkDirectory, "getdown.txt"));
		try {
			writer.println("# The URL from which the client is downloaded");
			writer.println(String.format("appbase = %s", appbase));
			writer.println();
			writeUIConfiguration(writer);
			writer.println();
			writer.println("# Resources");
			writeJavaConfiguration(writer);
			writeTrackingConfiguration(writer);
			writeMaxConcurrentDownloads(writer);
			writeCustomParameter(writer);
		} finally {
			writer.close();
		}
	}
}
