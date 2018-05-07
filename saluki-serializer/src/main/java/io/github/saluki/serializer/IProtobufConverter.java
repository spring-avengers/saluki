package io.github.saluki.serializer;

import io.github.saluki.serializer.exception.ProtobufAnnotationException;

public interface IProtobufConverter {

    Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException;

    Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException;
}
