package io.github.saluki.monitor.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import io.github.saluki.domain.GrpcInvoke;
import io.github.saluki.domain.GrpcnvokeStatistics;

@Mapper
public interface InvokeMapper {

    void addInvoke(List<GrpcInvoke> invokes);

    List<GrpcnvokeStatistics> queryData(Map<String, String> paramter);

    List<Map<String, String>> queryConsumer();

    List<Map<String, String>> queryProvider(Map<String, String> paramter);

}
