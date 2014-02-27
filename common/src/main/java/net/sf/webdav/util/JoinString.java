package net.sf.webdav.util;

import java.util.Arrays;
import java.util.Collection;

public class JoinString
{

    private final String joint;

    private final Collection<?> items;

    public JoinString( final String joint, final Collection<?> items )
    {
        this.joint = joint;
        this.items = items;
    }

    public JoinString( final String joint, final Object[] items )
    {
        this.joint = joint;
        this.items = Arrays.asList( items );
    }

    @Override
    public String toString()
    {
        if ( items == null || items.isEmpty() )
        {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        for ( final Object item : items )
        {
            if ( sb.length() > 0 )
            {
                sb.append( joint );
            }

            sb.append( item );
        }

        return sb.toString();
    }

}
