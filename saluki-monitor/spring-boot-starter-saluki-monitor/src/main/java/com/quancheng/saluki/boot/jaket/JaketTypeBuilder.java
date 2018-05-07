package io.github.saluki.boot.jaket;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.saluki.boot.jaket.builder.ArrayTypeBuilder;
import io.github.saluki.boot.jaket.builder.CollectionTypeBuilder;
import io.github.saluki.boot.jaket.builder.DefaultTypeBuilder;
import io.github.saluki.boot.jaket.builder.EnumTypeBuilder;
import io.github.saluki.boot.jaket.builder.MapTypeBuilder;
import io.github.saluki.boot.jaket.builder.TypeBuilder;
import io.github.saluki.boot.jaket.model.TypeDefinition;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public class JaketTypeBuilder {

    private static final List<TypeBuilder> builders = new ArrayList<TypeBuilder>();

    static {
        builders.add(new ArrayTypeBuilder());
        builders.add(new CollectionTypeBuilder());
        builders.add(new MapTypeBuilder());
        builders.add(new EnumTypeBuilder());
    }

    public static TypeDefinition build(Type type, Class<?> clazz, Map<Class<?>, TypeDefinition> typeCache) {
        TypeBuilder builder = getGenericTypeBuilder(type, clazz);
        TypeDefinition td = null;
        if (builder != null) {
            td = builder.build(type, clazz, typeCache);
        } else {
            td = DefaultTypeBuilder.build(clazz, typeCache);
        }
        return td;
    }

    private static TypeBuilder getGenericTypeBuilder(Type type, Class<?> clazz) {
        for (TypeBuilder builder : builders) {
            if (builder.accept(type, clazz)) {
                return builder;
            }
        }
        return null;
    }

    private Map<Class<?>, TypeDefinition> typeCache = new HashMap<Class<?>, TypeDefinition>();

    public TypeDefinition build(Type type, Class<?> clazz) {
        return build(type, clazz, typeCache);
    }

    public List<TypeDefinition> getTypeDefinitions() {
        return new ArrayList<TypeDefinition>(typeCache.values());
    }

}
