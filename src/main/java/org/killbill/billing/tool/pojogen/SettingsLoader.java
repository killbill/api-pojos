/*
 * Copyright 2022-2022 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.tool.pojogen;

import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class SettingsLoader {

    private static final String SEPARATOR = System.getProperty("file.separator");
    static final String MAVEN_SRC_DIR = "src" + SEPARATOR + "main" + SEPARATOR + "java";
    static final String MAVEN_RES_DIR = "src" + SEPARATOR + "main" + SEPARATOR + "resources";
    static final String MAVEN_TES_DIR = "src" + SEPARATOR + "test" + SEPARATOR + "java";

    private final Log log = new Log(SettingsLoader.class);
    private final Settings settings;

    /**
     * Create an instance with optional settings.xml . If null, then setting configuration will load from default
     * settings.xml in the classpath. If user try to set to non-existent file, then exception.
     */
    SettingsLoader(@Nullable final File settingsXml) throws Exception {
        if (settingsXml == null) {
            settings = getFromClasspath();
        } else if (!settingsXml.exists()) {
            throw new Exception("Try to use settingsXml: '" + settingsXml.getAbsolutePath() + "' but it's not exist");
        } else {
            settings = Settings.read(settingsXml);
        }

        if (settings.getDependencies() == null ||
            settings.getDependencies().isEmpty() ||
            settings.getDependencies().get(0) == null /* this one is weird: value is [null] */) {
            // Set dependency to local maven repository by default
            settings.setDependencyDirectories(List.of(new File(FileUtils.getUserDirectoryPath() + SEPARATOR + ".m2")));
        }
    }

    private Settings getFromClasspath() throws Exception {
        File file = Files.createTempFile("", "").toFile();
        FileUtils.copyInputStreamToFile(Objects.requireNonNull(getClass().getResourceAsStream("/settings.xml")), file);
        return Settings.read(file);
    }

    void overrideInputDependencyDirectory(final String inputDependencies) {
        if (isStringExist(inputDependencies)) {
            final File file = new File(inputDependencies);
            if (isFileExist(file)) {
                log.trace("Override inputDependencies directory to: " + inputDependencies);
                settings.setDependencyDirectories(List.of(file));
            } else {
                throw new IllegalArgumentException("Set '--input-dependencies' to non-existent directory: " + inputDependencies);
            }
        }
    }

    void overrideInputDirectory(final String input) {
        if (isStringExist(input)) {
            final File inputFile = new File(input);
            if (isFileExist(inputFile)) {
                log.trace("Override input directory to: " + input);
                settings.setSources(Collections.singletonList(inputFile));
            } else {
                throw new IllegalArgumentException("Set '--input' to non-existent directory");
            }
        }
    }

    void overrideInputPackagesDirectory(String[] inputPackages) {
        if (inputPackages != null && inputPackages.length > 0) {
            final List<String> packages = List.of(inputPackages);
            log.trace("Set '--input-packages' directory to: " + packages);
            settings.setPackages(packages);
        }
    }

    void overrideOutputDirectory(final String output) {
        if (isStringExist(output)) {
            final File file = new File(output);
            if (isFileExist(file)) {
                log.trace("Set '--output' directory to: " + output);
                settings.setOutput(file);

                String location = file.getAbsolutePath();
                if (location.contains(MAVEN_SRC_DIR)) {
                    overrideOutputResourcesDirectory(location.replace(MAVEN_SRC_DIR, MAVEN_RES_DIR), false);
                    overrideOutputTestDirectory(location.replace(MAVEN_SRC_DIR, MAVEN_TES_DIR), false);
                }
            } else {
                throw new IllegalArgumentException("Set '--output' to non-existent directory");
            }
        }
    }

    public void overrideOutputSubpackageDirectory(String outputSubPackage) {
        if (outputSubPackage != null && !outputSubPackage.isBlank()) {
            log.trace("Override outputSubPackage directory to: " + outputSubPackage);
            settings.setSubpackage(outputSubPackage);
        }
    }

    void overrideOutputResourcesDirectory(final String outputResources) {
        overrideOutputResourcesDirectory(outputResources, true);
    }

    void overrideOutputResourcesDirectory(final String outputResources, final boolean exceptionIfNotExist) {
        if (isStringExist(outputResources)) {
            final File file = new File(outputResources);
            if (isFileExist(file)) {
                log.trace("Set '--output-resources' directory to: " + outputResources);
                settings.setResource(file);
            } else {
                final String msg = "Set '--output-resources' to non-existent directory.";
                if (exceptionIfNotExist) {
                    throw new IllegalArgumentException(msg);
                } else {
                    log.warn(msg + " The output likely maven project, but doesn't have 'src/main/resources' directory. Skip resources generation");
                }
            }
        }
    }

    void overrideOutputTestDirectory(final String test) {
        overrideOutputTestDirectory(test, true);
    }

    void overrideOutputTestDirectory(final String test, final boolean exceptionIfNotExist) {
        if (isStringExist(test)) {
            final File file = new File(test);
            if (isFileExist(file)) {
                log.trace("Set '--output-test' directory to: " + test);
                settings.setTest(file);
            } else {
                final String msg = "Set '--output-test' to non-existent directory";
                if (exceptionIfNotExist) {
                    throw new IllegalArgumentException(msg);
                } else {
                    log.warn(msg + " The output likely maven project, but doesn't have 'src/test/java' directory. Skip test generation");
                }
            }
        }
    }

    Settings getSettings() {
        return settings;
    }

    private boolean isFileExist(final File file) {
        return file != null && file.exists();
    }

    private boolean isStringExist(String s) {
        return s != null && !s.isBlank();
    }
}
