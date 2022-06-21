/**
 * @classdesc 字典数据
 * @property {String} label 标签名字段
 * @property {*} value 标签值字段
 * @property {Object} raw 原始数据
 */
export default class DictData {
  constructor(label, value, raw) {
    this.label = label
    this.value = value
    this.raw = raw
  }
}
