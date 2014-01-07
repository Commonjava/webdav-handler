package net.sf.webdav.spi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import net.sf.webdav.WebdavStatus;

public interface WebdavResponse
{

    void setStatus( WebdavStatus status );

    Writer getWriter();

    String encodeRedirectURL( String url );

    void sendRedirect( String redirectUrl );

    void addHeader( String name, String value );

    void sendError( WebdavStatus status );

    void sendError( WebdavStatus status, String requestUri );

    void setDateHeader( String name, long date );

    void setHeader( String name, String value );

    void setContentType( String type );

    void setContentLength( int length );

    OutputStream getOutputStream()
        throws IOException;

    void setCharacterEncoding( String encoding );

}
