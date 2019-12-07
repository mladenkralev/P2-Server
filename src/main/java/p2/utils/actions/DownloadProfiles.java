package p2.utils.actions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import p2.utils.common.ServicesUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static p2.utils.Constants.CREATED;
import static p2.utils.Constants.REPOSITORY_DIR_NAME;
import static p2.utils.DeleteConstants.BUILD_DIR;

public class DownloadProfiles {
    public static void downloadRaw(String profileName, String userNameQueryParam) throws ProvisionException, URISyntaxException, IOException, InterruptedException {
        IProvisioningAgent agent = ServicesUtil.getAgent();

        //get the repository managers and define our repositories
        IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent.getService(
                IMetadataRepositoryManager.SERVICE_NAME);
        IArtifactRepositoryManager artifactRepository = (IArtifactRepositoryManager) agent.getService(
                IArtifactRepositoryManager.SERVICE_NAME);

        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
        IProfile profile = profileRegistry.getProfile(profileName);

        if (profile == null) {
            // throw
        }

        Set<IInstallableUnit> installableUnitsFromProfile = profile.query(QueryUtil.ALL_UNITS, new NullProgressMonitor()).toUnmodifiableSet();
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam, CREATED);
        // refactor to java 8 using files.walk

        File resultFolder = new File(BUILD_DIR + File.separator + "download" + File.separator + profileName);


        List<File> repositories = Arrays.asList(Objects.requireNonNull(userRepositories.toFile().listFiles()));
        for (File repository : repositories) {
            for (Iterator<IInstallableUnit> iterator = installableUnitsFromProfile.iterator(); iterator.hasNext(); ) {
                IInstallableUnit currentInstallableUnit = iterator.next();

                IMetadataRepository metadataRepo = metadataManager.loadRepository(repository.toURI(), new NullProgressMonitor());

                Set<IInstallableUnit> toInstall = metadataRepo.query(QueryUtil.createIUQuery(currentInstallableUnit), new NullProgressMonitor()).toUnmodifiableSet();
                if (toInstall.size() != 0) {
                    for (IInstallableUnit installableUnit : toInstall) {
                        List<String> args = new ArrayList<String>();
                        args.add(BUILD_DIR + File.separator + "p2Agent" + File.separator + "p2Director.bat"); // command name
                        args.add(installableUnit.getId()); // IU for install
                        args.add(metadataRepo.getLocation().toString()); // source
                        args.add(resultFolder.getAbsolutePath()); // destination

                        ProcessBuilder pb = new ProcessBuilder(args);

                        Process p = pb.start();
                        p.waitFor(10, TimeUnit.SECONDS);
                    }

                    iterator.remove();
                }
            }
        }

        List<String> classpathOsgi = new ArrayList<>();
        String osgiRuntimeBundleName = null;

        File folder = Paths.get(resultFolder.toPath().toString(), "plugins").toFile();
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.getName().contains("org.eclipse.osgi_")) {
                osgiRuntimeBundleName = file.getName();
            } else {
                classpathOsgi.add(file.getName() + "@start");
            }
        }

        String classpath = classpathOsgi.stream().collect(Collectors.joining(","));

        File batFile = new File(resultFolder.getAbsolutePath(), "start.bat");
        FileWriter writer = new FileWriter(batFile);
        writer.write(String.format("java -Dosgi.bundles=%s -jar plugins/%s -console -consoleLog", classpath, osgiRuntimeBundleName));
        writer.flush();
        writer.close();

        Path zip = pack(Paths.get(resultFolder.getAbsolutePath(), profileName).toString() , BUILD_DIR + File.separator + "download");
    }

    public static Path pack(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
        return p;
    }
}
