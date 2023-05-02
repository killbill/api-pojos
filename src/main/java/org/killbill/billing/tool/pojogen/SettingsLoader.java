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
import java.util.ArrayList;
import java.util.List;

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
    SettingsLoader(@Nullable final File settingsXml, final ProjectSourceType projectSourceType) throws Exception {
        if (settingsXml == null) {
            settings = projectSourceType.getDefaultSettings();
        } else if (!settingsXml.exists()) {
            throw new Exception("Try to use settingsXml: '" + settingsXml.getAbsolutePath() + "' but it's not exist");
        } else {
            settings = Settings.read(settingsXml);
        }

        if (settings.getDependencies() == null ||
            settings.getDependencies().isEmpty() ||
            settings.getDependencies().get(0) == null /* this one is weird: value is [null] */) {
            // Set dependency to local maven repository by default
            final File m2Dir = new File(FileUtils.getUserDirectoryPath() + SEPARATOR + ".m2");
            // use new ArrayList(List.of()) to make getDependencies() mutable. See #overrideSourceDependencyDirectories()
            settings.setDependencyDirectories(new ArrayList<>(List.of(m2Dir)));
        }
    }

    void overrideSourceDependencyDirectories(final String... sourceDependencyDirs) {
        if (sourceDependencyDirs == null) {
            return;
        }

        // We don't use source defined in XML configuration anymore.
        settings.getDependencies().clear();

        for (final String inputDependency : sourceDependencyDirs) {
            if (isStringExist(inputDependency)) {
                final File file = new File(inputDependency);
                if (isFileExist(file)) {
                    settings.getDependencies().add(file);
                } else {
                    log.warn("One of --source-dependency-dirs arguments point to non existent directory: " + file);
                }
            }
        }
    }

    void overrideSourceDirectories(final String... srcDirs) {
        if (srcDirs == null) {
            return;
        }

        // We don't use source defined in XML configuration anymore.
        settings.getSources().clear();

        for (final String srcDir : srcDirs) {
            if (isStringExist(srcDir)) {
                final File inputFile = new File(srcDir);
                if (isFileExist(inputFile)) {
                    settings.getSources().add(inputFile);
                } else {
                    log.warn("One of --source-dirs arguments point to non existent directory: " + inputFile);
                }
            }
        }

        if (settings.getSources().isEmpty()) {
            throw new IllegalArgumentException("Set '--source-dirs' to non-existent directory");
        }
    }

    void overrideSourcePackagesDirectory(String[] sourcePackages) {
        if (sourcePackages != null && sourcePackages.length > 0) {
            final List<String> packages = List.of(sourcePackages);
            log.trace("Set '--source-packages' directory to: " + packages);
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
