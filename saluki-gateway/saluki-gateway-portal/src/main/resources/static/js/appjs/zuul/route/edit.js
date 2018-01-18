$().ready(function() {
	validateRule();
});

$.validator.setDefaults({
	submitHandler : function() {
		update();
	}
});
function update() {
	$("#routeForm").ajaxSubmit({
		type : "POST",
		url : "/zuul/route/update",
		dataType : 'json',
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			if (data.code == 0) {
				parent.layer.msg("操作成功");
				parent.reLoad();
				var index = parent.layer.getFrameIndex(window.name);
				parent.layer.close(index);

			} else {
				parent.layer.alert(data.msg)
			}

		}
	});
}
function validateRule() {
	var icon = "<i class='fa fa-times-circle'></i> ";
	$("#routeForm").validate({
		rules : {
			path : {
				required : true
			},
			serviceName : {
				required : {
					depends : function(value, element) {
						var isGrpc = $('#grpc').val();
						var isDubbo = $('#dubbo').val();
						return isGrpc == 1 || isDubbo == 1;
					}
				}
			},
			methodName : {
				required : {
					depends : function(value, element) {
						var isGrpc = $('#grpc').val();
						var isDubbo = $('#dubbo').val();
						return isGrpc == 1 || isDubbo == 1;
					}
				}
			},
			serviceGroup : {
				required : {
					depends : function(value, element) {
						var isGrpc = $('#grpc').val();
						var isDubbo = $('#dubbo').val();
						return isGrpc == 1 || isDubbo == 1;
					}
				}
			},
			serviceVersion : {
				required : {
					depends : function(value, element) {
						var isGrpc = $('#grpc').val();
						var isDubbo = $('#dubbo').val();
						return isGrpc == 1 || isDubbo == 1;
					}
				}
			},
			zipFile : {
				required : {
					depends : function(value, element) {
						var isGrpc = $('#grpc').val();
						return isGrpc == 1;
					}
				}
			},
			serviceFileName : {
				required : {
					depends : function(value, element) {
						var isGrpc = $('#grpc').val();
						var isDubbo = $('#dubbo').val();
						if (isGrpc == 1) {
							return $('#zipFile').val() != null;
						} else {
							return false;
						}
					}
				}
			}
		},
		messages : {
			path : {
				required : icon + "请输入路由路径！"
			},
			serviceName : {
				required : icon + "请输入服务名！"
			},
			methodName : {
				required : icon + "请输入方法名！"
			},
			serviceGroup : {
				required : icon + "请输入组别！"
			},
			serviceVersion : {
				required : icon + "请输入版本！"
			},
			zipFile : {
				required : icon + "请上传proto目录文件！"
			},
			serviceFileName : {
				required : icon + "上传proto目录文件，需要指定目录中的服务定义文件名！"
			}
		}
	})
}