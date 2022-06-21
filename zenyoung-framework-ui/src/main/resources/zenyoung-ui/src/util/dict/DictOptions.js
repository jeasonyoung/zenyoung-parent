import {mergeRecursive} from '@/util/utils'
import dictConverter from './DictConverter'

export const options = {
  metas: {
    '*': {
      /**
       * 字典请求:方法签名为 function(dictMeta: DictMeta): Promise
       * @param {DictMeta} dictMeta
       * @returns {Promise<*[]>}
       */
      request: (dictMeta) => {
        console.log(`load dict ${dictMeta.type}`)
        return Promise.resolve([])
      },
      /**
       * 字典响应: 方法签名为 function(response: Object,dictMeta: DictMeta): DictData
       * @param response
       * @param dictMeta
       * @returns {*[]|*}
       */
      responseConverter: (response, dictMeta) => {
        const dicts = response['rows'] instanceof Array ? response['rows'] : response
        if (dicts === undefined) {
          console.warn(`no dict data of "${dictMeta.type}" found in the response`)
          return []
        }
        return dicts.map(d => dictConverter(d, dictMeta))
      },
      /**
       * 标签字段名
       */
      labelField: 'label',
      /**
       * 标签值字段名
       */
      valueField: 'value'
    }
  },
  /**
   * 默认标签字段数组
   */
  DEFAULT_LABEL_FIELDS: ['label', 'name', 'title'],
  /**
   * 默认值字段数组
   */
  DEFAULT_VALUE_FIFLDS: ['value', 'id', 'key']
}

export function mergeOptions(src) {
  mergeRecursive(options, src)
}

//导出
export default options
