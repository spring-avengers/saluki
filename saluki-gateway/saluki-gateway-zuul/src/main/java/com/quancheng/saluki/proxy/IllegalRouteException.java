package com.quancheng.saluki.proxy;

/**
 * Created with IntelliJ IDEA.
 * User: nbeveridge
 * Date: 12/09/2013
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
public class IllegalRouteException extends Exception {
    private final String route;

    public IllegalRouteException(String route) {
        super();    //To change body of overridden methods use File | Settings | File Templates.
        this.route = route;
    }

    public IllegalRouteException(String route, String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
        this.route = route;
    }

    public IllegalRouteException(String route, String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
        this.route = route;
    }

    public IllegalRouteException(String route, Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
        this.route = route;
    }

    public String getRoute() {
        return route;
    }
}
