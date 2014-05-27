package net.sf.webdav;

/**
 * Wraps the HttpServletResponse class to abstract the specific protocol used.
 * To support other protocols we would only need to modify this class and the
 * WebDavRetCode classes.
 * 
 * @author Marc Eaddy
 * @version 1.0, 16 Nov 1997
 */
public enum WebdavStatus
{

    /**
     * Status code (200) indicating the request succeeded normally.
     */
    SC_OK( 200, "OK" ),

    /**
     * Status code (201) indicating the request succeeded and created a new
     * resource on the server.
     */
    SC_CREATED( 201, "Created" ),

    /**
     * Status code (202) indicating that a request was accepted for processing,
     * but was not completed.
     */
    SC_ACCEPTED( 202, "Accepted" ),

    /**
     * Status code (204) indicating that the request succeeded but that there
     * was no new information to return.
     */
    SC_NO_CONTENT( 204, "No Content" ),

    /**
     * Status code (301) indicating that the resource has permanently moved to a
     * new location, and that future references should use a new URI with their
     * requests.
     */
    SC_MOVED_PERMANENTLY( 301, "Moved Permanently" ),

    /**
     * Status code (302) indicating that the resource has temporarily moved to
     * another location, but that future references should still use the
     * original URI to access the resource.
     */
    SC_MOVED_TEMPORARILY( 302, "Found" ),

    /**
     * Status code (304) indicating that a conditional GET operation found that
     * the resource was available and not modified.
     */
    SC_NOT_MODIFIED( 304, "Not Modified" ),

    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
    SC_BAD_REQUEST( 400, "Bad Request" ),

    /**
     * Status code (401) indicating that the request requires HTTP
     * authentication.
     */
    SC_UNAUTHORIZED( 401, "Unauthorized" ),

    /**
     * Status code (403) indicating the server understood the request but
     * refused to fulfill it.
     */
    SC_FORBIDDEN( 403, "Forbidden" ),

    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */
    SC_NOT_FOUND( 404, "Not Found" ),

    /**
     * Status code (500) indicating an error inside the HTTP service which
     * prevented it from fulfilling the request.
     */
    SC_INTERNAL_SERVER_ERROR( 500, "Internal Server Error" ),

    /**
     * Status code (501) indicating the HTTP service does not support the
     * functionality needed to fulfill the request.
     */
    SC_NOT_IMPLEMENTED( 501, "Not Implemented" ),

    /**
     * Status code (502) indicating that the HTTP server received an invalid
     * response from a server it consulted when acting as a proxy or gateway.
     */
    SC_BAD_GATEWAY( 502, "Bad Gateway" ),

    /**
     * Status code (503) indicating that the HTTP service is temporarily
     * overloaded, and unable to handle the request.
     */
    SC_SERVICE_UNAVAILABLE( 503, "Service Unavailable" ),

    /**
     * Status code (100) indicating the client may continue with its request.
     * This interim response is used to inform the client that the initial part
     * of the request has been received and has not yet been rejected by the
     * server.
     */
    SC_CONTINUE( 100, "Continue" ),

    /**
     * Status code (405) indicating the method specified is not allowed for the
     * resource.
     */
    SC_METHOD_NOT_ALLOWED( 405, "Method Not Allowed" ),

    /**
     * Status code (409) indicating that the request could not be completed due
     * to a conflict with the current state of the resource.
     */
    SC_CONFLICT( 409, "Conflict" ),

    /**
     * Status code (412) indicating the precondition given in one or more of the
     * request-header fields evaluated to false when it was tested on the
     * server.
     */
    SC_PRECONDITION_FAILED( 412, "Precondition Failed" ),

    /**
     * Status code (413) indicating the server is refusing to process a request
     * because the request entity is larger than the server is willing or able
     * to process.
     */
    SC_REQUEST_TOO_LONG( 413, "Request Too Long" ),

    /**
     * Status code (415) indicating the server is refusing to service the
     * request because the entity of the request is in a format not supported by
     * the requested resource for the requested method.
     */
    SC_UNSUPPORTED_MEDIA_TYPE( 415, "Unsupported Media Type" ),

    // -------------------------------------------- Extended WebDav status code

    /**
     * Status code (207) indicating that the response requires providing status
     * for multiple independent operations.
     */
    SC_MULTI_STATUS( 207, "Multi" ),

    // This one colides with HTTP 1.1
    // "207 Parital Update OK"

    /**
     * Status code (418) indicating the entity body submitted with the PATCH
     * method was not understood by the resource.
     */
    SC_UNPROCESSABLE_ENTITY( 418, "Unprocessable Entity" ),

    // This one colides with HTTP 1.1
    // "418 Reauthentication Required"

    /**
     * Status code (419) indicating that the resource does not have sufficient
     * space to record the state of the resource after the execution of this
     * method.
     */
    SC_INSUFFICIENT_SPACE_ON_RESOURCE( 419, "Insufficient Space on Resource" ),

    // This one colides with HTTP 1.1
    // "419 Proxy Reauthentication Required"

    /**
     * Status code (420) indicating the method was not executed on a particular
     * resource within its scope because some part of the method's execution
     * failed causing the entire method to be aborted.
     */
    SC_METHOD_FAILURE( 420, "Method Failure" ),

    /**
     * Status code (423) indicating the destination resource of a method is
     * locked, and either the request did not contain a valid Lock-Info header,
     * or the Lock-Info header identifies a lock held by another principal.
     */
    SC_LOCKED( 423, "Locked" );

    // --------------------------------------------------------- Public Methods

    /**
     * Returns the HTTP status text for the HTTP or WebDav status code specified
     * by looking it up in the static mapping. This is a static function.
     * 
     * @param nHttpStatusCode
     *      [IN] HTTP or WebDAV status code
     * @return A string with a short descriptive phrase for the HTTP status code
     *  (e.g., "OK").
     */
    public static String getStatusText( final int nHttpStatusCode )
    {
        for ( final WebdavStatus stat : values() )
        {
            if ( stat.code == nHttpStatusCode )
            {
                return stat.message;
            }
        }

        return "";
    }

    private int code;

    private String message;

    private WebdavStatus( final int code, final String message )
    {
        this.code = code;
        this.message = message;
    }

    public int code()
    {
        return code;
    }

    public String message()
    {
        return message;
    }

 
    public String toString()
    {
        return ""+ code;
    }

    
    public static WebdavStatus get( final int status )
    {
        for ( final WebdavStatus stat : values() )
        {
            if ( stat.code == status )
            {
                return stat;
            }
        }

        return null;
    }

};
