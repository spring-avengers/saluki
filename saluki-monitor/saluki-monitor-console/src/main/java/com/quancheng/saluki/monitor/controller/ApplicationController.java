package io.github.saluki.monitor.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.github.saluki.domain.Application;
import io.github.saluki.domain.ApplicationDependcy;
import io.github.saluki.monitor.service.ApplicationDependcyService;
import io.github.saluki.monitor.service.ConsulRegistryService;

@RestController
@RequestMapping(value = "/api/application")
public class ApplicationController {

    private static final Logger        log = Logger.getLogger(ApplicationController.class);

    @Autowired
    private ConsulRegistryService      registrySerivce;

    @Autowired
    private ApplicationDependcyService appDependcyService;

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public List<Application> listAllApps() {
        log.info("Return all application from registry");
        return registrySerivce.getAllApplication();
    }

    @RequestMapping(value = "dependcy", method = RequestMethod.GET)
    public List<ApplicationDependcy> listDependcyApps() {
        return appDependcyService.queryApplicationDependcy();
    }

}
