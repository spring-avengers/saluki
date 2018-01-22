package com.quancheng.saluki.zuul.filter;

import com.quancheng.saluki.zuul.DynamicCodeCompiler;
import com.quancheng.saluki.zuul.groovy.GroovyCompiler;
import com.quancheng.saluki.zuul.util.Announcer;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author HWEB
 */
public class ZuulFiltersLoader implements FiltersChangeNotifier {

    private final Announcer<FiltersListener> announcer = Announcer.to(FiltersListener.class);

    public static final DynamicCodeCompiler COMPILER = new GroovyCompiler();
    private final Path filtersRootPath;

    public ZuulFiltersLoader(Path filtersRootPath) {
        this.filtersRootPath = filtersRootPath;
    }

    private void registerPreFilters() {
        if (filtersRootPath.toFile().exists()) {
            for (File src : readZuulFiltersFrom(filtersRootPath)) {
                filterAdded(src.toPath());
            }
        }
    }

    private static List<File> readZuulFiltersFrom(Path path) {
        List<File> files = newArrayList();
        for (File file : path.toFile().listFiles()) {
            if (file.getName().endsWith(".groovy")) {
                files.add(file);
            }
            if (file.isDirectory()) {
                files.addAll(readZuulFiltersFrom(file.toPath()));
            }
        }
        return files;
    }

    private ZuulFilter parseFilter(File file) throws Exception {
        Class clazz = COMPILER.compile(file);
        if (!Modifier.isAbstract(clazz.getModifiers())) {
            ZuulFilter zuulFilter = (ZuulFilter) clazz.newInstance();
            return zuulFilter;
        }

        return null;
    }


    public void filterAdded(Path filterPah) {
        updateFilters(filterPah);
    }

    private void updateFilters(Path filterPah) {
        try {
            announcer.announce().filterAdded(filterPah, parseFilter(filterPah.toFile()));
        } catch (Exception e) {

        }
    }

    @Override
    public void addFiltersListener(FiltersListener filtersListener) {
        announcer.addListener(filtersListener);
    }

    public void reload() {
        registerPreFilters();
    }
}
