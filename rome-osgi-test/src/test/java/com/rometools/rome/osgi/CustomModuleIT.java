/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rometools.rome.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.junit.Test;

public class CustomModuleIT {

    @Test
    public void testCustomModule() throws Exception {
        final XmlReader reader = new XmlReader(getClass().getResourceAsStream("custom-module.xml"));
        final SyndFeed feed = new SyndFeedInput().build(reader);

        final CustomModule customModule = (CustomModule) feed.getModule(CustomModule.URI);

        assertNotNull(customModule);
        assertEquals("test-title", customModule.getTitle());
    }
}
