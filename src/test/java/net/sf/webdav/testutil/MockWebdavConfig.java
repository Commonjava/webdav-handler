package net.sf.webdav.testutil;

import net.sf.webdav.spi.WebdavConfig;

public class MockWebdavConfig
    implements WebdavConfig
{

    private boolean lazyFolderCreationOnPut;

    private boolean omitContentLengthHeaders;

    private String alt404Path;

    private String defaultIndexPath;

    @Override
    public boolean isLazyFolderCreationOnPut()
    {
        return lazyFolderCreationOnPut;
    }

    @Override
    public boolean isOmitContentLengthHeaders()
    {
        return omitContentLengthHeaders;
    }

    @Override
    public String getAlt404Path()
    {
        return alt404Path;
    }

    @Override
    public String getDefaultIndexPath()
    {
        return defaultIndexPath;
    }

    public void setLazyFolderCreationOnPut( final boolean lazyFolderCreationOnPut )
    {
        this.lazyFolderCreationOnPut = lazyFolderCreationOnPut;
    }

    public void setOmitContentLengthHeaders( final boolean omitContentLengthHeaders )
    {
        this.omitContentLengthHeaders = omitContentLengthHeaders;
    }

    public void setAlt404Path( final String alt404Path )
    {
        this.alt404Path = alt404Path;
    }

    public void setDefaultIndexPath( final String defaultIndexPath )
    {
        this.defaultIndexPath = defaultIndexPath;
    }

}
