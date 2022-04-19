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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {
    private final Logger logger;

    public Log(Class<?> cls) {
        this.logger = LogManager.getLogger(cls);
        ;
    }

    public static void setGlobal(Level level) {
        Configurator.setRootLevel(level);
    }

    public void fatal(String s) {
        write(this.logger, Level.FATAL, s);
    }

    public void error(String s) {
        write(this.logger, Level.ERROR, s);
    }

    public void warn(String s) {
        write(this.logger, Level.WARN, s);
    }

    public void info(String s) {
        write(this.logger, Level.INFO, s);
    }

    public void debug(String s) {
        write(this.logger, Level.DEBUG, s);
    }

    public void trace(String s) {
        write(this.logger, Level.TRACE, s);
    }

    public static void write(Logger logger, Level level, String s) {
        logger.log(level, s);
    }

    public void fatal(String format, Object... vargs) {
        write(this.logger, Level.FATAL, format, vargs);
    }

    public void error(String format, Object... vargs) {
        write(this.logger, Level.ERROR, format, vargs);
    }

    public void warn(String format, Object... vargs) {
        write(this.logger, Level.WARN, format, vargs);
    }

    public void info(String format, Object... vargs) {
        write(this.logger, Level.INFO, format, vargs);
    }

    public void debug(String format, Object... vargs) {
        write(this.logger, Level.DEBUG, format, vargs);
    }

    public void trace(String format, Object... vargs) {
        write(this.logger, Level.TRACE, format, vargs);
    }

    public static void write(Logger logger, Level level, String format, Object... vargs) {
        write(logger, level, String.format(format, vargs));
    }

    public void fatal(Object o) {
        write(this.logger, Level.FATAL, o);
    }

    public void error(Object o) {
        write(this.logger, Level.ERROR, o);
    }

    public void warn(Object o) {
        write(this.logger, Level.WARN, o);
    }

    public void info(Object o) {
        write(this.logger, Level.INFO, o);
    }

    public void debug(Object o) {
        write(this.logger, Level.DEBUG, o);
    }

    public void trace(Object o) {
        write(this.logger, Level.TRACE, o);
    }

    public static void write(Logger logger, Level level, Object o) {
        write(logger, level, String.format("\n%s", o.toString()));
    }

    public void fatal(Exception e) {
        write(this.logger, Level.FATAL, e);
    }

    public void error(Exception e) {
        write(this.logger, Level.ERROR, e);
    }

    public void warn(Exception e) {
        write(this.logger, Level.WARN, e);
    }

    public static void write(Logger logger, Level level, Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        e.printStackTrace(printer);
        String s = writer.toString().replaceAll("\t", "  > ");
        write(logger, level, e.toString());
    }

    public void trace(Main main) {
        write(this.logger, Level.TRACE, main);
    }

    public static void write(Logger logger, Level level, Main main) {
        StringBuilder s = new StringBuilder();
        s.append("Commandline options : [");
        s.append(String.format(" --verbose=%s,", main.getVerbose()));
        s.append(String.format(" --example=%s,", main.getExample()));

        s.append(String.format(" --settings=%s ", ((main.getLocation() == null)
                || main.getLocation().isEmpty()) ? null : main.getLocation().get(0)));
        s.append("] ");
        write(logger, level, s.toString());
    }

    public void trace(Implementation implementation, File output) {
        write(this.logger, Level.TRACE, implementation, output);
    }

    public static void write(Logger logger, Level level, Implementation implementation, File output) {
        write(logger, level, String.format("Generated class %s\n  > implements %s\n    > at %s\n\n%s\n\n",
                implementation.getName(), implementation.getBase(), output, Text.indent(implementation.toString(), 6)));
    }
}
