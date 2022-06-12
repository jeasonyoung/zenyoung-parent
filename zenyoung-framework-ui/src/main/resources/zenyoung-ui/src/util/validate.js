/**
 * 检查是否为外部链接
 * @param {string} path
 * 检查地址
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * 检查是否为URL
 * @param {string} url
 * 检查地址
 * @returns {Boolean}
 */
export function validURL(url) {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

/**
 * 检查是否为小写
 * @param {string} val
 * 检查字符
 * @returns {Boolean}
 */
export function validLowerCase(val) {
  const reg = /^[a-z]+$/
  return reg.test(val)
}

/**
 * 检查是否为大写
 * @param {string} val
 * 检查字符
 * @returns {Boolean}
 */
export function validUpperCase(val) {
  const reg = /^[A-Z]+$/
  return reg.test(val)
}

/**
 * 检查是否为字母串
 * @param {string} val
 * 检查字符串
 * @returns {Boolean}
 */
export function validAlphabets(val) {
  const reg = /^[A-Za-z]+$/
  return reg.test(val)
}

/**
 * 检查是否为Email
 * @param {string} email
 * @returns {Boolean}
 */
export function validEmail(email) {
  const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return reg.test(email)
}

/**
 * 是否为字符串
 * @param {string} val
 * @returns {Boolean}
 */
export function isString(val) {
  return typeof val === 'string' || val instanceof String;

}

/**
 * 是否为数组
 * @param {Array} arg
 * @returns {Boolean}
 */
export function isArray(arg) {
  if (typeof Array.isArray === 'undefined') {
    return Object.prototype.toString.call(arg) === '[object Array]'
  }
  return Array.isArray(arg)
}
