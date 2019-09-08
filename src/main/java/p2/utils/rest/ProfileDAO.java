package p2.utils.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.query.QueryUtil;
import p2.utils.actions.DownloadProfiles;
import p2.utils.actions.ProvisionBundles;
import p2.utils.common.FileUtil;
import p2.utils.common.ServicesUtil;
import p2.utils.pojo.Profile;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static p2.utils.Constants.REPOSITORY_DIR_NAME;
import static p2.utils.DeleteConstants.BUILD_DIR;

@Path("/")
public class ProfileDAO {
    private static final Logger logger = Logger.getLogger(ProfileDAO.class.getName());



    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@QueryParam("username") String userNameQueryParam,
                           @QueryParam("profileName") String profileNameParam,
                           String body) throws ProvisionException {
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        Gson gson = new Gson();
        Map<String, ArrayList<Map>> json = (Map) gson.fromJson(body, Object.class);

        ProvisionBundles.provisionBundles(json.get("installableUnits"), profileNameParam, userNameQueryParam);

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
    @Path("/download")
    public Response download(@QueryParam("username") String userNameQueryParam,
                             @QueryParam("profile") String profile) throws IOException, ProvisionException, URISyntaxException, InterruptedException {
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        IProvisioningAgent agent = ServicesUtil.getAgent();
        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
        DownloadProfiles.downloadRaw(profile,userNameQueryParam);
        List<IProfile> profiles = Arrays.asList(profileRegistry.getProfiles());
        List names = profiles.stream().map(IProfile::getProfileId).collect(Collectors.toList());


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
        List<Profile> result = new LinkedList<>();


        List<IProfile> profiles = Arrays.asList(profileRegistry.getProfiles());
        for(IProfile profile : profiles) {
            //TODO duplication fro /get REST
            String profileName = profile.getProfileId();
            Map<String,String> profileProperties = profile.getProperties();
            Collection installableUnitsInProfile = profile.available(QueryUtil.ALL_UNITS, new NullProgressMonitor()).toUnmodifiableSet();

            Profile profilePojo = new Profile(profileName,
                    installableUnitsInProfile,
                    profileProperties
                    );
            result.add(profilePojo);
        }

        Gson gson = new Gson();
        String response = gson.toJson(result);
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
        List<IProfile> searchedProfiles = profiles.stream().filter(it -> it.getProfileId().equals(profileNameQueryParam)).collect(Collectors.toList());

        if (searchedProfiles.size() != 1) {
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
