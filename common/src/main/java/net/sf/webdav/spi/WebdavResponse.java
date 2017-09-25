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
import java.io.OutputStream;
import java.io.Writer;

import net.sf.webdav.WebdavStatus;

public interface WebdavResponse
{

    void setStatus( WebdavStatus status );

    Writer getWriter()
        throws IOException;

    String encodeRedirectURL( String url );

    void sendRedirect( String redirectUrl )
        throws IOException;

    void addHeader( String name, String value );

    void sendError( WebdavStatus status )
        throws IOException;

    void sendError( WebdavStatus status, String message )
        throws IOException;

    void setDateHeader( String name, long date );

    void setHeader( String name, String value );

    void setContentType( String type );

    void setContentLength( int length );

    OutputStream getOutputStream()
        throws IOException;

    void setCharacterEncoding( String encoding );

}
