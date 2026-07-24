import { defineConfig } from 'vite';
// Vendored, sbt-2.x-hardened fork (see vendor/) — upstream picks up sbt 2.x's trailing
// ANSI escape as the output path; our fork strips it.
import scalaJSPlugin from 'vite-plugin-scalajs-anode';

export default defineConfig({
  plugins: [
    // `cwd` is the sbt root; `projectID` matches `sbt show demo/projectID`.
    scalaJSPlugin({ cwd: '..', projectID: 'demo' }),
  ],
  server: { open: true, port: 5173 },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
});
