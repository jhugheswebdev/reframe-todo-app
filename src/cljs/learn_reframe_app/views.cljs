(ns learn-reframe-app.views
  (:require
   [re-frame.core :as re-frame]
   [learn-reframe-app.subs :as subs]
   [reagent.core :as r]))
;
;(defn simple-component []
;  [:div
;   [:p "Component here"]
;   [:p.someclass
;    "I have " [:strong "bold"]
;    [:span {:style {:color "red"}} " and red "] "text."]])
;
;(defn simple-parent []
;  [:div
;   [:p "I include simple-component."]
;   [simple-component]])
;
;(defn hello-component [name]
;  [:p "Hello, " name "!"])
;
;(defn say-hello []
;  [hello-component "world"])
;
;(defn lister [items]
;  [:ul
;   (for [item items]
;     ^{:key item} [:li "Item" item])])
;
;(defn lister-user []
;  [:div
;   "Here is a list:"
;   [lister (range 7)]])
;
;(def click-count (r/atom 0))
;
;(defn counting-component []
;  [:div
;   "The atom " [:code "click-count"] " has value: "
;   @click-count ". "
;   [:input {:type "button" :value "Click me!"
;            :on-click #(swap! click-count inc)}]])
;
;(defn timer-component []
;  (let [seconds-elapsed (r/atom 0)]
;    (fn []
;      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
;      [:div
;       "Seconds Elapsed: " @seconds-elapsed])))
;
;(defn atom-input [value]
;  [:input {:type "text"
;           :value @value
;           :on-change #(reset! value (-> % .-target .-value))}])
;
;(defn shared-state []
;    (let [val (r/atom "foo")]
;      (fn []
;        [:div
;         [:p "The Value is now: " @val]
;         [:p "Change it here: " [atom-input val]]])))
;
;(defn render-simple []
;  (r/render [simple-component]
;            (.-body js/document)))


;BMI calculator

(def bmi-data (r/atom {:height 180 :weight 80}))

(defn calc-bmi []
  (let [{:keys [height weight bmi] :as data} @bmi-data
        h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))

(defn slider [param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! bmi-data assoc param (.. e -target -value))
                        (when (not= param :bmi)
                          (swap! bmi-data assoc :bmi nil)))}])

(defn bmi-component []
  (let [{:keys [weight height bmi]} (calc-bmi)
        [color diagnose] (cond
                           (< bmi 18.5) ["orange" "Underweight"]
                           (< bmi 25) ["inherit" "Normal"]
                           (< bmi 30) ["orange" "OVERWEIGHT"]
                           :else ["red" "obese"])]

    [:div
     [:h3 "BMI Calculator"]
     [:div
      "Height: " (int height) "cm"
      [slider :height height 100 220]]
     [:div
      "Weight: " (int weight) "kg"
      [slider :weight weight 30 150]]
     [:div
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      [slider :bmi bmi 10 50]]]))

;Timer

(defonce timer (r/atom (js/Date.)))

(defonce time-color (r/atom "#f34"))

(defonce time-updater (js/setInterval
                        #(reset! timer (js/Date.)) 1000))

(defn greeting [message]
  [:h1 message])

(defn clock []
  (let [time-str (-> @timer .toTimeString (clojure.string/split " ") first)]
    [:div.example-clock
     {:style {:color @time-color}}
     time-str]))

(defn color-input []
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value @time-color
            :on-change #(reset! time-color (-> % .-target .-value))}]])

(defn simple-example []
  [:div
   [greeting "Hello world, it is now"]
   [clock]
   [color-input]])

;(defn ^:export run []
;  (r/render [simple-example]
;            (js/document.getElementById "app")))

;Todos

(defonce todos (r/atom (sorted-map)))

(defonce counter (r/atom 0))

(defn add-todo [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :title text :done false})))

(defn toggle [id] (swap! todos update-in [id :done] not))
(defn save [id title] (swap! todos assoc-in [id :title] title))
(defn delete [id] (swap! todos dissoc id))

(defn mmap [m f a] (->> m (f a) (into (empty m))))
(defn complete-all [v] (swap! todos mmap map #(assoc-in % [1 :done] v)))
(defn clear-done [] (swap! todos mmap remove #(get-in % [1 :done])))

(defonce init (do
                (add-todo "Rename Cloact to Reagent")
                (add-todo "Add undo demo")
                (add-todo "Make all rendering async")
                (add-todo "Allow any arguments to component functions")
                (complete-all true)))

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val (r/atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (if-not (empty? v) (on-save v))
                (stop))]
    (fn [{:keys [id class placeholder]}]
      [:input {:type "text" :value @val
               :id id :class class :placeholder placeholder
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(defn todo-edit [](with-meta todo-input
                             {:component-did-mount #(.focus (r/dom-node %))}))

(defn todo-stats [{:keys [filt active done]}]
  (let [props-for (fn [name]
                    {:class (if (= name @filt) "selected")
                     :on-click #(reset! filt name)})]

    [:div
     [:span#todo-count
      [:strong active] " " (case active 1 "item" "items") "left"]
     [:ul#filters
      [:li [:a (props-for :all) "All"]]
      [:li [:a (props-for :active) "Active"]]
      [:li [:a (props-for :done) "Completed"]]]
     (when (pos? done)
       [:button#clear-completed {:on-click clear-done}
        "Clear completed" done])]))

(defn todo-item []
  (let [editing (r/atom false)]
    (fn [{:keys [id done title]}]
      [:li {:class (str (if done "Completed")
                        (if @editing "Editing"))}
       [:div.view
        [:input.toggle {:type "checkbox" :checked done
                        :on-change #(toggle id)}]
        [:label {:on-double-click #(reset! editing true)} title]
        [:button.destroy {:on-click #(delete id)}]]
       (when @editing
         [todo-edit {:class "edit" :title title
                     :on-save #(save id %)
                     :on-stop #(reset! editing false)}])])))

(defn todo-app [props]
  (let [filt (r/atom :all)]
    (fn []
      (let [items (vals @todos)
            done (->> items (filter :done) count)
            active (- (count items) done)]
        [:div
         [:section#todoapp
          [:header#header
           [:h1 "Todos"]
           [todo-input {:id "new-todo"
                        :placeholder "What needs to be done?"
                        :on-save add-todo}]]
          (when (-> items count pos?)
            [:div
             [:section#main
              [:input#toggle-all {:type "checkbox" :checked (zero? active)
                                  :on-change #(complete-all (pos? active))}]
              [:label {:for "toggle-all"} "Mark all as complete"]
              [:ul#todo-list
               (for [todo (filter (case @filt
                                    :active (complement :done)
                                    :done :done
                                    :all identity) items)]
                 ^{:key (:id todo)} [todo-item todo])]]
             [:footer#footer
              [todo-stats {:active active :done done :filt filt}]]])]
         [:footer#info
          [:p "Double click to edit a todo"]]]))))

(defn ^:export run []
  (r/render [todo-app]
            (js/document.getElementById "app")))



;; home

(defn home-panel []
  [:div.container
   [todo-app]])









;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
