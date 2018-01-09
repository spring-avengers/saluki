var prefix = "/sys/log"
$(function() {
	load();

});
$('#exampleTable').on(
		'load-success.bs.table',
		function(e, data) {
			if (data.total && !data.rows.length) {
				$('#exampleTable').bootstrapTable('selectPage').bootstrapTable(
						'refresh');
			}
		});

function load() {
	$('#exampleTable')
			.bootstrapTable(
					{
						method : 'get', // 服务器数据的请求方式 get or post
						url : prefix + "/list", // 服务器数据的加载地址
						iconSize : 'outline',
						toolbar : '#exampleToolbar',
						striped : true, // 设置为true会有隔行变色效果
						dataType : "json", // 服务器返回的数据类型
						pagination : true, // 设置为true会在底部显示分页条
						singleSelect : false, // 设置为true将禁止多选
						pageSize : 10, // 如果设置了分页，每页数据条数
						pageNumber : 1, // 如果设置了分布，首页页码
						sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者
						queryParams : function(params) {
							return {
								limit : params.limit,
								offset : params.offset,
								name : $('#searchName').val(),
								sort : 'gmt_create',
								order : 'desc',
								operation : $("#searchOperation").val(),
								username : $("#searchUsername").val()
							};
						},
						columns : [
								{
									checkbox : true
								},
								{
									field : 'id', // 列字段名
									title : '序号' // 列标题
								},
								{
									field : 'userId',
									title : '用户Id'
								},
								{
									field : 'username',
									title : '用户名'
								},
								{
									field : 'operation',
									title : '操作'
								},
								{
									field : 'time',
									title : '用时'
								},
								{
									field : 'method',
									title : '方法'
								},
								{
									field : 'ip',
									title : 'IP地址'
								},
								{
									field : 'gmtCreate',
									title : '创建时间'
								},
								{
									title : '操作',
									field : 'id',
									align : 'center',
									formatter : function(value, row, index) {
										var d = '<a class="btn btn-warning btn-sm" href="#" title="删除"  mce_href="#" onclick="remove(\''
												+ row.id
												+ '\')"><i class="fa fa-remove"></i></a> ';
										return d;
									}
								} ]
					});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
function remove(id) {
	layer.confirm('确定要删除选中的记录？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix + "/remove",
			type : "post",
			data : {
				'id' : id
			},
			beforeSend : function(request) {
				index = layer.load();
			},
			success : function(r) {
				if (r.code == 0) {
					layer.close(index);
					layer.msg(r.msg);
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	})
}
function batchRemove() {
	var rows = $('#exampleTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要删除的数据");
		return;
	}
	layer.confirm("确认要删除选中的'" + rows.length + "'条数据吗?", {
		btn : [ '确定', '取消' ]
	// 按钮
	}, function() {
		var ids = new Array();
		// 遍历所有选择的行数据，取每条数据对应的ID
		$.each(rows, function(i, row) {
			ids[i] = row['id'];
		});
		$.ajax({
			type : 'POST',
			data : {
				"ids" : ids
			},
			url : prefix + '/batchRemove',
			success : function(r) {
				if (r.code == 0) {
					layer.msg(r.msg);
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	}, function() {
	});
}