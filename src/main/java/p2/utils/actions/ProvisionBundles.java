package p2.utils.actions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.internal.p2.engine.Profile;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import p2.utils.common.ServicesUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static p2.utils.Constants.CREATED;
import static p2.utils.Constants.REPOSITORY_DIR_NAME;
import static p2.utils.DeleteConstants.BUILD_DIR;

public class ProvisionBundles {
    // TEST IT
    public static void provisionBundles(List<Map> installableUnits, String profileName, String userNameQueryParam) throws ProvisionException {
        IProvisioningAgent agent = ServicesUtil.getAgent();

        //get the repository managers and define our repositories
        IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent.getService(
                IMetadataRepositoryManager.SERVICE_NAME);

        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
        Profile profile = (Profile) (profileRegistry.getProfile(profileName) == null ?
                profileRegistry.addProfile(profileName) : profileRegistry.getProfile(profileName));

        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam, CREATED);
        // refactor to java 8 using files.walk
        List<File> repositories = Arrays.asList(userRepositories.toFile().listFiles());

        for (File repository : repositories) {
            for (Map installableUnit : installableUnits) {
                // TODO if an installable unit is installed, no need to iterate the othe repositories
                String tempName = (String) installableUnit.get("repositoryName");
                String version = (String) installableUnit.get("version");
                // TODO do versioning
                Version iuVersion = Version.create(version);

                IMetadataRepository metadataRepo = metadataManager.loadRepository(repository.toURI(), new NullProgressMonitor());
                Collection<IInstallableUnit> toInstall = metadataRepo.query(QueryUtil.createIUQuery(tempName), new NullProgressMonitor())
                        .toUnmodifiableSet();

                // if it is found inside the repository, go install it
                if (toInstall.size() != 0) {
                    ProvisioningSession session = new ProvisioningSession(agent);

                    InstallOperation installOperation = new InstallOperation(session, toInstall);
                    installOperation.setProfileId(profile.getProfileId());

                    if (installOperation.resolveModal(new NullProgressMonitor()).isOK()) {
                        Job job = installOperation.getProvisioningJob(new NullProgressMonitor());
                        job.addJobChangeListener(new JobChangeAdapter() {
                            public void done(IJobChangeEvent event) {
                                System.out.println(event.getResult());
//                          agent.stop();
                            }
                        });
                        job.schedule();
                    }
                }
            }
        }


    }

}
