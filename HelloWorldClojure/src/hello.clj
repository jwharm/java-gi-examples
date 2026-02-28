(ns hello
  (:import
   (org.gnome.gio ApplicationFlags Application$ActivateCallback)
   (org.gnome.gtk Align Application ApplicationWindow Box Button Button$ClickedCallback Orientation)))

(def app (doto (Application. "org.javagi.examples.HelloWorldClojure" (into-array ApplicationFlags [ApplicationFlags/DEFAULT_FLAGS]))
           (.onActivate (reify Application$ActivateCallback
                          (run [this]
                            (let [app-window (ApplicationWindow. app)]
                              (doto app-window
                                (.setTitle "GTK from Clojure")
                                (.setDefaultSize 300 200)
                                (.setChild (doto (Box. Orientation/VERTICAL 0)
                                             (.setHalign Align/CENTER)
                                             (.setValign Align/CENTER)
                                             (.append (doto (Button/withLabel "Hello world!")
                                                        (.onClicked (reify Button$ClickedCallback
                                                                      (run [this] (.close app-window))))))))
                                (.present))))))))

(defn run [opts]
  (.run app nil))
