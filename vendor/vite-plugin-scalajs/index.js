// Vendored + hardened fork of @scala-js/vite-plugin-scalajs (v1.1.0, Apache-2.0).
//
// Why vendored: sbt 2.x emits a trailing ANSI "erase to end of screen" escape (ESC[0J) on its own
// line when a `--batch` command exits. The upstream plugin extracts the Scala.js output directory as
// the LAST line of sbt's stdout (`split('\n').at(-1)`), so it picks up that escape instead of the
// path — producing a bogus module id like `@id/%1B[0J/main.js` and a failed load. Until upstream
// handles sbt 2.x, we own a copy and (a) strip ANSI escapes and (b) take the last NON-EMPTY line.
//
// This also gives ascent a place to extend the integration later (extra tasks, watch wiring, etc.).
//
// Upstream: https://github.com/scala-js/vite-plugin-scalajs

import { spawn } from "child_process";

// Strip CSI / ANSI escape sequences (colour codes, the ESC[0J screen-clear sbt 2.x emits, etc.).
function stripAnsi(s) {
  // eslint-disable-next-line no-control-regex
  return s.replace(/\x1b\[[0-9;?]*[ -/]*[@-~]/g, "");
}

function printSbtTask(task, cwd) {
  const args = ["--batch", "-no-colors", "-Dsbt.supershell=false", `print ${task}`];
  const options = {
    cwd: cwd,
    stdio: ["ignore", "pipe", "inherit"],
  };
  const child =
    process.platform === "win32"
      ? spawn("sbt.bat", args.map((x) => `"${x}"`), { shell: true, ...options })
      : spawn("sbt", args, options);

  let fullOutput = "";
  child.stdout.setEncoding("utf-8");
  child.stdout.on("data", (data) => {
    fullOutput += data;
    process.stdout.write(data); // tee on our own stdout
  });

  return new Promise((resolve, reject) => {
    child.on("error", (err) => {
      reject(new Error(`sbt invocation for Scala.js compilation could not start. Is it installed?\n${err}`));
    });
    child.on("close", (code) => {
      if (code !== 0) {
        let errorMessage = `sbt invocation for Scala.js compilation failed with exit code ${code}.`;
        if (fullOutput.includes("Not a valid command: --")) {
          errorMessage += "\nCause: Your sbt launcher script version is too old (<1.3.3).";
          errorMessage += "\nFix: Re-install the latest version of sbt launcher script from https://www.scala-sbt.org/";
        }
        reject(new Error(errorMessage));
      } else {
        // THE FIX vs upstream: sbt 2.x interleaves an ANSI screen-clear (ESC[0J) AND prints a
        // trailing `[success] elapsed time: …` line AFTER the `print` result — so neither "last
        // line" nor "last non-empty line" is the path. Strip ANSI, then pick the last line that
        // looks like the Scala.js linker output directory (an absolute path under a Scala.js target,
        // ending in `-fastopt` or `-opt`). Fall back to the last non-`[info]/[success]/...` line.
        const lines = stripAnsi(fullOutput)
          .split("\n")
          .map((l) => l.trim())
          .filter((l) => l.length > 0);
        const looksLikeOutputDir = (l) =>
          (l.startsWith("/") || /^[A-Za-z]:[\\/]/.test(l)) && /(-fastopt|-opt|fastLinkJS|fullLinkJS)/.test(l);
        const pathLine =
          [...lines].reverse().find(looksLikeOutputDir) ??
          [...lines].reverse().find((l) => !/^\[(info|success|warn|error)\]/.test(l));
        if (!pathLine)
          reject(new Error(`Could not find the Scala.js output directory in sbt output:\n${fullOutput}`));
        else resolve(pathLine);
      }
    });
  });
}

// Stable dev URL under which we serve the linked Scala.js output RAW (no Vite transform). The linker
// DOES emit a proper ES module (verified: no NoModule IIFE wrapper, and esbuild's bundler parses it
// as ESM), but Vite's dev-only `import-analysis` pass fails to parse the large generated bundle
// ("invalid JS syntax"). Serving it raw and loading it via a native <script type="module"> (see the
// example's main.js) skips that transform; the browser then loads it as a normal ES module.
const RAW_PREFIX = "/@scalajs-raw/";

export default function scalaJSPlugin(options = {}) {
  const { cwd, projectID, uriPrefix } = options;
  const fullURIPrefix = uriPrefix ? uriPrefix + ":" : "scalajs:";

  let isDev = undefined;
  let scalaJSOutputDir = undefined;

  async function ensureOutputDir() {
    if (scalaJSOutputDir === undefined) {
      const task = isDev ? "fastLinkJSOutput" : "fullLinkJSOutput";
      const projectTask = projectID ? `${projectID}/${task}` : task;
      scalaJSOutputDir = await printSbtTask(projectTask, cwd);
    }
    return scalaJSOutputDir;
  }

  return {
    name: "scalajs:sbt-scalajs-plugin",

    configResolved(resolvedConfig) {
      isDev = resolvedConfig.mode === "development";
    },

    async buildStart(_options) {
      if (isDev === undefined) throw new Error("configResolved must be called before buildStart");
      await ensureOutputDir();
    },

    // Serve the linked bundle (and its sourcemap) verbatim, with no transform/import-analysis, so a
    // NoModule classic-script bundle loads correctly via a plain <script src>.
    configureServer(server) {
      server.middlewares.use(async (req, res, next) => {
        if (!req.url || !req.url.startsWith(RAW_PREFIX)) return next();
        try {
          const dir = await ensureOutputDir();
          const name = req.url.slice(RAW_PREFIX.length).split("?")[0];
          const { readFile } = await import("fs/promises");
          const { join } = await import("path");
          const body = await readFile(join(dir, name));
          res.setHeader("Content-Type", name.endsWith(".map") ? "application/json" : "text/javascript");
          res.end(body);
        } catch (e) {
          next(e);
        }
      });
    },

    resolveId(source, _importer, _options) {
      if (scalaJSOutputDir === undefined) throw new Error("buildStart must be called before resolveId");
      if (!source.startsWith(fullURIPrefix)) return null;
      const path = source.substring(fullURIPrefix.length);
      return `${scalaJSOutputDir}/${path}`;
    },
  };
}

// The dev URL prefix the example's classic-script loader points at.
export const scalaJSRawPrefix = RAW_PREFIX;
