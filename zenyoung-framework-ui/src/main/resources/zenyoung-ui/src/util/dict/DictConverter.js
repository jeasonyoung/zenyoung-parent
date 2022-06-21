import DictOptions from './DictOptions'
import DictData from './DictData'

/**
 *
 * @param {DictData} dict
 * @param {DictMeta} dictMeta
 */
export default function(dict, dictMeta) {
  const label = determineDictField(dict, dictMeta.labelField, ...DictOptions.DEFAULT_LABEL_FIELDS)
  const value = determineDictField(dict, dictMeta.valueField, ...DictOptions.DEFAULT_VALUE_FIFLDS)
  return new DictData(dict[label], dict[value], dict)
}

/**
 * 确认字典字段
 * @param {DictData} dict
 * @param {...String} fields
 */
function determineDictField(dict, ...fields) {
  return fields.find(f => Object.prototype.hasOwnProperty.call(dict, f))
}

