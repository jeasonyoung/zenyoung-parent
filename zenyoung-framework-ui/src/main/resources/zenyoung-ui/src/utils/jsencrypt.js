import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'

// 密钥对生成 http://web.chacuo.net/netrsakeypair
const publicKey = '-----BEGIN PUBLIC KEY-----\n' +
  'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCt3Hz/3bRDcw12TjPfY+6GPOJ1\n' +
  'aDhEzwLLMoRJzlRoyFAgnvevMFdl9dLbmFxmRJelf1suZy1fwD4vnfAhZJRnfYjf\n' +
  'jgGYDOtNFLzFYRYRFArABW0vS64e6YDQG9CiiH0iLj2XOm98dGFtDxMUwmEnhstz\n' +
  '7AEL01uXrHbP23Ch2wIDAQAB\n' +
  '-----END PUBLIC KEY-----\n'

// RSA加密
export function encrypt(txt) {
  if (txt !== '') {
    const encryptor = new JSEncrypt()
    encryptor.setPublicKey(publicKey) // 设置公钥
    return encryptor.encrypt(txt) // 对数据进行加密
  }
  return txt
}
