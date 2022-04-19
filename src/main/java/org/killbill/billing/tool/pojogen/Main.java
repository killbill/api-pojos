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

@Command(name = "pojogen", version = "pojogen 1.1", mixinStandardHelpOptions = true)
public class Main implements Callable<Integer> {
    private static final Log log = new Log(Main.class);
    @Option(names = {"-v", "--verbose"}, description = "Print extra information during processing.")
    private boolean verbose;
    @Option(names = {"-e", "--example"}, description = "Print an example of a settings file to stdout and exit.")
    private boolean example;
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

    public Integer call() {
        try {
            if (this.verbose) {
                Log.setGlobal(Level.TRACE);
            }
            log.trace(this);
            if (this.example) {
                System.out.println(Resources.asString("settings.xml"));
                return 0;
            }
            Settings settings = null;
            File location = null;
            try {
                if (this.location != null && this.location.size() > 0) {
                    location = this.location.get(0);
                }
                if (location == null) {
                    log.warn("No setting file has been specified. Looking for settings.xml in current directory.");
                    location = new File("settings.xml");
                }
                settings = Settings.read(location);
            } catch (Exception e) {
                log.fatal(e);
                log.fatal("The settings file <%s> cannot be read. Exiting.", location);
                return 1;
            }
            log.trace("Settings: %s\n%s", this.location, settings);
            Charset encoding = StandardCharsets.UTF_8;
            String cwd = System.getProperty("user.dir");
            Templates templates = new Templates(encoding, Resources.class, "/templates");
            Configuration configuration = new Configuration(encoding, templates, settings);
            Generator generator = new Generator(configuration);
            generator.run();
        } catch (Exception e) {
            log.fatal(e);
        }
        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}

