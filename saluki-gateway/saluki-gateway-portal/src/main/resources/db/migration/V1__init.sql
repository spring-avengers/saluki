SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `sys_dept`
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上级部门ID，一级部门为0',
  `name` varchar(50) DEFAULT NULL COMMENT '部门名称',
  `order_num` int(11) DEFAULT NULL COMMENT '排序',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '是否删除  -1：已删除  0：正常',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8 COMMENT='部门管理';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES ('6', '0', '基础架构', '1', '1');
INSERT INTO `sys_dept` VALUES ('9', '0', '业务研发', '2', '1');

-- ----------------------------
-- Table structure for `sys_log`
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `operation` varchar(50) DEFAULT NULL COMMENT '用户操作',
  `time` int(11) DEFAULT NULL COMMENT '响应时间',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `params` varchar(5000) DEFAULT NULL COMMENT '请求参数',
  `ip` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7956 DEFAULT CHARSET=utf8 COMMENT='系统日志';

-- ----------------------------
-- Records of sys_log
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_menu`
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父菜单ID，一级菜单为0',
  `name` varchar(50) DEFAULT NULL COMMENT '菜单名称',
  `url` varchar(200) DEFAULT NULL COMMENT '菜单URL',
  `perms` varchar(500) DEFAULT NULL COMMENT '授权(多个用逗号分隔，如：user:list,user:create)',
  `type` int(11) DEFAULT NULL COMMENT '类型   0：目录   1：菜单   2：按钮',
  `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
  `order_num` int(11) DEFAULT NULL COMMENT '排序',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8 COMMENT='菜单管理';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('1', '0', '系统管理', null, null, '0', 'fa fa-desktop', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('2', '0', '系统监控', null, null, '0', 'fa fa-video-camera', '5', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('3', '0', '网关管理', null, null, '0', 'fa fa-bar-chart', '7', '2017-08-09 23:06:55', '2017-08-14 14:13:43');


-- ----------------------------
-- 系统管理
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('4', '1', '系统菜单', 'sys/menu', 'sys:menu:menu', '1', 'fa fa-th-list', '0', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('5', '1', '用户管理', 'sys/user', 'sys:user:user', '1', 'fa fa-user', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('6', '1', '角色管理', 'sys/role', 'sys:role:role', '1', 'fa fa-paw', '2', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('7', '1', '部门管理', 'sys/dept', 'sys:dept:dept', '1', 'fa fa-users', '3', '2017-08-09 23:06:55', '2017-08-14 14:13:43');


INSERT INTO `sys_menu` VALUES ('8', '4', '新增', '', 'sys:menu:add', '2', '', '0', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('9', '4', '批量删除', '', 'sys:menu:batchRemove', '2','', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('10', '4', '编辑', '', 'sys:menu:edit', '2', '', '2', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('11', '4', '删除', '', 'sys:menu:remove', '2', '', '3', '2017-08-09 23:06:55', '2017-08-14 14:13:43');



INSERT INTO `sys_menu` VALUES ('12', '6', '新增', '', 'sys:user:add', '2', '', '0', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('13', '6', '编辑', '', 'sys:user:edit', '2', '', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('14', '6', '删除', '', 'sys:user:remove','2', '', '2', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('15', '6', '批量删除', '', 'sys:user:batchRemove', '2', '', '3', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('16', '6', '停用', '', 'sys:user:disable', '2', '', '4', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('17', '6', '重置密码', '', 'sys:user:resetPwd', '2', '', '5', '2017-08-09 23:06:55', '2017-08-14 14:13:43');

INSERT INTO `sys_menu` VALUES ('18', '7', '新增', '', 'sys:role:add', '2', '', '0', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('19', '7', '批量删除', '', 'sys:role:batchRemove', '2','', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('20', '7', '编辑', '', 'sys:role:edit', '2', '', '2', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('21', '7', '删除', '', 'sys:role:remove', '2', '', '3', '2017-08-09 23:06:55', '2017-08-14 14:13:43');


INSERT INTO `sys_menu` VALUES ('22', '8', '增加', '', 'sys:dept:add', '2', '', '0', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('23', '8', '刪除', '', 'sys:dept:remove', '2', '', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('24', '8', '编辑', '', 'sys:dept:edit', '2', '', '2', '2017-08-09 23:06:55', '2017-08-14 14:13:43');


-- ----------------------------
-- 系统监控
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('25', '2', '在线用户', 'sys/online', 'sys:monitor:online', '1', 'fa fa-user', '0', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('26', '2', '系统日志', 'sys/log', 'sys:monitor:log', '1', 'fa fa-warning', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('27', '2', '运行监控', 'sys/log/run', 'sys:monitor:run', '1', 'fa fa-caret-square-o-right', '2', '2017-08-09 23:06:55', '2017-08-14 14:13:43');


-- ----------------------------
-- zuul管理
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('28', '3', '路由管理', '/filter/route', 'filter:route:route', '1', 'fa fa-area-chart', '0', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('29', '3', '策略管理', '/filter/route', 'filter:route:route', '1', 'fa fa-warning', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('30', '3', '动态过滤', '/filter/route', 'filter:route:route', '1', 'fa fa-caret-square-o-right', '2', '2017-08-09 23:06:55', '2017-08-14 14:13:43');

INSERT INTO `sys_menu` VALUES ('31', '28', '新增', '', 'filter:route:add', '2', '', '0', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('32', '28', '批量删除', '', 'filter:route:batchRemove', '2','', '1', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('33', '28', '编辑', '', 'filter:route:edit', '2', '', '2', '2017-08-09 23:06:55', '2017-08-14 14:13:43');
INSERT INTO `sys_menu` VALUES ('34', '28', '删除', '', 'filter:route:remove', '2', '', '3', '2017-08-09 23:06:55', '2017-08-14 14:13:43');


 
-- ----------------------------
-- Table structure for `sys_role`
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(100) DEFAULT NULL COMMENT '角色名称',
  `role_sign` varchar(100) DEFAULT NULL COMMENT '角色标识',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `user_id_create` bigint(255) DEFAULT NULL COMMENT '创建用户id',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8 COMMENT='角色';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES ('1', '超级用户角色', 'admin', '拥有最高权限', '2', '2017-08-12 00:43:52', '2017-08-12 19:14:59');
INSERT INTO `sys_role` VALUES ('2', '普通用户', 'user', '普通用户',  '2', '2017-08-12 00:43:52', '2017-08-12 19:14:59');
-- ----------------------------
-- Table structure for `sys_role_menu`
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `menu_id` bigint(20) DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2974 DEFAULT CHARSET=utf8 COMMENT='角色与菜单对应关系';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES ('1', '1', '1');
INSERT INTO `sys_role_menu` VALUES ('2', '1', '2');
INSERT INTO `sys_role_menu` VALUES ('3', '1', '3');
INSERT INTO `sys_role_menu` VALUES ('4', '1', '4');
INSERT INTO `sys_role_menu` VALUES ('5', '1', '5');
INSERT INTO `sys_role_menu` VALUES ('6', '1', '6');
INSERT INTO `sys_role_menu` VALUES ('7', '1', '7');
INSERT INTO `sys_role_menu` VALUES ('8', '1', '8');
INSERT INTO `sys_role_menu` VALUES ('9', '1', '9');
INSERT INTO `sys_role_menu` VALUES ('10', '1', '10');
INSERT INTO `sys_role_menu` VALUES ('11', '1', '11');
INSERT INTO `sys_role_menu` VALUES ('12', '1', '12');
INSERT INTO `sys_role_menu` VALUES ('13', '1', '13');
INSERT INTO `sys_role_menu` VALUES ('14', '1', '14');
INSERT INTO `sys_role_menu` VALUES ('15', '1', '15');
INSERT INTO `sys_role_menu` VALUES ('16', '1', '16');
INSERT INTO `sys_role_menu` VALUES ('17', '1', '17');
INSERT INTO `sys_role_menu` VALUES ('18', '1', '18');
INSERT INTO `sys_role_menu` VALUES ('19', '1', '19');
INSERT INTO `sys_role_menu` VALUES ('20', '1', '20');
INSERT INTO `sys_role_menu` VALUES ('21', '1', '21');
INSERT INTO `sys_role_menu` VALUES ('22', '1', '22');
INSERT INTO `sys_role_menu` VALUES ('23', '1', '23');
INSERT INTO `sys_role_menu` VALUES ('24', '1', '24');
INSERT INTO `sys_role_menu` VALUES ('25', '1', '25');
INSERT INTO `sys_role_menu` VALUES ('26', '1', '26');
INSERT INTO `sys_role_menu` VALUES ('27', '1', '27');
INSERT INTO `sys_role_menu` VALUES ('28', '1', '28');
INSERT INTO `sys_role_menu` VALUES ('29', '1', '29');
INSERT INTO `sys_role_menu` VALUES ('30', '1', '30');
INSERT INTO `sys_role_menu` VALUES ('31', '1', '31');
INSERT INTO `sys_role_menu` VALUES ('32', '1', '32');
INSERT INTO `sys_role_menu` VALUES ('33', '1', '33');
INSERT INTO `sys_role_menu` VALUES ('34', '1', '34');

-- ----------------------------
-- Table structure for `sys_user`
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `name` varchar(100) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL COMMENT '密码',
  `dept_id` int(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(100) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(255) DEFAULT NULL COMMENT '状态 0:禁用，1:正常',
  `user_id_create` bigint(255) DEFAULT NULL COMMENT '创建用户id',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=137 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES ('1', 'admin', '超级管理员', 'd633268afedf209e1e4ea0f5f43228a8', '6', 'admin@example.com', '123456', '1', '1', '2017-08-15 21:40:39', '2017-08-15 21:41:00');
INSERT INTO `sys_user` VALUES ('2', 'test', '普通用户', 'b132f5f968c9373261f74025c23c2222', '6', 'test@test.com', null, '1', '1', '2017-08-14 13:43:05', '2017-08-14 21:15:36');
-- ----------------------------
-- Table structure for `sys_user_role`
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8 COMMENT='用户与角色对应关系';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES ('1', '1', '1');
INSERT INTO `sys_user_role` VALUES ('2', '2', '2');

DROP TABLE IF EXISTS `gateway_route`;

CREATE TABLE `gateway_route` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `from_path` varchar(255) DEFAULT NULL COMMENT '请求路径',
  `from_pathpattern` varchar(255) DEFAULT NULL COMMENT '请求路径匹配符',
  `to_hostport` varchar(255) DEFAULT NULL COMMENT '目标地址',
  `to_path` varchar(100) DEFAULT NULL COMMENT '目标路径',
  `service_id` varchar(100) DEFAULT NULL COMMENT '服务ID',
  `rpc` tinyint(1) DEFAULT NULL COMMENT '是否RPC请求',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `serviceDefinition` (`to_hostport`,`to_path`,`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='网关路由表';



# Dump of table gateway_rpc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `gateway_rpc`;

CREATE TABLE `gateway_rpc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `service_name` varchar(255) DEFAULT NULL COMMENT '服务名',
  `method_name` varchar(100) DEFAULT NULL COMMENT '方法名',
  `service_group` varchar(100) DEFAULT NULL COMMENT '服务组名',
  `service_version` varchar(100) DEFAULT NULL COMMENT '服务版本',
  `proto_context` blob COMMENT 'proto内容',
  `proto_req` blob COMMENT 'proto请求',
  `proto_rep` blob COMMENT 'proto请求',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '修改时间',
  `route_id` bigint(20) unsigned NOT NULL COMMENT '路由ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `serviceDefinition` (`service_name`,`method_name`,`service_group`,`service_version`),
  KEY `route` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='rpc服务映射表';

