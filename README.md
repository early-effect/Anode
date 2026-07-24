# [Anode](https://github.com/early-effect/Anode)

Anode is a Scala.js facade for Preact with emphasis on stateless components.

## Demo (Vite)

```bash
npm ci                  # root: preact / postcss / jsdom for tests
cd demo-app && npm ci   # Vite + plugin
sbt demo/fastLinkJS     # optional warm link
cd demo-app && npm run dev
```

Then open the URL Vite prints (default http://localhost:5173).

Production site build:

```bash
sbt demo/fullLinkJS
cd demo-app && npm run build   # output in demo-app/dist
```

## Tests

```bash
npm ci
sbt test
```

Copyright 2021 Russ White

Licensed under the Apache License, Version 2.0.
