if (process.env.NODE_ENV === "production") {
  const opt = require("./preactor-demo-app-opt.js");
  opt.entrypoint();
} else {
  var exports = window;
  exports.require = require("./preactor-demo-app-fastopt-entrypoint.js").require;
  window.global = window;

  const fastOpt = require("./preactor-demo-app-fastopt.js");
//  fastOpt.entrypoint();
  module.exports = fastOpt;
  if (module.hot) {
    module.hot.accept();
  }
}
