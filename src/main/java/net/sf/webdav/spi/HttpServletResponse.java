package net.sf.webdav.spi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import net.sf.webdav.WebdavStatus;

public interface HttpServletResponse
{

    void setStatus( int status );

    void setStatus( WebdavStatus status );

    Writer getWriter();

    String encodeRedirectURL( String string );

    void sendRedirect( String redirectURL );

    void addHeader( String name, String value );

    void sendError( WebdavStatus status );

    void sendError( WebdavStatus status, String requestURI );

    void sendError( int status );

    void sendError( int status, String requestURI );

    void setDateHeader( String name, long lastModified );

    void setHeader( String name, String value );

    void setContentType( String mimeType );

    void setContentLength( int resourceLength );

    OutputStream getOutputStream()
        throws IOException;

    void setCharacterEncoding( String encoding );

}
