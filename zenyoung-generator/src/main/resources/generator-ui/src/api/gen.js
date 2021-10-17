import request from "../utils/request";

// 测试数据库链接字符串
export function genTest(data) {
    return request({
        url: '/gen/data/test',
        method: 'post',
        data: data
    })
}

// 保存数据库链接字符串
export function genSave(data) {
    return request({
        url: '/gen/data/save',
        method: 'post',
        data: data
    })
}

// 获取表数据
export function genTables(queryTableName){
    return request({
        url: '/gen/data/tables',
        method: 'get',
        params: {"queryTableName": queryTableName}
    })
}

// 获取预览数据
export function genPreview(tableName){
    return request({
        url: '/gen/data/preview',
        method: 'get',
        params: {"tableName": tableName}
    })
}

//下载生产数据
export function genDownload(tableName){
    return request({
        url: '/gen/data/download',
        method: 'get',
        responseType: 'blob',
        params: {'tableName': tableName}
    })
}
