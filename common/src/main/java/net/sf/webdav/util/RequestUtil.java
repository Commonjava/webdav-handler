/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.webdav.util;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Map;
import java.util.TimeZone;

/**
 * General purpose request parsing and encoding utility methods.
 * 
 * @author Craig R. McClanahan
 * @author Tim Tye
 * @version $Revision: 1.2 $ $Date: 2008-08-05 07:38:45 $
 */

public final class RequestUtil
{

    /**
     * The DateFormat to use for generating readable dates in cookies.
     */
    private static SimpleDateFormat FORMAT = new SimpleDateFormat( " EEEE, dd-MMM-yy kk:mm:ss zz" );

    static
    {
        FORMAT.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }

    /**
     * Shamelessly copied from: http://stackoverflow.com/questions/6824157/parse-accept-language-header-in-java
     */
    public static Locale parseLocale( final String header )
    {
        //        String header = "en-ca,en;q=0.8,en-us;q=0.6,de-de;q=0.4,de;q=0.2";

        if ( header == null || header.trim()
                                     .length() < 1 )
        {
            return Locale.getDefault( Category.FORMAT );
        }

        final Map<Double, String> prefs = parseQualityHeader( header );

        final List<Double> sortedKeys = new ArrayList<Double>( prefs.keySet() );
        Collections.sort( sortedKeys );
        Collections.reverse( sortedKeys );

        final Locale[] available = Locale.getAvailableLocales();
        Locale result = null;
        for ( final Double key : sortedKeys )
        {
            final String[] parts = prefs.get( key )
                                        .split( "_" );

            for ( final Locale l : available )
            {
                if ( !parts[0].equals( l.getISO3Language() ) )
                {
                    continue;
                }

                if ( parts.length > 1 && !parts[1].equals( l.getISO3Country() ) )
                {
                    continue;
                }

                if ( parts.length > 2 && !parts[2].equals( l.getVariant() ) )
                {
                    continue;
                }

                result = l;
            }
        }

        return result == null ? Locale.getDefault( Category.FORMAT ) : result;
    }

    /**
     * Encode a cookie as per RFC 2109. The resulting string can be used as the
     * value for a <code>Set-Cookie</code> header.
     * 
     * @param cookie
     *      The cookie to encode.
     * @return A string following RFC 2109.
     */
    //    public static String encodeCookie( final Cookie cookie )
    //    {
    //
    //        final StringBuilder buf = new StringBuilder( cookie.getName() );
    //        buf.append( "=" );
    //        buf.append( cookie.getValue() );
    //
    //        final String comment = cookie.getComment();
    //        if ( comment != null )
    //        {
    //            buf.append( "; Comment=\"" );
    //            buf.append( comment );
    //            buf.append( "\"" );
    //        }
    //
    //        final String domain = cookie.getDomain();
    //        if ( domain != null )
    //        {
    //            buf.append( "; Domain=\"" );
    //            buf.append( domain );
    //            buf.append( "\"" );
    //        }
    //
    //        final int age = cookie.getMaxAge();
    //        if ( age >= 0 )
    //        {
    //            buf.append( "; Max-Age=\"" );
    //            buf.append( age );
    //            buf.append( "\"" );
    //        }
    //
    //        final String path = cookie.getPath();
    //        if ( path != null )
    //        {
    //            buf.append( "; Path=\"" );
    //            buf.append( path );
    //            buf.append( "\"" );
    //        }
    //
    //        if ( cookie.getSecure() )
    //        {
    //            buf.append( "; Secure" );
    //        }
    //
    //        final int version = cookie.getVersion();
    //        if ( version > 0 )
    //        {
    //            buf.append( "; Version=\"" );
    //            buf.append( version );
    //            buf.append( "\"" );
    //        }
    //
    //        return ( buf.toString() );
    //    }

    public static Map<Double, String> parseQualityHeader( final String header )
    {
        final Map<Double, String> parsed = new HashMap<Double, String>();
        for ( final String str : header.split( "," ) )
        {
            final String[] arr = str.trim()
                                    .replace( "-", "_" )
                                    .split( ";" );

            //Parse the q-value
            Double q = 1.0D;
            for ( String s : arr )
            {
                s = s.trim();
                if ( s.startsWith( "q=" ) )
                {
                    q = Double.parseDouble( s.substring( 2 )
                                             .trim() );
                    break;
                }
            }

            parsed.put( q, arr[0] );
        }

        return parsed;
    }

    /**
     * Filter the specified message string for characters that are sensitive in
     * HTML. This avoids potential attacks caused by including JavaScript codes
     * in the request URL that is often reported in error messages.
     * 
     * @param message
     *      The message string to be filtered
     */
    public static String filter( final String message )
    {

        if ( message == null )
        {
            return ( null );
        }

        final char content[] = new char[message.length()];
        message.getChars( 0, message.length(), content, 0 );
        final StringBuilder result = new StringBuilder( content.length + 50 );
        for ( final char element : content )
        {
            switch ( element )
            {
                case '<':
                    result.append( "&lt;" );
                    break;
                case '>':
                    result.append( "&gt;" );
                    break;
                case '&':
                    result.append( "&amp;" );
                    break;
                case '"':
                    result.append( "&quot;" );
                    break;
                default:
                    result.append( element );
            }
        }
        return ( result.toString() );

    }

    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it. <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths. It does not try
     * to perform security checks for malicious input.
     * @param absolutize 
     * 
     * @param path
     *      Relative path to be normalized
     */
    public static String deRelativize( final boolean absolutize, final String path )
    {

        if ( path == null )
        {
            return null;
        }

        // Create a place for the normalized path
        String result = path;

        if ( result.equals( "/." ) )
        {
            return "/";
        }

        // Add a leading "/" if necessary
        if ( absolutize && !result.startsWith( "/" ) )
        {
            result = "/" + result;
        }

        // Resolve occurrences of "//" in the normalized path
        while ( true )
        {
            final int index = result.indexOf( "//" );
            if ( index < 0 )
            {
                break;
            }
            result = result.substring( 0, index ) + result.substring( index + 1 );
        }

        // Resolve occurrences of "/./" in the normalized path
        while ( true )
        {
            final int index = result.indexOf( "/./" );
            if ( index < 0 )
            {
                break;
            }
            result = result.substring( 0, index ) + result.substring( index + 2 );
        }

        // Resolve occurrences of "/../" in the normalized path
        while ( true )
        {
            final int index = result.indexOf( "/../" );
            if ( index < 0 )
            {
                break;
            }
            if ( index == 0 )
            {
                return ( null ); // Trying to go outside our context
            }
            final int index2 = result.lastIndexOf( '/', index - 1 );
            result = result.substring( 0, index2 ) + result.substring( index + 3 );
        }

        // Return the normalized path that we have completed
        return result;

    }

    public static String normalize( final boolean absolutize, final String... path )
    {
        if ( path == null || path.length < 1 )
        {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        int idx = 0;
        for ( String part : path )
        {
            if ( part.length() < 1 || "/".equals( part ) )
            {
                continue;
            }

            if ( idx == 0 && part.startsWith( "file:" ) )
            {
                if ( part.length() > 5 )
                {
                    sb.append( part.substring( 5 ) );
                }

                continue;
            }

            if ( idx > 0 )
            {
                while ( part.charAt( 0 ) == '/' )
                {
                    if ( part.length() < 2 )
                    {
                        continue;
                    }

                    part = part.substring( 1 );
                }
            }

            while ( part.charAt( part.length() - 1 ) == '/' )
            {
                if ( part.length() < 2 )
                {
                    continue;
                }

                part = part.substring( 0, part.length() - 1 );
            }

            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }

            sb.append( part );
            idx++;
        }

        return deRelativize( absolutize, sb.toString() );
    }

    /**
     * Parse the character encoding from the specified content type header. If
     * the content type is null, or there is no explicit character encoding,
     * <code>null</code> is returned.
     * 
     * @param contentType
     *      a content type header
     */
    public static String parseCharacterEncoding( final String contentType )
    {

        if ( contentType == null )
        {
            return ( null );
        }
        final int start = contentType.indexOf( "charset=" );
        if ( start < 0 )
        {
            return ( null );
        }
        String encoding = contentType.substring( start + 8 );
        final int end = encoding.indexOf( ';' );
        if ( end >= 0 )
        {
            encoding = encoding.substring( 0, end );
        }
        encoding = encoding.trim();
        if ( ( encoding.length() > 2 ) && ( encoding.startsWith( "\"" ) ) && ( encoding.endsWith( "\"" ) ) )
        {
            encoding = encoding.substring( 1, encoding.length() - 1 );
        }
        return ( encoding.trim() );

    }

    //    /**
    //     * Parse a cookie header into an array of cookies according to RFC 2109.
    //     * 
    //     * @param header
    //     *      Value of an HTTP "Cookie" header
    //     */
    //    public static Cookie[] parseCookieHeader( String header )
    //    {
    //
    //        if ( ( header == null ) || ( header.length() < 1 ) )
    //        {
    //            return ( new Cookie[0] );
    //        }
    //
    //        final ArrayList<Cookie> cookies = new ArrayList<Cookie>();
    //        while ( header.length() > 0 )
    //        {
    //            int semicolon = header.indexOf( ';' );
    //            if ( semicolon < 0 )
    //            {
    //                semicolon = header.length();
    //            }
    //            if ( semicolon == 0 )
    //            {
    //                break;
    //            }
    //            final String token = header.substring( 0, semicolon );
    //            if ( semicolon < header.length() )
    //            {
    //                header = header.substring( semicolon + 1 );
    //            }
    //            else
    //            {
    //                header = "";
    //            }
    //            try
    //            {
    //                final int equals = token.indexOf( '=' );
    //                if ( equals > 0 )
    //                {
    //                    final String name = token.substring( 0, equals )
    //                                             .trim();
    //                    final String value = token.substring( equals + 1 )
    //                                              .trim();
    //                    cookies.add( new CookieImpl( name, value ) );
    //                }
    //            }
    //            catch ( final Throwable e )
    //            {
    //                ;
    //            }
    //        }
    //
    //        return ( cookies.toArray( new Cookie[cookies.size()] ) );
    //
    //    }

    /**
     * Append request parameters from the specified String to the specified Map.
     * It is presumed that the specified Map is not accessed from any other
     * thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: URL decoding is performed
     * individually on the parsed name and value elements, rather than on the
     * entire query string ahead of time, to properly deal with the case where
     * the name or value includes an encoded "=" or "&" character that would
     * otherwise be interpreted as a delimiter.
     * 
     * @param map
     *      Map that accumulates the resulting parameters
     * @param data
     *      Input string containing request parameters
     * 
     * @exception IllegalArgumentException
     *      if the data is malformed
     */
    public static void parseParameters( final Map<String, String[]> map, final String data, final String encoding )
        throws UnsupportedEncodingException
    {

        if ( ( data != null ) && ( data.length() > 0 ) )
        {

            // use the specified encoding to extract bytes out of the
            // given string so that the encoding is not lost. If an
            // encoding is not specified, let it use platform default
            byte[] bytes = null;
            try
            {
                if ( encoding == null )
                {
                    bytes = data.getBytes();
                }
                else
                {
                    bytes = data.getBytes( encoding );
                }
            }
            catch ( final UnsupportedEncodingException uee )
            {
            }

            parseParameters( map, bytes, encoding );
        }

    }

    /**
     * Decode and return the specified URL-encoded String. When the byte array
     * is converted to a string, the system default character encoding is
     * used... This may be different than some other servers.
     * 
     * @param str
     *      The url-encoded string
     * 
     * @exception IllegalArgumentException
     *      if a '%' character is not followed by a valid 2-digit hexadecimal
     *      number
     */
    public static String URLDecode( final String str )
    {

        return URLDecode( str, null );

    }

    /**
     * Decode and return the specified URL-encoded String.
     * 
     * @param str
     *      The url-encoded string
     * @param enc
     *      The encoding to use; if null, the default encoding is used
     * @exception IllegalArgumentException
     *      if a '%' character is not followed by a valid 2-digit hexadecimal
     *      number
     */
    public static String URLDecode( final String str, final String enc )
    {

        if ( str == null )
        {
            return ( null );
        }

        // use the specified encoding to extract bytes out of the
        // given string so that the encoding is not lost. If an
        // encoding is not specified, let it use platform default
        byte[] bytes = null;
        try
        {
            if ( enc == null )
            {
                bytes = str.getBytes();
            }
            else
            {
                bytes = str.getBytes( enc );
            }
        }
        catch ( final UnsupportedEncodingException uee )
        {
        }

        return URLDecode( bytes, enc );

    }

    /**
     * Decode and return the specified URL-encoded byte array.
     * 
     * @param bytes
     *      The url-encoded byte array
     * @exception IllegalArgumentException
     *      if a '%' character is not followed by a valid 2-digit hexadecimal
     *      number
     */
    public static String URLDecode( final byte[] bytes )
    {
        return URLDecode( bytes, null );
    }

    /**
     * Decode and return the specified URL-encoded byte array.
     * 
     * @param bytes
     *      The url-encoded byte array
     * @param enc
     *      The encoding to use; if null, the default encoding is used
     * @exception IllegalArgumentException
     *      if a '%' character is not followed by a valid 2-digit hexadecimal
     *      number
     */
    public static String URLDecode( final byte[] bytes, final String enc )
    {

        if ( bytes == null )
        {
            return ( null );
        }

        final int len = bytes.length;
        int ix = 0;
        int ox = 0;
        while ( ix < len )
        {
            byte b = bytes[ix++]; // Get byte to test
            if ( b == '+' )
            {
                b = (byte) ' ';
            }
            else if ( b == '%' )
            {
                b = (byte) ( ( convertHexDigit( bytes[ix++] ) << 4 ) + convertHexDigit( bytes[ix++] ) );
            }
            bytes[ox++] = b;
        }
        if ( enc != null )
        {
            try
            {
                return new String( bytes, 0, ox, enc );
            }
            catch ( final Exception e )
            {
                // FIXME
                e.printStackTrace();
            }
        }
        return new String( bytes, 0, ox );

    }

    /**
     * Convert a byte character value to hexidecimal digit value.
     * 
     * @param b
     *      the character value byte
     */
    private static byte convertHexDigit( final byte b )
    {
        if ( ( b >= '0' ) && ( b <= '9' ) )
        {
            return (byte) ( b - '0' );
        }
        if ( ( b >= 'a' ) && ( b <= 'f' ) )
        {
            return (byte) ( b - 'a' + 10 );
        }
        if ( ( b >= 'A' ) && ( b <= 'F' ) )
        {
            return (byte) ( b - 'A' + 10 );
        }
        return 0;
    }

    /**
     * Put name and value pair in map. When name already exist, add value to
     * array of values.
     * 
     * @param map
     *      The map to populate
     * @param name
     *      The parameter name
     * @param value
     *      The parameter value
     */
    private static void putMapEntry( final Map<String, String[]> map, final String name, final String value )
    {
        String[] newValues = null;
        final String[] oldValues = map.get( name );
        if ( oldValues == null )
        {
            newValues = new String[1];
            newValues[0] = value;
        }
        else
        {
            newValues = new String[oldValues.length + 1];
            System.arraycopy( oldValues, 0, newValues, 0, oldValues.length );
            newValues[oldValues.length] = value;
        }
        map.put( name, newValues );
    }

    /**
     * Append request parameters from the specified String to the specified Map.
     * It is presumed that the specified Map is not accessed from any other
     * thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: URL decoding is performed
     * individually on the parsed name and value elements, rather than on the
     * entire query string ahead of time, to properly deal with the case where
     * the name or value includes an encoded "=" or "&" character that would
     * otherwise be interpreted as a delimiter. NOTE: byte array data is
     * modified by this method. Caller beware.
     * 
     * @param map
     *      Map that accumulates the resulting parameters
     * @param data
     *      Input string containing request parameters
     * @param encoding
     *      Encoding to use for converting hex
     * 
     * @exception UnsupportedEncodingException
     *      if the data is malformed
     */
    public static void parseParameters( final Map<String, String[]> map, final byte[] data, final String encoding )
        throws UnsupportedEncodingException
    {

        if ( data != null && data.length > 0 )
        {
            int ix = 0;
            int ox = 0;
            String key = null;
            String value = null;
            while ( ix < data.length )
            {
                final byte c = data[ix++];
                switch ( (char) c )
                {
                    case '&':
                        value = new String( data, 0, ox, encoding );
                        if ( key != null )
                        {
                            putMapEntry( map, key, value );
                            key = null;
                        }
                        ox = 0;
                        break;
                    case '=':
                        if ( key == null )
                        {
                            key = new String( data, 0, ox, encoding );
                            ox = 0;
                        }
                        else
                        {
                            data[ox++] = c;
                        }
                        break;
                    case '+':
                        data[ox++] = (byte) ' ';
                        break;
                    case '%':
                        data[ox++] = (byte) ( ( convertHexDigit( data[ix++] ) << 4 ) + convertHexDigit( data[ix++] ) );
                        break;
                    default:
                        data[ox++] = c;
                }
            }
            // The last value does not end in '&'. So save it now.
            if ( key != null )
            {
                value = new String( data, 0, ox, encoding );
                putMapEntry( map, key, value );
            }
        }

    }

}
