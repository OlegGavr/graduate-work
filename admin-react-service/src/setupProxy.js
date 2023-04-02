// eslint-disable-next-line @typescript-eslint/no-var-requires
const {createProxyMiddleware} = require("http-proxy-middleware");

module.exports = function (app) {
    app.use(
        process.env.REACT_APP_API_URL,
        createProxyMiddleware({
            target: process.env.REACT_APP_PROXY_IP,
            secure: false,
            changeOrigin: true,
            pathRewrite: (path) => {
                return path.replace("/rest", "");
            },
        }),
    );
};
