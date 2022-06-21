import Dict from './Dict'
import {mergeOptions} from './DictOptions'

export default function (Vue, options) {
  mergeOptions(options)
  Vue.mixin({
    data() {
      if (this.$options === undefined || this.$options.dicts === undefined || this.$options.dicts === null) {
        return {}
      }
      const dict = new Dict()
      dict.owner = this
      return {
        dict
      }
    },
    created() {
      if (!(this.dict instanceof Dict)) {
        return
      }
      const onCreated = options['onCreated']
      if (onCreated) {
        onCreated(this.dict)
      }
      this.dict.init(this.$options.dicts).then(() => {
        const onReady = options['onReady']
        if (onReady) {
          onReady(this.dict)
        }
        this.$nextTick(() => {
          this.$emit('dictReady', this.dict)
          if (this.$options.methods) {
            const onDictReady = this.$options.methods['onDictReady']
            if (onDictReady instanceof Function) {
              onDictReady.call(this, this.dict)
            }
          }
        })
      })
    }
  })
}
