package p2.utils;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import p2.utils.common.ServicesUtil;
import p2.utils.filter.CORSFilter;
import p2.utils.rest.ProfileDAO;
import p2.utils.rest.RepositoryDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Activator implements BundleActivator {

    private BundleContext bc;
    private ServiceTracker tracker;
    private HttpService httpService = null;
    private static final Logger logger = Logger.getLogger(Activator.class.getName());

    @Override
    public synchronized void start(BundleContext bundleContext) throws Exception {
        this.bc = bundleContext;
        ServiceReference refHttpService = bc.getServiceReference(HttpService.class.getName());
        HttpService httpService = (HttpService) bc.getService(refHttpService);
        System.out.println("Raboti");
        ResourceConfig repositoryConfiguration = new ResourceConfig(RepositoryDAO.class);
        repositoryConfiguration.register(MultiPartFeature.class);
        repositoryConfiguration.register(CORSFilter.class);

        ServletContainer servletContainer = new ServletContainer(repositoryConfiguration);
        httpService.registerServlet("/api/repository", servletContainer, null, httpService.createDefaultHttpContext());
        scan(RepositoryDAO.class);

        ResourceConfig profileConfiguration = new ResourceConfig(ProfileDAO.class);
        ServletContainer profileServletContainer = new ServletContainer(profileConfiguration);
        httpService.registerServlet("/api/profile", profileServletContainer, null, httpService.createDefaultHttpContext());
        scan(ProfileDAO.class);

        System.getProperties().forEach((key, value) -> System.out.println(key + "|" + value));

        ServicesUtil.createOrReturnExistingAgent();
    }

    public void scan(Class baseClass) {
        Resource resource = Resource.builder(baseClass).build();
        String uriPrefix = "";
        process(uriPrefix, resource);
    }

    private void process(String uriPrefix, Resource resource) {
        String pathPrefix = uriPrefix;
        List<Resource> resources = new ArrayList<>();
        resources.addAll(resource.getChildResources());
        if (resource.getPath() != null) {
            pathPrefix = pathPrefix + resource.getPath();
        }
        for (ResourceMethod method : resource.getAllMethods()) {
            if (method.getType().equals(ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR)) {
                resources.add(
                        Resource.from(resource.getResourceLocator()
                                .getInvocable().getDefinitionMethod().getReturnType()));
            } else {
                System.out.println(method.getHttpMethod() + "\t" + pathPrefix);
            }
        }
        for (Resource childResource : resources) {
            process(pathPrefix, childResource);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        ServiceReference refHttpService = context.getServiceReference(HttpService.class.getName());

        HttpService httpService = (HttpService) context.getService(refHttpService);

        httpService.unregister("/");

        ServicesUtil.destroyAgent();
    }
}
