var prefix = "/filter/route";
$(function() {
	load();
});

function load() {
	$('#routeTable')
			.bootstrapTable(
					{
						method : 'get',
						url : prefix + "/list",
						iconSize : 'outline',
						toolbar : '#exampleToolbar',
						expandColumn : '3',
						striped : true,
						dataType : "json",
						pagination : true,
						singleSelect : false,
						pageSize : 10,
						pageNumber : 1,
						sidePagination : "server",
						detailView : true,
						columns : [
								{
									checkbox : true
								},
								{
									field : 'routeId',
									title : '序号'
								},
								{
									field : 'fromPath',
									title : '源路径'
								},
								{
									field : 'fromPathpattern',
									title : '源路径匹配'
								},
								{
									field : 'toHostport',
									title : '目标地址'
								},
								{
									field : 'toPath',
									title : '目标路径'
								},
								{
									field : 'serviceId',
									title : '服务ID'
								},
								{
									title : '操作',
									field : 'routeId',
									align : 'center',
									formatter : function(value, row, index) {
										var e = '<a class="btn btn-primary btn-sm '
												+ s_edit_h
												+ '" href="#" mce_href="#" title="编辑" onclick="edit(\''
												+ row.routeId
												+ '\')"><i class="fa fa-edit"></i></a> ';
										var d = '<a class="btn btn-warning btn-sm" href="#" title="删除"  mce_href="#" onclick="remove(\''
												+ row.routeId
												+ '\')"><i class="fa fa-remove"></i></a> ';
										return e + d;
									}
								} ],
						onExpandRow : function(index, row, $detail) {
							if (row.rpc) {
								chirdTable(index, row, $detail);
							}
						}
					});
}
function reLoad() {
	$('#zuulTable').bootstrapTable('refresh');
}
function chirdTable(index, row, $detail) {
	var cur_table = $detail.html('<table></table>').find('table');
	var rows = [];
	rows.push(row);
	$(cur_table).bootstrapTable({
		columns : [ {
			field : 'rpc',
			title : 'Rpc服务',
			formatter : function(value, row, index) {
				if (value) {
					return "是";
				} else {
					return "否"
				}
			}
		}, {
			field : 'serviceName',
			title : '接口名'
		}, {
			field : 'methodName',
			title : '方法名'
		}, {
			field : 'serviceGroup',
			title : '组别'
		}, {
			field : 'serviceVersion',
			title : '版本'
		} ],
		data : rows
	});
}
function add() {
	layer.open({
		type : 2,
		title : '添加路由',
		maxmin : true,
		shadeClose : true,
		area : [ '1300px', '700px' ],
		content : prefix + '/add'
	});
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
			success : function(r) {
				if (r.code === 0) {
					layer.msg("删除成功");
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	})

}
function edit(id) {
	layer.open({
		type : 2,
		title : '路由修改',
		maxmin : true,
		shadeClose : true,
		area : [ '1300px', '700px' ],
		content : prefix + '/edit/' + id
	});
}
function batchRemove() {
	var rows = $('#routeTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要删除的数据");
		return;
	}
	layer.confirm("确认要删除选中的'" + rows.length + "'条数据吗?", {
		btn : [ '确定', '取消' ]
	}, function() {
		var ids = new Array();
		$.each(rows, function(i, row) {
			ids[i] = row['roleId'];
		});
		console.log(ids);
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