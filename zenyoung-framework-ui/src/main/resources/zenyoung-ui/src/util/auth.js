import Cookies from 'js-cookie'

const accessTokenKey = 'auth-access-token'
const refreshTokenKey = 'auth-refresh-token'

/**
 * 获取访问令牌
 * @returns {*}
 */
export function getAccessToken() {
  return Cookies.get(accessTokenKey)
}

/**
 * 设置访问令牌
 * @param accessToken
 * 访问令牌
 */
export function setAccessToken(accessToken) {
  Cookies.set(accessTokenKey, accessToken)
}

/**
 * 移除访问令牌
 */
export function removeAccessToken() {
  Cookies.remove(accessTokenKey)
}

/**
 * 获取刷新令牌
 * @returns {*}
 */
export function getRefreshToken() {
  return Cookies.get(refreshTokenKey)
}

/**
 * 设置刷新令牌
 * @param refreshToken
 * 刷新令牌
 */
export function setRefreshToken(refreshToken) {
  Cookies.set(refreshTokenKey, refreshToken)
}

/**
 * 移除刷新令牌
 */
export function removeRefreshToken() {
  Cookies.remove(refreshTokenKey)
}
