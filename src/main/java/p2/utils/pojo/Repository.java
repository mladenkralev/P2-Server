package p2.utils.pojo;

import com.google.gson.annotations.Expose;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import p2.utils.actions.P2Actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class Repository {
    @Expose
    private transient File repositoryDirectory;
    @Expose
    private String repositoryName;
    @Expose
    private int repoSize;
    @Expose
    private Collection<JsonInstallableUnit> installableUnits = new ArrayList<>();

    public Repository(String repositoryName, File repositoryDirectory) {
        this.repositoryName = repositoryName;
        this.repositoryDirectory = repositoryDirectory;
        try {
            Collection<IInstallableUnit> installableUnits = P2Actions.getInstallableUnits(repositoryDirectory);
            for (IInstallableUnit iu : installableUnits) {
                this.installableUnits.add(new JsonInstallableUnit(iu));
            }

            this.repoSize = installableUnits.size();
        } catch (ProvisionException e) {
            //ignore for now
        }
    }
}
