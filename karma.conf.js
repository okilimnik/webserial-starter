module.exports = function (config) {
    config.set({
        singleRun: true,
        browsers: ['ChromeHeadlessNoSandbox'],
        files: ['resources/test/karma-test.js'],
        preprocessors: {
            'resources/test/karma-test.js': ['coverage']
        },
        frameworks: ['cljs-test'],
        plugins: [
            'karma-cljs-test',
            'karma-chrome-launcher',
            'karma-coverage'
        ],
        colors: true,
        logLevel: config.LOG_INFO,
        client: {
            args: ['shadow.test.karma.init']
        },

        customLaunchers: {
            ChromeHeadlessNoSandbox: {
                base: 'ChromeHeadless',
                flags: ['--no-sandbox']
            }
        },

        reporters: ['coverage'],

        coverageReporter: {
            reporters: [
                { type: 'text' },
                { type: 'text-summary' },
            ]
        }
    })
}