package io.github.saluki.monitor.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import io.github.saluki.monitor.dao.domain.GrpcInvoke;

@Mapper
public interface InvokeMapper {

    int addInvoke(GrpcInvoke invoke);

    int truncateTable();

    List<GrpcInvoke> queryData();

}
