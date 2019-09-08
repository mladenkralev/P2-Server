package p2.utils.pojo;

import com.google.gson.annotations.Expose;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import p2.utils.actions.P2Actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Profile {
    @Expose
    private String profileName;
    @Expose
    private Collection<JsonInstallableUnit> installableUnits = new ArrayList<>();
    @Expose
    private Map<String,String> properties;
    @Expose
    private int profileSize;

    public Profile(String profileName, Collection<IInstallableUnit> installableUnits, Map<String,String> properties) {
        this.profileName = profileName;
        for (IInstallableUnit iu : installableUnits) {
            this.installableUnits.add(new JsonInstallableUnit(iu));
        }
        this.profileSize = installableUnits.size();
        this.properties = new HashMap<>(properties);
    }
}
