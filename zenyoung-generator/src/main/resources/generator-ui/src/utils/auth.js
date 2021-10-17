import Cookies from 'js-cookie'

const tokenKey = 'generator-token'

export function getToken() {
    return Cookies.get(tokenKey)
}

export function setToken(token) {
    return Cookies.set(tokenKey, token)
}
