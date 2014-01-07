package net.sf.webdav.testutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.webdav.spi.WebdavRequest;

public class MockHttpServletRequest
    implements WebdavRequest
{

    private String method;

    private Principal userPrincipal;

    private String requestURI;

    private Map<String, String> headers = new HashMap<>();

    private Map<String, String> attributes = new HashMap<>();

    private Map<String, String> parameters = new HashMap<>();

    private InputStream inputStream;

    private String servletPath;

    private String contextPath;

    private String serverName;

    private Locale locale;

    private String pathInfo;

    @Override
    public String getMethod()
    {
        return method;
    }

    @Override
    public Principal getUserPrincipal()
    {
        return userPrincipal;
    }

    @Override
    public String getRequestURI()
    {
        return requestURI;
    }

    @Override
    public Set<String> getHeaderNames()
    {
        return headers.keySet();
    }

    @Override
    public String getHeader( final String name )
    {
        return headers.get( name );
    }

    @Override
    public Set<String> getAttributeNames()
    {
        return attributes.keySet();
    }

    @Override
    public String getAttribute( final String name )
    {
        return attributes.get( name );
    }

    @Override
    public Set<String> getParameterNames()
    {
        return parameters.keySet();
    }

    @Override
    public String getParameter( final String name )
    {
        return parameters.get( name );
    }

    @Override
    public String getPathInfo()
    {
        return pathInfo;
    }

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public String getServerName()
    {
        return serverName;
    }

    @Override
    public String getContextPath()
    {
        return contextPath;
    }

    @Override
    public String getServletPath()
    {
        return servletPath;
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        return inputStream;
    }

    @Override
    public int getContentLength()
    {
        final String cl = headers.get( "content-length" );
        return cl == null ? -1 : Integer.parseInt( cl );
    }

    public void setMethod( final String method )
    {
        this.method = method;
    }

    public void setUserPrincipal( final Principal userPrincipal )
    {
        this.userPrincipal = userPrincipal;
    }

    public void setRequestURI( final String requestURI )
    {
        this.requestURI = requestURI;
    }

    public void setHeaders( final Map<String, String> headers )
    {
        this.headers = headers;
    }

    public void setAttributes( final Map<String, String> attributes )
    {
        this.attributes = attributes;
    }

    public void setParameters( final Map<String, String> parameters )
    {
        this.parameters = parameters;
    }

    public void setInputStream( final InputStream inputStream )
    {
        this.inputStream = inputStream;
    }

    public void setServletPath( final String servletPath )
    {
        this.servletPath = servletPath;
    }

    public void setContextPath( final String contextPath )
    {
        this.contextPath = contextPath;
    }

    public void setServerName( final String serverName )
    {
        this.serverName = serverName;
    }

    public void setLocale( final Locale locale )
    {
        this.locale = locale;
    }

    public void setPathInfo( final String pathInfo )
    {
        this.pathInfo = pathInfo;
    }

    public void setAttribute( final String name, final String value )
    {
        attributes.put( name, value );
    }

    public void addHeader( final String name, final String value )
    {
        headers.put( name, value );
    }

    public void setContent( final byte[] resourceContent )
    {
        inputStream = new ByteArrayInputStream( resourceContent );
    }

}
