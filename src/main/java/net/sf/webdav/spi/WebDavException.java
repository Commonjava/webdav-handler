package net.sf.webdav.spi;

public class WebDavException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    public WebDavException( final Throwable cause )
    {
        super( cause );
    }

    public WebDavException( final String message )
    {
        super( message );
    }

}
