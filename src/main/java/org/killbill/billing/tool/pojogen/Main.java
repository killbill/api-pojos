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

import org.apache.logging.log4j.Level;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "pojogen", version = "pojogen 1.2", mixinStandardHelpOptions = true)
public class Main implements Callable<Integer> {
    private static final Log log = new Log(Main.class);

    @Option(names = {"-v", "--verbose"}, description = "Print extra information during processing.")
    private boolean verbose;

    @Option(names = {"-e", "--example"}, description = "Print an example of a settings file to stdout and exit.")
    private boolean example;

    @Option(names = "--input",
            description = "Top level directory of interface classes.\n  Example: ./killbill-api/src/main/java.")
    private String input;

    @Option(names = "--input-dependencies",
            split = ",",
            description = "Location of JAR/library needed by -s/--source classes. \n" +
                    "Default value: <USER_HOME>/.m2")
    private String inputDependencies;

    @Option(names = "--input-packages",
            split = ",",
            description = "Comma-separated of package names in '--input' that should generated.\n" +
                          "  Default value: All packages in killbill-api project. Example:\n" +
                          "  com.acme.foo,com.acme.bar")
    private String[] inputPackages;

    @Option(names = "--output",
            description = "Top Level directory of generated classes location. Example:\n" +
                          "  ./killbill-plugin-framework-java/src/main/java")
    private String output;

    @Option(names = "--output-subpackage", description = "Sub-package location for generated classes.\n  Default value: boilerplate")
    private String outputSubPackage;

    @Option(names = "--output-resources",
            description = "Top Level directory of generated resources location. Example:\n" +
                          "  ./killbill-plugin-framework-java/src/main/resources")
    private String outputResources;

    @Option(names = "--output-test",
            description = "Top Level directory of generated resources location. Example:\n" +
                          "  ./killbill-plugin-framework-java/src/test/java")
    private String test;

    @Parameters(arity = "0..1", paramLabel = "settings.xml", description = "Specify the location of the settings file.")
    private List<File> location;

    public Main() {
        this.verbose = false;
        this.example = false;
        this.location = null;
    }

    public boolean getExample() {
        return this.example;
    }

    public List<File> getLocation() {
        return this.location;
    }

    public boolean getVerbose() {
        return this.verbose;
    }

    private void printExample() {
        if (this.example) {
            try {
                System.out.println(Resources.asString("settings.xml"));
            } catch (Exception e) {
                throw new RuntimeException("Example 'settings.xml' not found: " + e.getMessage());
            }
        }
    }

    public Integer call() {
        printExample();
        if (this.example) {
            // Quick exit if asking for example
            return 0;
        }

        if (this.verbose) {
            Log.setGlobal(Level.TRACE);
        }
        log.trace(this);

        final Settings settings;
        try {
            final File settingsXml = location == null ? null : location.get(0);

            SettingsLoader settingsLoader = new SettingsLoader(settingsXml);
            settingsLoader.overrideInputDependencyDirectory(inputDependencies);
            settingsLoader.overrideInputDirectory(input);
            settingsLoader.overrideInputPackagesDirectory(inputPackages);
            settingsLoader.overrideOutputDirectory(output);
            settingsLoader.overrideOutputSubpackageDirectory(outputSubPackage);
            settingsLoader.overrideOutputResourcesDirectory(outputResources);
            settingsLoader.overrideOutputTestDirectory(test);

            settings = settingsLoader.getSettings();
        } catch (Exception e) {
            log.error("Error when load settings: " + e);
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }

        log.trace("Settings: %s\n%s", this.location, settings);
        Charset encoding = StandardCharsets.UTF_8;
        Templates templates = new Templates(encoding, Resources.class, "/templates");
        Configuration configuration = new Configuration(encoding, templates, settings);
        try {
            Generator generator = new Generator(configuration);
            generator.run();
        } catch (Exception e) {
            log.error("Error when running generator: " + e);
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }

        return 0;
    }

    public static void main(String[] args) {
        System.setProperty("picocli.ignore.invalid.split", "true");
        System.exit(new CommandLine(new Main()).execute(args));
    }
}

