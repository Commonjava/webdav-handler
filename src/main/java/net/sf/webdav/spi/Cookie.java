package net.sf.webdav.spi;

public interface Cookie
{

    String getName();

    String getValue();

    String getComment();

    String getDomain();

    int getMaxAge();

    String getPath();

    boolean getSecure();

    int getVersion();

}
