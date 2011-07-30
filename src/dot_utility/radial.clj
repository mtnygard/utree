(ns dot-utility.radial
  (:import [javax.swing JFrame JPanel]
           [java.awt BorderLayout RenderingHints Color Dimension Graphics2D BasicStroke]
           [java.awt.geom Path2D$Double Line2D$Double]))

(def default-frame-options
     {:title "Radar display"
      :width 400
      :height 400})

(def pi2 (* 2.0 Math/PI))
(defn cos [^double a] (Math/cos a))
(defn sin [^double a] (Math/sin a))

(def color-axis (Color. 186 186 186))
(def color-score (Color. 156 158 222))
(def color-outline (Color. 109 110 155))
(def color-fill (Color. 156 158 222 64))

(defn petals-on-the-rose
  [ds scale-fn]
  (let [theta (/ pi2 (count ds))]
    (loop [m []
           n 0
           ang (/ Math/PI 2.0)
           [[_ value domain] & more] (seq ds)]
      (if value
        (recur (conj m
                      {:angle ang
                       :radius (scale-fn value domain)})
               (inc n)
               (+ ang theta)
               more)
        m))))

(defn points [ds]
  (petals-on-the-rose ds /))

(defn axes [ds]
  (petals-on-the-rose ds (constantly 1.0)))

(defn polar->cartesian
  [polars]
  (reduce
   (fn [coords {radius :radius angle :angle}]
     (conj coords {:x (* radius (cos angle))
                   :y (* radius (sin angle))}))
   []
   polars))

(defn line
  [^double x1 ^double y1 ^double x2 ^double y2]
  (new Line2D$Double x1 y1 x2 y2))

(defn radii
  [pts]
  (map (fn [{x :x y :y}] (line 0 0 x y)) pts))

(defn hull
  [pts]
  (let [path (Path2D$Double.)
        {x-org :x y-org :y} (first pts)
        rest (reverse pts)]
    (.moveTo path x-org y-org)
    (doseq [{x :x y :y} rest]
      (.lineTo path x y))
    path))

(defn draw-radar
  [^Graphics2D g ds w h]
  (let [scale (/ (min w h) 2.0)
        pen-width (/ 1.5 scale)
        mapping (affine-mapping scale (- scale) (/ w 2) (/ h 2))
        axes (polar->cartesian (axes ds))
        scores (polar->cartesian (points ds))
        hull (hull scores)
        ]
    #_(.scale g scale (- scale))
    (.translate g (/ w 2.0) (/ h 2.0))
    (.scale g scale (- scale))
    (.setStroke g (BasicStroke. pen-width))
    (.setColor g color-axis)
    (doseq [l (radii axes)]
      (.draw g l))
    (.setStroke g (BasicStroke. (* 2 pen-width)))
    (.setColor g color-score)
    (doseq [l (radii scores)]
      (.draw g l))
    (.setColor g color-fill)
    (.fill g hull)
    (.setColor g color-outline)
    (.draw g hull)))

(defn antialias
  "Return a Graphics object with antialiasing turned on.
   Does not mutate the original"
  [g]
  (if (= RenderingHints/VALUE_ANTIALIAS_ON (.getRenderingHint g RenderingHints/KEY_ANTIALIASING))
    g
    (doto (.create g)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON))))

(defn make-radar-panel
  [ds width height]
  (doto
      (proxy [javax.swing.JComponent] []
        (paintComponent [g]
                        (draw-radar (antialias g) ds (.getWidth this) (.getHeight this))))))

(defn show-radar-display
  "Display a new window with a radar plot of the given data items.

   width and height control the size of the window.

   Each item is a list of (label, score, max-score). E.g., (\"Performance\", 3, 10) means
   that the Performance measure scored 3 out of a possible 10. (All scores start from 0.)"
  [ds & options]
  (let [{:keys [width height title]} (into default-frame-options (apply hash-map options))
        radar (make-radar-panel ds width height)
        panel (doto (JPanel.)
                (.setBorder (javax.swing.BorderFactory/createEmptyBorder 20 20 20 20))
                (.setLayout (BorderLayout.))
                (.add radar BorderLayout/CENTER))]
    (doto (JFrame. title)
      (.add panel)
      (.setSize width height)
      (.show))
    radar))
