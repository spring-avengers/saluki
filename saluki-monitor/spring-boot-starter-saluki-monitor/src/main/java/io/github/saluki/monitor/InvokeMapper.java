package io.github.saluki.monitor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import io.github.saluki.monitor.domain.GrpcInvoke;

@Mapper
public interface InvokeMapper {

    int addInvoke(GrpcInvoke invoke);

    int truncateTable();

    List<GrpcInvoke> queryData();

}
