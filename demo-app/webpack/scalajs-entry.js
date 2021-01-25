if (process.env.NODE_ENV === "production") {
  const opt = require("./demo-app-opt.js");
  module.exports = opt;
  //opt.entrypoint();
} else {
  var exports = window;
  exports.require = require("./demo-app-fastopt-entrypoint.js").require;
  window.global = window;

  const fastOpt = require("./demo-app-fastopt.js");
  fastOpt.main();
  module.exports = fastOpt;
  let e = window.document.documentElement.querySelector("script[src='hot/hot-update.js']");
  window.document.head.removeChild(e)
  if (module.hot) {
    module.hot.accept();
  }
}
