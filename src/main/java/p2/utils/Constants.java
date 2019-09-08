package p2.utils;

import java.nio.file.Paths;

import static p2.utils.DeleteConstants.BUILD_DIR;

public class Constants {
    public static final String REPOSITORY_DIR_NAME = "repositories";
    public static final String UPLOAD_DIR_NAME = "uploadDir";
    public static final String CREATED = "created";
    public static final String IMPORTED = "imported";

    public static final java.nio.file.Path REPOSITORIES = Paths.get(BUILD_DIR, REPOSITORY_DIR_NAME);
    public static final java.nio.file.Path UPLOAD_DIR = Paths.get(BUILD_DIR, UPLOAD_DIR_NAME);
}
