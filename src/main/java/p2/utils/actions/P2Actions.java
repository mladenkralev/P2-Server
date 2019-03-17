package p2.utils.actions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import p2.utils.common.ServicesUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;

//remove this one
public class P2Actions {

    public static Collection<IInstallableUnit> getInstallableUnits(File repository) throws ProvisionException {
        IProvisioningAgent agent = ServicesUtil.getAgent();

        //get the repository managers and define our repositories
        IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
        IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);

        //Load and query the metadata
        IMetadataRepository metadataRepo = metadataManager.loadRepository(Paths.get(repository.getPath()).toUri(), new NullProgressMonitor());
        return metadataRepo.query(QueryUtil.ALL_UNITS, new NullProgressMonitor()).toUnmodifiableSet();

    }


}
