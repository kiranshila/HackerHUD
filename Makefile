##
# HackerHUD
#
# @file
CLOJURE=clj
OUT=out/main.bin

.PHONY: clean default erase bootstrap

default: $(OUT)

out/main.js: $(shell find src -type f) $(shell find resources -type f)
	$(CLOJURE) -A:build

$(OUT): out/main.js
	$(CLOJURE) -A:rom

flash: $(OUT)
	$(CLOJURE) -A:flash

clean:
	-rm -r .cpcache/
	-rm -r out

erase:
	$(CLOJURE) -A:erase

bootstrap:
	$(CLOJURE) -A:bootstrap
