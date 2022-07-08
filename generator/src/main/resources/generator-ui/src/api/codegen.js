import request from "@/util/request";
import {saveAs} from "file-saver";
import {Loading, Message} from "element-ui";

//获取是否独立部署
export function getAlone() {
    return request({
        url: '/get-alone',
        method: 'get'
    })
}

//获取导出文件类型
export function getExportFileTypes() {
    return request({
        url: '/get-file-types',
        method: 'get'
    })
}

//获取数据库集合
export function getDatabases() {
    return request({
        url: '/get-databases',
        method: 'get'
    })
}

//获取数据库下的表集合
export function getAllTables(dbName) {
    return request({
        url: `/${dbName}/tables`,
        method: 'get'
    })
}

function startLoading(msg) {
    const title = msg || "正在加载数据,请求稍后。。。"
    return Loading.service({text: title, spinner: "el-icon-loading", background: "rgba(0,0,0,0.7)"})
}

function endLoading(ld) {
    if (ld) {
        ld.close()
        ld = null
    }
}

//获取预览文件
export function getPreview(data) {
    let ld = startLoading("正在预览代码，请稍候...")
    return request({
        url: '/preview',
        method: 'post',
        data: data
    }).then((res) => {
        endLoading(ld)
        return new Promise((resolve) => resolve(res))
    }).catch(() => {
        endLoading(ld)
    })
}

//下载代码文件
export function download(data, filename) {
    let ld = startLoading("正在下载文件，请稍候...")
    return request({
        url: '/download',
        method: 'post',
        data: data,
        responseType: 'blob'
    }).then(async (data) => {
        const blob = new Blob([data])
        if (navigator.msSaveBlob) {
            navigator.msSaveBlob(blob, filename)
        } else {
            const url = window.URL.createObjectURL(blob)
            saveAs(url, filename)
        }
        endLoading(ld)
    }).catch((err) => {
        endLoading(ld)
        console.error(err)
        Message.error("下载文件出现错误，请联系管理员！")
    })
}

//构建本地代码
export function buildLocal(data) {
    let ld = startLoading("正在生成本地文件，请稍候...")
    return request({
        url: '/local',
        method: 'post',
        data: data
    }).then((res) => {
        endLoading(ld)
        return new Promise((resolve) => resolve(res))
    }).catch(() => {
        endLoading(ld)
    })
}
