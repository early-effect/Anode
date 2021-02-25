var path = require("path");
var CopyWebpackPlugin = require("copy-webpack-plugin");
var HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
  devServer: {
    hot: true,
    inline:true,
    compress: true,
    disableHostCheck: true,
    historyApiFallback: true,
    port: 9000,
    host: "0.0.0.0",
  },
  mode: "development",
  resolve: {
    alias: {
      js: path.resolve(__dirname, "../../../../src/main/js"),
      scalajs: path.resolve(__dirname, "./scalajs-entry.js")
    },
    modules: [path.resolve(__dirname, "node_modules")]
  },
  plugins: [
    new CopyWebpackPlugin([
      {from: path.resolve(__dirname, "../../../../public")}
    ]),
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, "../../../../public/index.html"),
      inject: "head",
    })
  ],
  output: {
    hotUpdateChunkFilename: 'hot/hot-update.js',
    hotUpdateMainFilename: 'hot/hot-update.json',
    // devtoolModuleFilenameTemplate: f => {
    //   if (
    //     f.resourcePath.startsWith("http://") ||
    //     f.resourcePath.startsWith("https://") ||
    //     f.resourcePath.startsWith("file://")
    //   ) {
    //     return f.resourcePath;
    //   } else {
    //     return "webpack://" + f.namespace + "/" + f.resourcePath;
    //   }
    // }
  }
};
