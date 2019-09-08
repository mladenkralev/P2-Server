package p2.utils.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.Publisher;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import p2.utils.actions.PublishBundles;
import p2.utils.common.FileUtil;
import p2.utils.pojo.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static p2.utils.Constants.*;
import static p2.utils.DeleteConstants.BUILD_DIR;

@Path("/")
public class RepositoryDAO {

    private static final Logger logger = Logger.getLogger(RepositoryDAO.class.getName());

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/create")
    public Response createRepository(@QueryParam("username") String userNameQueryParam,
                                     final FormDataMultiPart multiPart) throws IOException, ProvisionException, URISyntaxException {

        Map<String, List<FormDataBodyPart>> bodyParts = multiPart.getFields();

        deleteAndCreateDirectory(UPLOAD_DIR);
        createIfNotExisting(REPOSITORIES);

        // repositories follow this pattern repositories/<username>/<p2FolderRepo>
        java.nio.file.Path currentWorkingRepository = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam, CREATED,
                FileUtil.getUniqueRepositoryName());

        createIfNotExisting(currentWorkingRepository);

        StringBuffer fileDetails = new StringBuffer();
        for (Entry<String, List<FormDataBodyPart>> entry : bodyParts.entrySet()) {
            logger.info(entry.getKey());
            logger.info(entry.getValue().toString());

            for (FormDataBodyPart part : entry.getValue()) {
                FormDataContentDisposition file = part.getFormDataContentDisposition();
                InputStream inputStream = part.getEntityAs(InputStream.class);

                Files.copy(inputStream, Paths.get(UPLOAD_DIR.toString(),
                        file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        IPublisherInfo info = PublishBundles.createPublisherRepository(
                currentWorkingRepository);
        IPublisherAction[] actions = PublishBundles.publishEverything(
                UPLOAD_DIR);
        Publisher publisher = new Publisher(info);
        publisher.publish(actions, new NullProgressMonitor());

        //delete
        FileUtils.deleteDirectory(UPLOAD_DIR.toFile());
        /* Save multiple files */
        return Response.ok(fileDetails.toString()).build();
    }


    @GET
    @Path("/delete")
    public Response deleteRepository(@QueryParam("username") String userNameQueryParam,
                                     @QueryParam("repository") String repository) throws IOException {
        if (!REPOSITORIES.toFile().exists()) {
            // RETURN NO REPO FOUNDS
        }

        if (REPOSITORIES.toFile().list().length == 0) {
            // no users found
        }

        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam, CREATED);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        List<File> repositories = Arrays.asList(userRepositories.toFile().listFiles());
        List names = repositories.stream().filter(it -> it.getName().equals(repository)).map(File::getName).collect(Collectors.toList());
        System.out.println("asd");
        if (names.size() == 1) {
            java.nio.file.Path path = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam, repository);
            FileUtils.deleteDirectory(path.toFile());
            return Response.ok().build();
        }
        return Response.status(404).build();
    }

    @GET
    @Path("/list")
    public Response listRepositories(@QueryParam("username") String userNameQueryParam) throws ProvisionException {

        if (!REPOSITORIES.toFile().exists()) {
            // RETURN NO REPO FOUNDS
        }

        if (REPOSITORIES.toFile().list().length == 0) {
            // no users found
        }

        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam, CREATED);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        // use P2 api for repos?
        // maybe some more complex object should be returned, that will have something else than name
        List<File> repoDirectories = Arrays.asList(userRepositories.toFile().listFiles());
        List<Repository> repositories = repoDirectories.stream()
                .map(it -> new Repository(it.getName(), it))
                .collect(Collectors.toList());

        if (repositories.size() == 0) {
            // must return 4** error
        }

        Gson gson = new Gson();
        String response = gson.toJson(repositories);
        System.out.println(response);
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/get")
    public Response getRepository(@QueryParam("username") String userNameQueryParam,
                                  @QueryParam("repository") String repositoryNameQueryParam) {

        if (!REPOSITORIES.toFile().exists()) {
            // RETURN NO REPO FOUNDS
        }

        if (REPOSITORIES.toFile().list().length == 0) {
            // no users found
        }
        //  TODO IF IMPORTED ADD MORE LOGIC HERE
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam, CREATED);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        // maybe some more complex object should be returned, that will have something else than name
        List<File> repoDirectories = Arrays.asList(userRepositories.toFile().listFiles());
        for (File file : repoDirectories) {
            System.out.println(!file.getName().equals(repositoryNameQueryParam));
        }

        List<Repository> repositories = repoDirectories.stream()
                .filter(it -> it.getName().equals(repositoryNameQueryParam))
                .map(it -> new Repository(it.getName(), it))
                .collect(Collectors.toList());

        if (repositories.size() == 0) {
            // must return 4** error
        }

        Gson gson = new Gson();
        String response = gson.toJson(repositories);
        System.out.println(response);
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/listIUs")
    public Response getAllInstallableUnitsFromAllRepositories(@QueryParam("username") String userNameQueryParam) {

        if (!REPOSITORIES.toFile().exists()) {
            // RETURN NO REPO FOUNDS
        }

        if (REPOSITORIES.toFile().list().length == 0) {
            // no users found
        }
        //  TODO IF IMPORTED ADD MORE LOGIC HERE
        java.nio.file.Path userRepositories = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME, userNameQueryParam, CREATED);
        if (!userRepositories.toFile().exists()) {
            // cannot find repositories/<name>
        }

        List<File> repoDirectories = Arrays.asList(userRepositories.toFile().listFiles());

        List<Repository> repositories = repoDirectories.stream()
                .map(it -> new Repository(it.getName(), it))
                .collect(Collectors.toList());

        if (repositories.size() == 0) {
            // must return 4** error
        }

        Gson gson = new Gson();
        String response = gson.toJson(repositories);
        System.out.println(response);
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }



    private void createIfNotExisting(java.nio.file.Path directory) throws IOException {
        if (!directory.toFile().exists()) {
            directory.toFile().mkdirs();
        }
    }

    private void deleteAndCreateDirectory(java.nio.file.Path directory) throws IOException {
        FileUtils.deleteDirectory(directory.toFile());
        Files.createDirectory(directory);
    }
}
