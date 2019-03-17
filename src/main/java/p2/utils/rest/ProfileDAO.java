package p2.utils.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import p2.utils.actions.ProvisionBundles;
import p2.utils.actions.PublishBundles;
import p2.utils.common.FileUtil;
import p2.utils.common.ServicesUtil;
import p2.utils.pojo.Profile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static p2.utils.DeleteConstants.BUILD_DIR;

@Path("/")
public class ProfileDAO {
    private static final Logger logger = Logger.getLogger(ProfileDAO.class.getName());
    private static final String UPLOAD_DIR_NAME = "uploadDir";
    private static final String REPOSITORY_DIR_NAME = "repositories";

    @GET
    @Path("/create")
    public Response create(@QueryParam("username") String userNameQueryParam,
                           @QueryParam("repository") String repositoryNameParam,
                           @QueryParam("profileName") String profileNameParam) throws ProvisionException {
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        List<File> repositories = Arrays.asList(userRepositories.toFile().listFiles());
        List names = repositories.stream().filter(it -> it.getName().equals(repositoryNameParam)).map(File::getName).collect(Collectors.toList());

        if (names.size() == 1) {
            java.nio.file.Path path = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME,
                    userNameQueryParam, repositoryNameParam);

            String profileName = "";
            if(profileNameParam == null || profileNameParam.equals("")) {
                profileName = FileUtil.getUniqueProfileName();
            }
            ProvisionBundles.provisionBundles(path, profileName);
        }

        return Response.status(200).build();
    }

    @GET
    @Path("/delete")
    public Response delete(@QueryParam("username") String userNameQueryParam,
                           @QueryParam("profile") String profile) throws IOException, ProvisionException {
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        // TODO validate input params!
        IProvisioningAgent agent = ServicesUtil.getAgent();
        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);

        profileRegistry.removeProfile(profile);
        return Response.ok().build();
    }

    @GET
    @Path("/list")
    public Response list(@QueryParam("username") String userNameQueryParam) throws ProvisionException {
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }
        IProvisioningAgent agent = ServicesUtil.getAgent();
        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);

        List<IProfile> profiles = Arrays.asList(profileRegistry.getProfiles());
        List names = profiles.stream().map(IProfile::getProfileId).collect(Collectors.toList());

        Gson gson = new Gson();
        String response = gson.toJson(names);

        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/get")
    public Response getRepository(@QueryParam("username") String userNameQueryParam,
                                  @QueryParam("profile") String profileNameQueryParam) throws ProvisionException {
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        // TODO validate input params!
        IProvisioningAgent agent = ServicesUtil.getAgent();
        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);

        List<IProfile> profiles = Arrays.asList(profileRegistry.getProfiles());
        List<IProfile> searchedProfiles= profiles.stream().filter(it -> it.getProfileId().equals(profileNameQueryParam)).collect(Collectors.toList());

        if(searchedProfiles.size() != 1) {
            // TODO no profile or many profiles with one name -> both cases are unacceptable
        }

        Profile profilePojo = new Profile(searchedProfiles.get(0).getProfileId(),
                searchedProfiles.get(0).available(QueryUtil.ALL_UNITS, new NullProgressMonitor()).toUnmodifiableSet(),
                searchedProfiles.get(0).getProperties());

        Gson gson = new Gson();
        String response = gson.toJson(profilePojo);
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }



}
