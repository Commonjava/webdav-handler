/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.web.vertx.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sf.webdav.util.RequestUtil;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.commonjava.util.logging.Log4jUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.impl.DefaultVertx;

public class VertXInputStreamTest
{

    private static final String BASE = VertXInputStream.class.getSimpleName();

    @BeforeClass
    public static void setupClass()
    {
        Log4jUtil.configure( Level.DEBUG );
    }

    @Test
    public void readSimpleFileViaAsyncFile()
        throws IOException
    {
        final FileHandler fh = new FileHandler();

        final DefaultVertx v = new DefaultVertx();
        v.setContext( v.createEventLoopContext() );
        v.fileSystem()
         .open( getResource( BASE, "test-read.txt" ), fh );

        synchronized ( fh )
        {
            try
            {
                fh.wait();
            }
            catch ( final InterruptedException e )
            {
                return;
            }
        }

        InputStream stream = null;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            stream = new VertXInputStream( fh.af );
            IOUtils.copy( stream, baos );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }

        assertThat( new String( baos.toByteArray() ), equalTo( "This is a test!" ) );
    }

    @Test
    public void closeFileBeforeReadingAll()
        throws IOException
    {
        final FileHandler fh = new FileHandler();

        final DefaultVertx v = new DefaultVertx();
        v.setContext( v.createEventLoopContext() );
        v.fileSystem()
         .open( getResource( BASE, "test-early-close.txt" ), fh );

        synchronized ( fh )
        {
            try
            {
                fh.wait();
            }
            catch ( final InterruptedException e )
            {
                return;
            }
        }

        InputStream stream = null;
        try
        {
            stream = new VertXInputStream( fh.af );

            final byte[] buf = new byte[15];
            stream.read( buf );

            assertThat( new String( buf ), equalTo( "This is a test!" ) );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }
    }

    @Test
    public void closeFileBeforeReadingAny()
        throws IOException
    {
        final FileHandler fh = new FileHandler();

        final DefaultVertx v = new DefaultVertx();
        v.setContext( v.createEventLoopContext() );
        v.fileSystem()
         .open( getResource( BASE, "test-early-close.txt" ), fh );

        synchronized ( fh )
        {
            try
            {
                fh.wait();
            }
            catch ( final InterruptedException e )
            {
                return;
            }
        }

        InputStream stream = null;
        try
        {
            stream = new VertXInputStream( fh.af );
            //
            //            final byte[] buf = new byte[15];
            //            stream.read( buf );
            //
            //            assertThat( new String( buf ), equalTo( "This is a test!" ) );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }
    }

    private String getResource( final String base, final String... parts )
    {
        final String[] arry = new String[parts.length + 1];
        arry[0] = base;
        System.arraycopy( parts, 0, arry, 1, parts.length );

        final String path = RequestUtil.normalize( false, arry );

        final URL resource = Thread.currentThread()
                                   .getContextClassLoader()
                                   .getResource( path );

        if ( resource == null )
        {
            fail( "Cannot find classpath resource: " + path );
        }

        return resource.getPath();
    }

    private static final class FileHandler
        implements Handler<AsyncResult<AsyncFile>>
    {
        private AsyncFile af;

        @Override
        public synchronized void handle( final AsyncResult<AsyncFile> event )
        {
            af = event.result();
            notifyAll();
        }
    }

}
