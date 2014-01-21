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
