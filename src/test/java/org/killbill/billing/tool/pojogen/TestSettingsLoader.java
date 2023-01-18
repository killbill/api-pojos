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

import com.google.common.io.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class TestSettingsLoader {

    private SettingsLoader getSettingsLoader(final String settingsXml) {
        try {
            final File file = settingsXml == null ?
                    null :
                    new File(Resources.getResource(settingsXml).toURI());
            return new SettingsLoader(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test(groups = "fast")
    void constructor() {
        SettingsLoader settingsLoader = getSettingsLoader(null);
        Settings settings = settingsLoader.getSettings();

        Assert.assertNotNull(settings.getSources());
        Assert.assertFalse(settings.getSources().isEmpty());
        Assert.assertEquals(settings.getSources().get(0), new File("./killbill-api/src/main/java"));

        Assert.assertNotNull(settings.getDependencies());
        Assert.assertFalse(settings.getDependencies().isEmpty());
        Assert.assertNotNull(settings.getDependencies().get(0));
        Assert.assertTrue(settings.getDependencies().get(0).getPath().contains("m2"));

        Assert.assertNotNull(settings.getOutput());
        Assert.assertEquals(settings.getOutput(), new File("./killbill-plugin-framework-java/src/main/java"));

        settingsLoader = getSettingsLoader("settings_with-dependency.xml");
        settings = settingsLoader.getSettings();
        Assert.assertEquals(settings.getDependencies().size(), 1);
        Assert.assertEquals(settings.getDependencies().get(0), new File("./lib"));
    }

    @Test(groups = "fast")
    void overrideInputDependencyDirectory() {
        final SettingsLoader settingsLoader = getSettingsLoader(null);
        Assert.assertTrue(settingsLoader.getSettings().getDependencies().get(0).getPath().contains("m2"));

        settingsLoader.overrideInputDependencyDirectory("src"); // This is maven its own structure
        Assert.assertFalse(settingsLoader.getSettings().getDependencies().get(0).getPath().contains("m2"));
        Assert.assertTrue(settingsLoader.getSettings().getDependencies().get(0).getPath().contains("src"));

        Assert.assertThrows(IllegalArgumentException.class, () -> settingsLoader.overrideOutputDirectory("non-existent"));
    }

    @Test(groups = "fast")
    void overrideInputDirectory() {
        final SettingsLoader settingsLoader = getSettingsLoader(null);
        Assert.assertTrue(settingsLoader.getSettings().getSources().get(0).getPath().contains("killbill-api"));

        settingsLoader.overrideInputDirectory("src");
        Assert.assertFalse(settingsLoader.getSettings().getSources().get(0).getPath().contains("killbill-api"));
        Assert.assertTrue(settingsLoader.getSettings().getSources().get(0).getPath().contains("src"));

        Assert.assertThrows(IllegalArgumentException.class, () -> settingsLoader.overrideInputDirectory("non-existent"));
    }

    @Test(groups = "fast")
    void overrideInputPackagesDirectory() {
        final SettingsLoader settingsLoader = getSettingsLoader(null);
        // Default settings.xml contains all killbill-api packages
        Assert.assertTrue(settingsLoader.getSettings().getPackages().size() > 30);

        settingsLoader.overrideInputPackagesDirectory(new String[] {"com.acme.foo", "com.acme.helper", "com.acme.blah"});
        Assert.assertEquals(settingsLoader.getSettings().getPackages().size(), 3);
    }

    @Test(groups = "fast")
    void overrideOutputDirectory() {
        final SettingsLoader settingsLoader = getSettingsLoader(null);
        Assert.assertTrue(settingsLoader.getSettings().getOutput().getPath().contains("killbill-plugin-framework-java"));

        settingsLoader.overrideOutputDirectory("src"); // This project "src" directory
        Assert.assertTrue(settingsLoader.getSettings().getOutput().getPath().contains("src"));

        Assert.assertThrows(IllegalArgumentException.class, () -> settingsLoader.overrideOutputDirectory("non-existent"));
    }

    @Test(groups = {"unix"}) // To avoid having a path/separator problem with windows based machine.
    void overrideOutputDirectoryWithMavenStructure() {
        final SettingsLoader settingsLoader = getSettingsLoader(null);

        settingsLoader.overrideOutputDirectory("../killbill/util/src/main/java");
        Assert.assertTrue(settingsLoader.getSettings().getOutput().getPath().contains(SettingsLoader.MAVEN_SRC_DIR));

        // Make sure that #overrideOutputResourcesDirectory() and #overrideOutputTestDirectory() called
        Assert.assertTrue(settingsLoader.getSettings().getResource().getPath().contains(SettingsLoader.MAVEN_RES_DIR));
        Assert.assertTrue(settingsLoader.getSettings().getTest().getPath().contains(SettingsLoader.MAVEN_TES_DIR));

        Assert.assertThrows(IllegalArgumentException.class, () -> settingsLoader.overrideOutputDirectory("non-existent"));
    }

    @Test(groups = "fast")
    void overrideOutputSubpackageDirectory() {
        final SettingsLoader settingsLoader = getSettingsLoader(null);
        Assert.assertEquals(settingsLoader.getSettings().getSubpackage(), "boilerplate");

        settingsLoader.overrideOutputSubpackageDirectory("impl");
        Assert.assertEquals(settingsLoader.getSettings().getSubpackage(), "impl");
    }

    @Test
    void overrideOutputResourcesDirectory() {
        final SettingsLoader settingsLoader = getSettingsLoader(null);
        Assert.assertTrue(settingsLoader.getSettings().getResource().getPath().contains("resources"));

        settingsLoader.overrideOutputResourcesDirectory("src"); // This project "src" directory
        Assert.assertTrue(settingsLoader.getSettings().getResource().getPath().contains("src"));
        // Make sure it's not pointing to "killbill-plugin-framework-java/src/main/resources" anymore
        Assert.assertFalse(settingsLoader.getSettings().getResource().getPath().contains("killbill-plugin-framework-java"));
        Assert.assertFalse(settingsLoader.getSettings().getResource().getPath().contains("main"));
        Assert.assertFalse(settingsLoader.getSettings().getResource().getPath().contains("resources"));

        Assert.assertThrows(IllegalArgumentException.class, () -> settingsLoader.overrideOutputResourcesDirectory("non-existent"));

        try {
            settingsLoader.overrideOutputResourcesDirectory("src", false);
        } catch (final Exception e) {
            Assert.fail("This should never happened because exceptionIfNotExist = false");
        }
    }

    @Test
    void overrideOutputTestDirectory() {
        final SettingsLoader settingsLoader = getSettingsLoader(null);
        Assert.assertTrue(settingsLoader.getSettings().getTest().getPath().contains("test"));

        settingsLoader.overrideOutputTestDirectory("src"); // This project "src" directory
        Assert.assertTrue(settingsLoader.getSettings().getTest().getPath().contains("src"));
        // Make sure it's not pointing to "killbill-plugin-framework-java/src/test/java" anymore
        Assert.assertFalse(settingsLoader.getSettings().getTest().getPath().contains("killbill-plugin-framework-java"));
        Assert.assertFalse(settingsLoader.getSettings().getTest().getPath().contains("test"));
        Assert.assertFalse(settingsLoader.getSettings().getTest().getPath().contains("java"));

        Assert.assertThrows(IllegalArgumentException.class, () -> settingsLoader.overrideOutputResourcesDirectory("non-existent"));

        try {
            settingsLoader.overrideOutputTestDirectory("src", false);
        } catch (final Exception e) {
            Assert.fail("This should never happened because exceptionIfNotExist = false");
        }
    }
}
