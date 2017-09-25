/**
 * Copyright (C) 2006-2017 Apache Software Foundation (https://sourceforge.net/p/webdav-servlet, https://github.com/Commonjava/webdav-handler)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.webdav.spi;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Locale;
import java.util.Set;

public interface WebdavRequest
{

    String getMethod();

    Principal getUserPrincipal();

    String getRequestURI();

    Set<String> getHeaderNames();

    String getHeader( String name );

    Set<String> getAttributeNames();

    String getAttribute( String name );

    Set<String> getParameterNames();

    String getParameter( String name );

    String getPathInfo();

    Locale getLocale();

    String getServerName();

    InputStream getInputStream()
        throws IOException;

    int getContentLength();

    String getContextPath();

    String getServicePath();

}
