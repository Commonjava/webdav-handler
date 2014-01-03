/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.webdav;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Map;

import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.spi.WebDavException;

/**
 * Servlet which provides support for WebDAV level 2.
 * 
 * the original class is org.apache.catalina.servlets.WebdavServlet by Remy
 * Maucherat, which was heavily changed
 * 
 * @author Remy Maucherat
 */

public class WebdavServlet
    extends WebDavServletBean
{

    private static final String ROOTPATH_PARAMETER = "rootpath";

    public WebdavServlet( final Map<String, String> initParams )
    {
        super( initParams );
    }

    public void init()
        throws WebDavException
    {
        // Parameters from web.xml
        String clazzName = getConfig().getInitParameter( "ResourceHandlerImplementation" );
        if ( clazzName == null || clazzName.equals( "" ) )
        {
            clazzName = LocalFileSystemStore.class.getName();
        }

        final File root = getFileRoot();

        final IWebdavStore webdavStore = constructStore( clazzName, root );

        final boolean lazyFolderCreationOnPut =
            getInitParameter( "lazyFolderCreationOnPut" ) != null && getInitParameter( "lazyFolderCreationOnPut" ).equals( "1" );

        final String dftIndexFile = getInitParameter( "default-index-file" );
        final String insteadOf404 = getInitParameter( "instead-of-404" );

        final int noContentLengthHeader = getIntInitParameter( "no-content-length-headers" );

        super.init( webdavStore, dftIndexFile, insteadOf404, noContentLengthHeader, lazyFolderCreationOnPut );
    }

    protected IWebdavStore constructStore( final String clazzName, final File root )
    {
        IWebdavStore webdavStore;
        try
        {
            final Class<?> clazz = WebdavServlet.class.getClassLoader()
                                                      .loadClass( clazzName );

            final Constructor<?> ctor = clazz.getConstructor( new Class[] { File.class } );

            webdavStore = (IWebdavStore) ctor.newInstance( new Object[] { root } );
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException( "some problem making store component", e );
        }
        return webdavStore;
    }

    private File getFileRoot()
    {
        String rootPath = getInitParameter( ROOTPATH_PARAMETER );
        if ( rootPath == null )
        {
            throw new WebdavException( "missing parameter: " + ROOTPATH_PARAMETER );
        }
        if ( rootPath.equals( "*WAR-FILE-ROOT*" ) )
        {
            String file = LocalFileSystemStore.class.getProtectionDomain()
                                                    .getCodeSource()
                                                    .getLocation()
                                                    .getFile()
                                                    .replace( '\\', '/' );
            if ( file.charAt( 0 ) == '/' && System.getProperty( "os.name" )
                                                  .indexOf( "Windows" ) != -1 )
            {
                file = file.substring( 1, file.length() );
            }

            final int ix = file.indexOf( "/WEB-INF/" );
            if ( ix != -1 )
            {
                rootPath = file.substring( 0, ix )
                               .replace( '/', File.separatorChar );
            }
            else
            {
                throw new WebdavException( "Could not determine root of war file. Can't extract from path '" + file + "' for this web container" );
            }
        }
        return new File( rootPath );
    }

}
