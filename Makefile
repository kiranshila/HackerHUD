##
# HackerHUD
#
# @file
# @version 0.1

CLOJURE=clj
PORT=/dev/ttyUSB0
ESP=esptool.py
OUT=out/main.bin
ESPFLAGS=--port $(PORT) --baud 2000000

.PHONY: clean default erase bootloader

default: out/main.bin

out/main.js: src/hackerhud/core.cljs resources/build-opts.edn deps.edn
	$(CLOJURE) -A:build

out/main.bin: out/main.js
	$(CLOJURE) -A:rom

flash: out/main.bin
	$(ESP) $(ESPFLAGS) write_flash 0x2C0000 $(OUT)

clean:
	-rm -r .cpcache/
	-rm -r out

erase:
	$(ESP) $(ESPFLAGS) erase_flash

bootloader:
	$(ESP) $(ESPFLAGS) write_flash 0x1000 bootloader.bin 0x8000 partitions_espruino.bin 0x10000 espruino_esp32.bin

# end
