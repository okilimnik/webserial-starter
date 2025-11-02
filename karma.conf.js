module.exports = function (config) {
    config.set({
        singleRun: true,
        browsers: ['ChromeHeadless'],
        files: ['resources/test/karma-test.js'],
        frameworks: ['cljs-test'],
        plugins: [
            'karma-cljs-test',
            'karma-chrome-launcher'
        ],
        colors: true,
        logLevel: config.LOG_INFO,
        client: {
            args: ['shadow.test.karma.init']
        }
    })
}