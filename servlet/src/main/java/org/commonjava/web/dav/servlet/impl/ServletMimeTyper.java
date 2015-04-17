/**
 * Copyright (C) 2006 Apache Software Foundation (jdcasey@commonjava.org)
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
package org.commonjava.web.dav.servlet.impl;

import javax.servlet.ServletContext;

import net.sf.webdav.spi.IMimeTyper;

public class ServletMimeTyper
    implements IMimeTyper
{

    private final ServletContext servletContext;

    public ServletMimeTyper( final ServletContext servletContext )
    {
        this.servletContext = servletContext;
    }

    @Override
    public String getMimeType( final String path )
    {
        return servletContext.getMimeType( path );
    }

}
