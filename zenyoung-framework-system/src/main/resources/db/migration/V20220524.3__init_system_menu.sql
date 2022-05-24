-- --------------------------------------------------------------------------------------------------------------------
-- 初始化菜单
-- --------------------------------------------------------------------------------------------------------------------
-- 清除数据
delete from tbl_sys_role_menus;
delete from tbl_sys_menu;
-- --------------------------------------------------------------------------------------------------------------------
-- 新增数据
insert into tbl_sys_menu(`id`,`code`,`name`,`parent_id`,`path`,`component`,`type`,`perms`,`icon`) values
-- 1.系统管理-目录
(100, 1, '系统管理', 0, 'sys', null, 1, null, 'system'),
-- 部门管理-菜单
(100001, 1, '部门管理', 100, 'dept', 'sys/dept/index', 2, 'sys:dept:list', 'tree'),
-- 部门管理-按钮
(10000101, 1, '部门查询', 100001, null, null, 3, 'sys:dept:query', '#'),
(10000102, 2, '部门新增', 100001, null, null, 3, 'sys:dept:add', '#'),
(10000103, 3, '部门修改', 100001, null, null, 3, 'sys:dept:edit', '#'),
(10000104, 4, '部门删除', 100001, null, null, 3, 'sys:dept:del','#'),
-- 岗位管理-菜单
(100002, 2, '岗位管理', 100, 'post', 'sys/post/index', 2, 'sys:post:list', 'post'),
-- 岗位管理-按钮
(10000201, 1, '岗位查询', 100002, null, null, 3, 'sys:post:query', '#'),
(10000202, 2, '岗位新增', 100002, null, null, 3, 'sys:post:add', '#'),
(10000203, 3, '岗位修改', 100002, null, null, 3, 'sys:post:edit', '#'),
(10000204, 4, '岗位删除', 100002, null, null, 3, 'sys:post:del', '#'),
-- 菜单管理-菜单
(100003, 3, '菜单管理', 100, 'role', 'sys/menu/index', 2, 'sys:menu:list', 'tree-table'),
-- 菜单管理-按钮
(10000301, 1, '菜单查询', 100003, null, null, 3, 'sys:menu:query', '#'),
(10000302, 2, '菜单新增', 100003, null, null, 3, 'sys:menu:add', '#'),
(10000303, 3, '菜单修改', 100003, null, null, 3, 'sys:menu:edit', '#'),
(10000304, 4, '菜单删除', 100003, null, null, 3, 'sys:menu:del', '#'),
-- 角色管理-菜单
(100004, 4, '角色管理', 100, 'role', 'sys/role/index', 2, 'sys:role:list', 'peoples'),
-- 角色管理-按钮
(10000401, 1, '角色查询', 100004, null, null, 3, 'sys:role:query', '#'),
(10000402, 2, '角色新增', 100004, null, null, 3, 'sys:role:add', '#'),
(10000403, 3, '角色修改', 100004, null, null, 3, 'sys:role:edit', '#'),
(10000404, 4, '角色删除', 100004, null, null, 3, 'sys:role:del', '#'),
-- 用户管理-菜单
(100005, 5, '用户管理', 100, 'user', 'sys/user/index', 2, 'sys:user:list', 'user'),
-- 用户管理-按钮
(10000501, 1, '用户查询', 100005, null, null, 3, 'sys:user:query','#'),
(10000502, 2, '用户新增', 100005, null, null, 3, 'sys:user:add', '#'),
(10000503, 3, '用户修改', 100005, null, null, 3, 'sys:user:edit', '#'),
(10000504, 4, '用户删除', 100005, null, null, 3, 'sys:user:del', '#'),
(10000505, 5, '重置密码', 100005, null, null, 3, 'sys:user:resetPwd', '#'),
-- 字典管理-菜单
(100006, 6, '字典管理', 100, 'dict', 'sys/dict/index', 2, 'sys:dict:list', 'dict'),
-- 字典管理-按钮
(10000601, 1, '字典查询', 100006, null, null, 3, 'sys:dict:query', '#'),
(10000602, 2, '字典新增', 100006, null, null, 3, 'sys:dict:add', '#'),
(10000603, 3, '字典修改', 100006, null, null, 3, 'sys:dict:edit','#'),
(10000604, 4, '字典删除', 100006, null, null, 3, 'sys:dict:del', '#'),
-- 参数设置-菜单
(100007, 7, '参数设置', 100, 'config', 'sys/config/index', 2, 'sys:config:list', 'edit'),
-- 参数设置-按钮
(10000701, 1, '参数查询', 100007, null, null, 3, 'sys:config:query', '#'),
(10000702, 2, '参数新增', 100007, null, null, 3, 'sys:config:add', '#'),
(10000703, 3, '参数修改', 100007, null, null, 3, 'sys:config:edit', '#'),
(10000704, 4, '参数删除', 100007, null, null, 3, 'sys:config:edit', '#'),
-- 2.系统监控
(200, 2, '系统监控', 0, 'monitor', null, 1, null, 'monitor'),
-- 登录日志-菜单
(200001, 1, '登录日志', 200, 'login', 'monitor/login/index', 2, 'monitor:login:list', 'loginInfo'),
-- 登录日志-按钮
(20000101, 1, '登录查询', 200001, null, null, 3, 'monitor:login:query','#'),
(20000102, 2, '登录删除', 200001, null, null, 3, 'monitor:login:del', '#'),
-- 操作日志-菜单
(200002, 2, '操作日志', 200, 'log', 'monitor/opera/index', 2, 'monitor:opera:list', 'log'),
-- 操作日志-按钮
(20000201, 1, '操作查询', 200002, null, null, 3, 'monitor:opera:query', '#'),
(20000202, 2, '操作删除', 200002, null, null, 3, 'monitor:opera:del', '#'),
-- 在线用户-菜单
(200003, 3, '在线用户', 200, 'online', 'monitor/online/index',2, 'monitor:online:list', 'online'),
-- 在线用户-按钮
(20000301, 1, '在线查询', 200003, null, null, 3, 'monitor:online:query', '#'),
(20000302, 2, '批量强退', 200003, null, null, 3, 'monitor:online:batch', '#'),
(20000303, 3, '单条强退', 200003, null, null, 3, 'monitor:online:force', '#');
-- 数据监控-菜单
(200004, 4, '数据监控', 200, 'druid', 'monitor/druid/index', 2, 'monitor:druid:list', 'druid'),
-- 服务监控-菜单
(200005, 5, '服务监控', 200, 'server', 'monitor/server/index', 2, 'monitor:server:list', 'server'),
-- 缓存监控-菜单
(200006, 6, '缓存监控', 200, 'cache', 'monitor/cache/index', 2, 'monitor:cache:list', 'redis'),
-- 3.系统工具-目录
(300, 3, '系统工具', 0, 'tool', null, 1, null, 'tool');
-- 代码生成-菜单
(300001, 1, '代码生成', 300, 'gen', 'tool/gen/index', 2, 'tool:gen:list', 'code'),
-- 系统接口-菜单
(300002, 2, '系统接口', 300, 'swagger', 'tool/swagger/index', 2, 'tool:swagger:list','swagger'),
-- --------------------------------------------------------------------------------------------------------------------
-- --------------------------------------------------------------------------------------------------------------------
-- 初始化角色菜单
insert into tbl_sys_role_menus(`role_id`, `menu_id`)
select '1653383273813', id
from tbl_sys_menu;
-- --------------------------------------------------------------------------------------------------------------------
-- --------------------------------------------------------------------------------------------------------------------
