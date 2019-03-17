package p2.utils.pojo;

import com.google.gson.annotations.Expose;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Profile {
    @Expose
    private String profileName;
    @Expose
    private Collection installableUnits;
    @Expose
    private Map<String,String> properties;

    public Profile(String profileName, Collection installableUnits, Map<String,String> properties) {
        this.profileName = profileName;
        this.installableUnits = installableUnits;
        this.properties = new HashMap<>(properties);
    }
}
