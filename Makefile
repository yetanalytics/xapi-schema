.phony: clean repl test-clj test-cljs ci
clean:
	rm -rf target pom.xml.asc logs out
repl:
	clj -A:dev -r
test-clj:
	clojure -A:dev -m xapi-schema.runner
test-cljs:
	clojure -A:dev -m cljs.main -co build.test.edn -c
	node out/main.js
ci:	test-clj test-cljs
