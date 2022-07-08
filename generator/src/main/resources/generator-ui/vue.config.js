const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    indexPath: process.env.NODE_ENV === "production" ? "code.html" : "index.html",
    chainWebpack: config => {
        config.plugin('html')
            .tap(args => {
                args[0].title = `${process.env.VUE_APP_TITLE}-${process.env.NODE_ENV}`
                return args
            })
    }
})
