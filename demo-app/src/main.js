// Load the Scala.js ES module bundle. We point a native `<script type="module">` straight at the
// linked file (served raw by our vendored vite plugin under `/@scalajs-raw/`) rather than
// `import 'scalajs:main.js'`, because Vite's dev-only `import-analysis` pass fails to parse the large
// generated bundle ("invalid JS syntax") even though it is valid ESM. Serving it raw skips that
// transform; the browser loads it as a normal ES module. With
// `scalaJSUseMainModuleInitializer := true`, loading the bundle invokes Main.main(args).
const s = document.createElement('script');
s.type = 'module';
s.src = '/@scalajs-raw/main.js';
document.head.appendChild(s);
