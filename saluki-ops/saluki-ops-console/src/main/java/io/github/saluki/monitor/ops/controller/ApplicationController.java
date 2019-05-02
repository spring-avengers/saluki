package io.github.saluki.monitor.ops.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.github.saluki.monitor.dao.domain.Application;
import io.github.saluki.monitor.dao.domain.ApplicationDependcy;
import io.github.saluki.monitor.ops.service.ApplicationDependcyService;
import io.github.saluki.monitor.ops.service.ConsulRegistryService;

@RestController
@RequestMapping(value = "/api/application")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private ConsulRegistryService registrySerivce;

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
