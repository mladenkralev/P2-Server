package p2.utils.actions;

import org.eclipse.equinox.internal.p2.artifact.repository.ArtifactRepositoryManager;
import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepositoryFactory;
import org.eclipse.equinox.internal.p2.metadata.repository.MetadataRepositoryManager;
import org.eclipse.equinox.internal.p2.metadata.repository.SimpleMetadataRepositoryFactory;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.PublisherInfo;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import p2.utils.common.ServicesUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;

//utiliaze this one
public class PublishBundles {
    public static IPublisherInfo createPublisherRepository(Path repository) throws ProvisionException, URISyntaxException {
        PublisherInfo result = new PublisherInfo();

        URI repositoryUri = repository.toUri();

        IMetadataRepository metadataRepository = new SimpleMetadataRepositoryFactory().create(
                repositoryUri, "Sample Metadata Repository",
                MetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, Collections.EMPTY_MAP);

        IArtifactRepository artifactRepository = new SimpleArtifactRepositoryFactory().create(
                repositoryUri, "Sample Artifact Repository",
                ArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, Collections.EMPTY_MAP);

        result.setMetadataRepository(metadataRepository);
        result.setArtifactRepository(artifactRepository);
        result.setArtifactOptions(IPublisherInfo.A_PUBLISH | IPublisherInfo.A_INDEX);

        addRepositoryToManagers(repositoryUri);

        return result;
    }

    public static IPublisherAction[] publishEverything(Path bundlesPath) {
        IPublisherAction[] result = new IPublisherAction[1];
        File[] bundleLocations = new File[1];
        bundleLocations[0] = bundlesPath.toFile();
        BundlesAction bundlesAction = new BundlesAction(bundleLocations);
        result[0] = bundlesAction;
        return result;
    }

    private static void addRepositoryToManagers(URI repositoryUri) throws ProvisionException {
        IProvisioningAgent agent = ServicesUtil.getAgent();
        //get the repository managers and define our repositories
        IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent.getService(
                IMetadataRepositoryManager.SERVICE_NAME);
        IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent.getService(
                IArtifactRepositoryManager.SERVICE_NAME);

        metadataManager.addRepository(repositoryUri);
        artifactManager.addRepository(repositoryUri);
    }
}
