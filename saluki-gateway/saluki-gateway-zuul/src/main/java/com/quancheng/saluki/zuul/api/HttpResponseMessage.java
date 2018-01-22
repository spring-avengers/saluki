package com.quancheng.saluki.zuul.api;

import java.util.List;
import java.util.Map;


public interface HttpResponseMessage {

    /**
     * Returns the header value with the specified header name.  If there are
     * more than one header value for the specified header name, the first
     * value is returned.
     *
     * @return the header value or {@code null} if there is no such header
     */
    String getHeader(String name);
    
    /**
     * Returns {@code true} if and only if there is a header with the specified
     * header name.
     */
    boolean containsHeader(String name);
    
    /**
     * Adds a new header with the specified name and value.
     */
    void addHeader(String name, Object value);
    
    /**
     * Returns the all header names and values that this message contains.
     *
     * @return the {@link List} of the header name-value pairs.  An empty list
     *         if there is no header in this message.
     */
    List<Map.Entry<String, String>> getHeaders();
    
}
