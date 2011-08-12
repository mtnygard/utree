(ns dot-utility.radial
  (:import [javax.swing JFrame JPanel]
           [java.awt BorderLayout RenderingHints Color Dimension Graphics2D BasicStroke font.TextLayout]
           [java.awt.geom Path2D$Double Line2D$Double]
           [java.awt.image BufferedImage]
           [java.io File]
           [javax.imageio ImageIO]))

(def default-frame-options
     {:title "Radar display"
      :width 400
      :height 400})

(def pi2 (* 2.0 Math/PI))
(defn cos [^double a] (Math/cos a))
(defn sin [^double a] (Math/sin a))

(def color-labels (Color. 96 96 104))
(def color-axis (Color. 186 186 186))
(def color-score (Color. 156 158 222))
(def color-outline (Color. 109 110 155))
(def color-fill (Color. 156 158 222 64))
(def color-background (Color. 255 255 255))

(defn petals-on-the-rose
  [ds scale-fn]
  (let [theta (/ pi2 (count ds))]
    (loop [m []
           n 0
           ang (/ Math/PI 2.0)
           [[_ value domain] & more] (seq ds)]
      (if value
        (recur (conj m [(scale-fn value domain) ang])
               (inc n)
               (+ ang theta)
               more)
        m))))

(defn points [ds]
  (petals-on-the-rose ds /))

(defn axes [ds]
  (petals-on-the-rose ds (constantly 1.0)))

(defn labels [ds]
  (zipmap (map first ds) (petals-on-the-rose ds (constantly 1.0))))

(defn polar->cartesian
  ([radius angle]
     [(* radius (cos angle))
      (* radius (sin angle))])
  ([polars]
     (map #(apply polar->cartesian %) polars)))

(defn hull
  [pts]
  (let [path (Path2D$Double.)
        [x1 y1] (first pts)
        rest (reverse pts)]
    (.moveTo path x1 y1)
    (doseq [[x y] rest]
      (.lineTo path x y))
    path))

(def *graphics* nil)
(def *scale* 1.0)
(defn set-color [color] (.setColor *graphics* color))
(defn draw [shape] (.draw *graphics* shape))
(defn fill [shape] (.fill *graphics* shape))
(defn fill-rect [x y width height] (.fillRect *graphics* x y width height))
(defn translate [x-off y-off] (.translate *graphics* x-off y-off))
(defn scale [x-scale y-scale] (.scale *graphics* x-scale y-scale))
(defn set-pen-width [pw] (.setStroke *graphics* (BasicStroke. (/ pw *scale*))))
(defn font [] (.getFont *graphics*))
(defn set-font-size [pts] (.setFont *graphics* (.deriveFont (.getFont *graphics*) (float (/ pts *scale*)))))
(defn line [x1 y1 x2 y2] (.draw *graphics* (Line2D$Double. x1 y1 x2 y2)))
(defn text-layout [str] (TextLayout. str (font) (.getFontRenderContext *graphics*)))
(defn draw-string [str x y] (.drawString *graphics* str x y))

(defmacro with-graphics [g & body]
  `(binding [*graphics* (.create ~g)]
     (do
       ~@body)))

(defn antialias
  "Return a Graphics object with antialiasing turned on.
   Does not mutate the original"
  [g]
  (if (= RenderingHints/VALUE_ANTIALIAS_ON (.getRenderingHint g RenderingHints/KEY_ANTIALIASING))
    g
    (doto (.create g)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON))))

(defmacro with-antialiasing [g & body]
  `(with-graphics (antialias ~g) (do ~@body)))

(defmacro with-scaling [scale & body]
  `(binding [*scale* ~scale]
     (with-graphics (doto (.create *graphics*) (.scale ~scale ~scale))
       (do
         ~@body))))

(defn draw-hull
  [ds]
  (let [hull (hull (polar->cartesian (points ds)))]
    (set-color color-fill)
    (fill hull)
    (set-color color-outline)
    (draw hull)))

(defn draw-radials
  [polars]
  (doseq [[x y] (polar->cartesian polars)]
    (line 0 0 x y)))

(defn draw-labels
  [scale labels]
  (doseq [[l [r theta]] labels]
    (let [[cx cy] (polar->cartesian (* scale r) theta)
          layout (text-layout l)
          bounds (.getBounds layout)]
      (.draw layout *graphics*
                   (float (- cx (.x bounds) (/ (.width bounds) 2.0)))
                   (float (- cy (.y bounds) (/ (.height bounds) 2.0)))))))

(defn draw-radar
  [ds w h]
  (set-color color-background)
  (fill-rect 0 0 w h)
  (let [scale (/ (min (- w 60) (- h 60)) 2.0)]
    (translate (/ w 2.0) (/ h 2.0))
    (with-scaling scale
      (set-pen-width 1.5)

      (set-color color-axis)
      (draw-radials (axes ds))
      (set-pen-width 3.0)
      (set-color color-score)
      (draw-radials (points ds))
      (draw-hull ds))
    (set-color color-labels)
    (set-font-size 16.0)
    (draw-labels scale (labels ds))))

(defn make-radar-panel
  [ds width height]
  (doto
      (proxy [javax.swing.JComponent] []
        (paintComponent [g]
                        (with-antialiasing g
                          (draw-radar ds (.getWidth this) (.getHeight this)))))))

(defn show-radar-display-in-frame
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

(defn write-radar-display-to-file
  [ds filename & options]
  (let [{:keys [width height]} (into default-frame-options (apply hash-map options))
        image (BufferedImage. width height BufferedImage/TYPE_INT_RGB)]
    (with-antialiasing (.createGraphics image)
      (draw-radar ds width height))
    (ImageIO/write image "jpeg" (File. filename))))

(def test-data
     '[("Performance" 1 10)
       ("Security" 2 5)
       ("Scalability" 7 10)
       ("Availability" 8 10)
       ("Portability" 5 10)
       ("Modifiability" 3 10)])

(comment
  (show-radar-display test-data))
