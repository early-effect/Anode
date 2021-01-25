const webpack = require("webpack");
const path = require("path");

const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const merge = require("webpack-merge");
const core = require("./webpack-core.config.js");
const md5 = require("crypto-js/md5");

const generatedConfig = require("./scalajs.webpack.config.js");
const entries = {};

entries[Object.keys(generatedConfig.entry)[0]] = "scalajs";

module.exports = merge(core, {
  mode: "production",
  entry: entries,
  output: {
    "path": path.resolve(__dirname, "dist"),
    "publicPath": "/",
    //Deployer script needs chunkhash to be 32 chars in length and bookended by `.` in order to set proper cache headers
    hashDigestLength: 32,
    hashFunction: "md5",
    filename: "js/[name].[chunkhash].js"
  },
  plugins: [
    new CopyWebpackPlugin([
      { from: path.resolve(__dirname, "../../../../public") }
    ]),
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, "../../../../public/index.html"),
      inject: "head",
    }),
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: JSON.stringify('production')
      }
    })
  ],
  optimization: {
    runtimeChunk: "single",
    splitChunks: {
      chunks: "all",
      maxInitialRequests: Infinity,
      minSize: 0,
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name(module) {
            // get the name. E.g. node_modules/packageName/not/this/part.js
            // or node_modules/packageName
            const packageName = module.context.match(
              /[\\/]node_modules[\\/](.*?)([\\/]|$)/,
            )[1];

            // npm package names without @ symbols and md5 summed
            return `lib.${md5(packageName.replace("@", "")).toString()}`;
          },
        },
      },
    },
  },
})
