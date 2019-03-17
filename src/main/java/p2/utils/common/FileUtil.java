package p2.utils.common;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class FileUtil {
    private static BundleContext bc = FrameworkUtil.getBundle(FileUtil.class).getBundleContext();

    private static List<String> adjectivesList = new ArrayList<>();
    private static List<String> repositoryNounsList = new ArrayList<>();
    private static List<String> profileNounsList = new ArrayList<>();

    static {
        URL repositoryNouns = bc.getBundle().getResource("repositoryNouns.txt");
        URL profileNouns = bc.getBundle().getResource("profileNouns.txt");
        URL adjectives = bc.getBundle().getResource("adjectives.txt");

        repositoryNounsList.addAll(Objects.requireNonNull(getContentFromCsf(repositoryNouns)));
        adjectivesList.addAll(Objects.requireNonNull(getContentFromCsf(adjectives)));
        profileNounsList.addAll(Objects.requireNonNull(getContentFromCsf(profileNouns)));
    }

    public static String getUniqueRepositoryName() {
        int adjectiveIndex = new Random().nextInt(adjectivesList.size());
        int repositoryNouns = new Random().nextInt(repositoryNounsList.size());
        int pseudoRandomNumber = new Random().nextInt(99);

        return adjectivesList.get(adjectiveIndex) + repositoryNounsList.get(repositoryNouns) + pseudoRandomNumber;
    }

    public static String getUniqueProfileName() {
        int adjectiveIndex = new Random().nextInt(adjectivesList.size());
        int profileNouns = new Random().nextInt(profileNounsList.size());
        int pseudoRandomNumber = new Random().nextInt(99);

        return adjectivesList.get(adjectiveIndex) + profileNounsList.get(profileNouns) + pseudoRandomNumber;
    }

    private static List<String> getContentFromCsf(URL url) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            while (br.ready()) {
                return Arrays.asList(br.readLine().split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List getNouns() {
        return repositoryNounsList;
    }

}
