package net.sf.webdav.spi;

public interface WebdavConfig
{

    boolean isLazyFolderCreationOnPut();

    boolean isOmitContentLengthHeaders();

    String getAlt404Path();

    String getDefaultIndexPath();

}
