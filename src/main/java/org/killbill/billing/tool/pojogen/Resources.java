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

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
public class Resources {
  private static final Log log = new Log(Resources.class);
  public static String asString(String name) throws Exception{
    String result = null;
    StringWriter writer = new StringWriter();
    InputStream input = Resources.class.getClassLoader().getResourceAsStream(name);
    IOUtils.copy(input, writer, StandardCharsets.UTF_8);
    return writer.toString();
  }
}
