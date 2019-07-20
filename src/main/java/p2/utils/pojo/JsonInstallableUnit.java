package p2.utils.pojo;

import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class JsonInstallableUnit {
    public Collection<IArtifactKey> artifacts;
    public Map<String, String> properties;
    public Collection<String> providedCapabilities = new HashSet<>();
    public Collection<String> requireCapabilities = new HashSet<>();

    public JsonInstallableUnit(IInstallableUnit installableUnit) {
        this.artifacts = installableUnit.getArtifacts();
        this.properties = installableUnit.getProperties();

        installableUnit.getProvidedCapabilities().forEach(it -> {
            if(!it.getName().equals("bundle")) {
                providedCapabilities.add("package " + it.getName() + " " + it.getVersion());
            }

        });
        installableUnit.getRequirements().forEach(it -> {
            requireCapabilities.add(it.toString());
        });
    }
}
