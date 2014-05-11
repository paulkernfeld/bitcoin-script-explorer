Bitcoin Script Explorer
=======================
Bitcoin Script Explorer is a tool designed to help programmers understand Bitcoin Script.

Use it at its [official location](http://paulkernfeld.com/bse).

Tools
-----
Bitcoin Script Explorer is a [ClojureScript](https://github.com/clojure/clojurescript) project using [leiningen](http://leiningen.org/) and [PhantomJS](http://phantomjs.org/), so you'll need to install those tools if you want to build the ClojureScript.

To run the ClojureScript unit tests:

    lein cleantest

To build the ClojureScript to `app/js/cljs/main.js`:

    lein production

Project Structure
-----------------
- `app/` contains the web project
- `resources/` contains Javascript files which are imported into ClojureScript (for crypto)
- `runners/` contains the PhantomJS runner, used for running ClojureScript unit tests
- `src/` contains the ClojureScript source code
- `test/` contains ClojureScript unit tests
- `project.clj` contains the project's leiningen configuration
