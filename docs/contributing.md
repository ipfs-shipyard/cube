# How to contribute to Cube?

## CONTRIBUTE WITH COOOOODE
### Requirements

- Java version 1.8 (8) or later
- Leingen (https://leiningen.org)
- Only tested on Linux and macOS currently

### Development
#### Quickstart

Open a terminal window and enter the command:

```
lein repl
```

Once the repl is running, run `(start true)` to start the web server
and figwheel which builds the frontend.

Now visit `localhost:3000` in your favorite browser

#### Frontend

- Use `(figwheel-sidecar.repl-api/start-figwheel!)` to build and watch
  all frontend resources. Runs automatically with `(start true)`
- Styles won't build automatically, you can run `lein less auto` to start doing
  that in a separate terminal.
- cljs code will automatically build with `(start true)` but you can also run
  `lein cljsbuild once` to build it once.

If you want a browser repl, first run `(start true)` and then in your favorite
repl-enabled editor: `:Piggieback (figwheel-sidecar.repl-api/repl-env)` (example
with vim-fireplace)

Make sure that after `(start true)` you open `localhost:3000` (or wherever
you have cube running) in your browser, as the repl will hang until a browser
is available for executing commands for you.

#### Backend

- Use `lein run` to start the application like a user normally would (opens a GUI,
  uses a random port and opens a browser window automatically)
- Use `(start)` in repl to just run the server
- Use `(reset)` after `(start)` to reload all code and restart the system
- Use `PORT=3000 CUBE_GUI=false CUBE_OPEN_BROWSER=false lein run` to run the app
  without opening the GUI or a browser window, and always use port 3000.

##### Tests

You can run the tests with `lein test` or even `lein auto test` for self-running
tests. That's magic!

#### Release

- Make sure you're on Java version 1.8 (8) with `java -version`
- `lein less once`
- `lein cljs once`
- `lein uberjar`

Now the released jar is the one in `target/cube-$version-standalone.jar`

#### Directory Structure

Basically, all the backend code lives at `src/cube`, frontend lives at `src/ui`
and the shared code between the two lives in `src/shared`.

## Issues

Please don't be afraid to open a issue if you have a question/problems about the code/product.

Also, please do open a issue discussing a feature before trying to implement it,
otherwise we might have to reject your eventual PR :(
