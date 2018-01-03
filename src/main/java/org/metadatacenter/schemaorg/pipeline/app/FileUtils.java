package org.metadatacenter.schemaorg.pipeline.app;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

  public static Path renameFileExtension(Path path, String newExtension) {
    String target;
    String source = path.toString();
    String currentExtension = getFileExtension(source);
    if (currentExtension.equals("")) {
      target = source + "." + newExtension;
    } else {
      target = source.replaceFirst(
          Pattern.quote("." + currentExtension) + "$",
          Matcher.quoteReplacement("." + newExtension));
    }
    return Paths.get(target);
  }

  public static String getFileExtension(String f) {
    String ext = "";
    int i = f.lastIndexOf('.');
    if (i > 0 && i < f.length() - 1) {
      ext = f.substring(i + 1);
    }
    return ext;
  }
}
