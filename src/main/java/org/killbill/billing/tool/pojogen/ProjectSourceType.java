package org.killbill.billing.tool.pojogen;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public enum ProjectSourceType {
    API, PLUGIN_API;

    public Settings getDefaultSettings() {
        switch (this) {
            case API: return getFromClasspath("/killbill-api-config.xml");
            case PLUGIN_API: return getFromClasspath("/killbill-plugin-api-config.xml");
            default: throw new IllegalStateException("Unknown ProjectType");
        }
    }

    private Settings getFromClasspath(final String fileName) {
        try {
            File file = Files.createTempFile("", "").toFile();
            FileUtils.copyInputStreamToFile(Objects.requireNonNull(getClass().getResourceAsStream(fileName)), file);
            final Settings result = Settings.read(file);
            Files.deleteIfExists(file.toPath());
            return result;
        } catch (final Exception e) {
            throw new RuntimeException("Cannot get resource from classpath: " + e.getMessage());
        }
    }
}
