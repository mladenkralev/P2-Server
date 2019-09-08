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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        List<File> repositories = Arrays.asList(Objects.requireNonNull(userRepositories.toFile().listFiles()));
        for (File repository : repositories) {
            for (Iterator<IInstallableUnit> iterator = installableUnitsFromProfile.iterator(); iterator.hasNext(); ) {
                IInstallableUnit currentInstallableUnit = iterator.next();

                IMetadataRepository metadataRepo = metadataManager.loadRepository(repository.toURI(), new NullProgressMonitor());

                Set<IInstallableUnit> toInstall = metadataRepo.query(QueryUtil.createIUQuery(currentInstallableUnit), new NullProgressMonitor()).toUnmodifiableSet();
                if (toInstall.size() != 0) {
                    for (IInstallableUnit installableUnit : toInstall) {
                        List<String> args = new ArrayList<String>();
                        args.add(BUILD_DIR + File.separator + "p2Director.bat"); // command name
                        args.add(installableUnit.getId()); // IU for install
                        args.add(metadataRepo.getLocation().toString()); // source
                        args.add(BUILD_DIR + File.separator + profileName); // destination

                        ProcessBuilder pb = new ProcessBuilder(args);

                        Process p = pb.start();
                        p.waitFor(10, TimeUnit.SECONDS);
                    }

                    iterator.remove();
                }
            }

//            Path newRepositoryPath = Paths.get(BUILD_DIR, "profiles");
//
//            IPublisherInfo info = PublishBundles.createPublisherRepository(
//                    newRepositoryPath);
//            IPublisherAction[] actions = PublishBundles.publishEverything(
//                    Paths.get(metadataRepo.getLocation()));
//            Publisher publisher = new Publisher(info);
//            publisher.publish(actions, new NullProgressMonitor());
//            for(String installableUnit: installableUnitsFromProfile) {
//                IMetadataRepository metadataRepo = metadataManager.loadRepository(repository.toURI(), new NullProgressMonitor());
//                Collection toInstall = metadataRepo.query(QueryUtil.createIUQuery(installableUnit), new NullProgressMonitor()).toUnmodifiableSet();
//                // if it is found inside the repository, go install it
//                if(toInstall.size() != 0){
//                    ProvisioningSession session = new ProvisioningSession(agent);
//                    InstallOperation installOperation = new InstallOperation(session, toInstall);
//                    installOperation.setProfileId(profile.getProfileId());
//
//                    if (installOperation.resolveModal(new NullProgressMonitor()).isOK()) {
//                        Job job = installOperation.getProvisioningJob(new NullProgressMonitor());
//                        job.addJobChangeListener(new JobChangeAdapter() {
//                            public void done(IJobChangeEvent event) {
//                                System.out.println(event.getResult());
////                          agent.stop();
//                            }
//                        });
//                        job.schedule();
//                    }
//                }
        }

    }

//    private void writeDataToDisk(File file) {
//        String content = "This is the text content";
//        try (FileOutputStream fop = new FileOutputStream(file)) {
//
//            // if file doesn't exists, then create it
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//
//            // get the content in bytes
//            byte[] contentInBytes = content.getBytes();
//
//            fop.write(contentInBytes);
//            fop.flush();
//            fop.close();
//
//            System.out.println("Done");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//
//        IPublisherInfo info = PublishBundles.createPublisherRepository(
//                currentWorkingRepository);
//        IPublisherAction[] actions = PublishBundles.publishEverything(
//                UPLOAD_DIR);
//        Publisher publisher = new Publisher(info);
//        publisher.publish(actions, new NullProgressMonitor());

//    }
}
