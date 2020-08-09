((nil
  (cider-custom-cljs-repl-init-form . "(do (require 'esprit.repl)
                                           (cider.piggieback/cljs-repl (esprit.repl/repl-env :endpoint-address \"192.168.4.69\")))")
  (cider-default-cljs-repl . custom)
  (cider-print-fn . pr)))
