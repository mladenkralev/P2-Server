package p2.utils.common;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.nio.file.Paths;
import java.util.logging.Logger;

import static p2.utils.DeleteConstants.BUILD_DIR;

public class ServicesUtil {
    private static final Logger logger = Logger.getLogger(ServicesUtil.class.getName());
    private static BundleContext context = FrameworkUtil.getBundle(FileUtil.class).getBundleContext();

    private static IProvisioningAgent agent;

    public static IProvisioningAgent createOrReturnExistingAgent() throws ProvisionException {
        if (agent == null) {
            IProvisioningAgentProvider agentProvider = (IProvisioningAgentProvider) ServicesUtil
                    .getService(IProvisioningAgentProvider.SERVICE_NAME);
            agent = agentProvider.createAgent(Paths.get(BUILD_DIR, "p2").toUri());
            return agent;
        }
        return agent;
    }

    public static void destroyAgent() {
        agent.stop();
    }


    public static Object getService(String serviceName) {
        ServiceReference sr = context.getServiceReference(serviceName);
        if (sr == null) {
            logger.warning(String.format("Could not get service [%s]", serviceName));
        }
        return context.getService(sr);
    }


    public static IProvisioningAgent getAgent() throws ProvisionException {
        return createOrReturnExistingAgent();
    }
}
