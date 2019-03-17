package p2.utils.actions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import p2.utils.common.ServicesUtil;

import java.nio.file.Path;
import java.util.Collection;

public class ProvisionBundles {
    public static void provisionBundles(Path repository, String name) throws ProvisionException {
        IProvisioningAgent agent = ServicesUtil.getAgent();

        //get the repository managers and define our repositories
        IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent.getService(
                IMetadataRepositoryManager.SERVICE_NAME);

        IMetadataRepository metadataRepo = metadataManager.loadRepository(repository.toUri(), new NullProgressMonitor());
        Collection toInstall = metadataRepo.query(QueryUtil.ALL_UNITS, new NullProgressMonitor()).toUnmodifiableSet();
        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);

        IProfile profile = profileRegistry.getProfile(name) == null ?
                profileRegistry.addProfile(name) : profileRegistry.getProfile(name);

        ProvisioningSession session = new ProvisioningSession(agent);
        InstallOperation installOperation = new InstallOperation(session, toInstall);
        installOperation.setProfileId(profile.getProfileId());

        if (installOperation.resolveModal(new NullProgressMonitor()).isOK()) {
            Job job = installOperation.getProvisioningJob(new NullProgressMonitor());
            job.addJobChangeListener(new JobChangeAdapter() {
                public void done(IJobChangeEvent event) {
                    System.out.println(event.getResult());
//                    agent.stop();
                }
            });
            job.schedule();
        }
    }

}
