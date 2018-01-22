package com.quancheng.saluki.zuul.api;

public interface Interrupts {

    void movedPermanently (String location);
    void temporaryRedirect (String location);
    
}
