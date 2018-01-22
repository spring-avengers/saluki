package com.quancheng.saluki.zuul.filter;

import java.nio.file.Path;
import java.util.EventListener;

/**
 * @author HWEB
 */
public interface FiltersListener extends EventListener {

    /**
     * @param filterPath
     * @param filter
     */
    void filterAdded(Path filterPath, ZuulFilter filter);


    /**
     *
     * @param filterPath
     * @param filter
     */
    void filterRemoved(Path filterPath, ZuulFilter filter);

}
