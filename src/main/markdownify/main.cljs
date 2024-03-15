(ns markdownify.main
  (:require [reagent.core :as reagent]
            ["showdown" :as showdown]))

(defonce markdown-converter (showdown/Converter.))

(defonce flash-message (reagent/atom nil))
(defonce flash-timeout (reagent/atom nil))

(defn flash
  ([text]
   (flash text 3000))
  ([text ms]
   (js/clearTimeout @flash-timeout)
   (reset! flash-message text)
   (reset! flash-timeout
           (js/setTimeout #(reset! flash-message nil) ms))))

(defn md->html [md]
  (.makeHtml markdown-converter md))

(defn html->md [html]
  (.makeMarkdown markdown-converter html))

(defonce text-state (reagent/atom {:format :md
                                   :value ""}))

(defn ->md [{:keys [format value]}]
  (case format
    :md value
    :html (html->md value)))

(defn ->html [{:keys [format value]}]
  (case format
    :md (md->html value)
    :html value))

(defn copy-to-clipboard [s]
  ;; https://medium.com/hackernoon/copying-text-to-clipboard-with-javascript-df4d4988697f
  (let [el (.createElement js/document "textarea")
        selected (when (pos? (-> js/document .getSelection .-rangeCount))
                   (-> js/document .getSelection (.getRangeAt 0)))]
    (set! (.-value el) s)
    (.setAttribute  el "readonly" "")
    (set! (-> el .-style .-position) "absolute")
    (set! (-> el .-style .-left) "-9999px")
    (-> js/document .-body (.appendChild el))
    (.select el)
    (.execCommand js/document "copy")
    (-> js/document .-body (.removeChild el))
    (when selected
      (-> js/document .getSelection .removeAllRanges)
      (-> js/document .getSelection (.addRange selected)))))

(defn app []
  [:div
   [:div
    {:style {:position :absolute
             :text-align :cente             :left 0
             :right 0
             :background-color :yellow
             :max-width 250
             :padding "1em"
             :z-index 100 ;; To appear above everyting else
             :margin :auto
             :border-radius 10
             :transform (if @flash-message
                          "scaleY(1)"
                          "scaleY(0)")
             :transition "transform 0.2s ease-out"}}
    @flash-message]
   [:h1 "Markdownify"]
   (comment
     [:div
      [:pre [:code @flash-message]]])
   [:div
    {:style {:display :flex}}
    [:div
     {:style {:flex "1"}}
     [:h2 "Markdown"]
     [:textarea
      {:on-change (fn [e]
                    (reset! text-state {:format :md
                                        :value (-> e .-target .-value)}))
       :value (->md @text-state)
       :style {:resize "none"
               :height "500px"
               :width "100%"}}]
     [:button
      {:on-click (fn []
                   (copy-to-clipboard (->md @text-state))
                   (flash "Markdown copied to clipboard!"))
       :style {:background-color :green
               :padding "1em"
               :color :white
               :border-radius 10}}
      "Copy Markdown"]]
    [:div
     {:style {:flex "1"}}
     [:h2 "HTML"]
     [:textarea
      {:on-change (fn [e]
                    (reset! text-state {:format :html
                                        :value (-> e .-target .-value)}))
       :value (->html @text-state)
       :style {:resize "none"
               :height "500px"
               :width "100%"}}]
     [:button
      {:on-click (fn []
                   (copy-to-clipboard (->html @text-state))
                   (flash "HTML copied to clipboard!"))
       :style {:background-color :green
               :padding "1em"
               :color :white
               :border-radius 10}}
      "Copy HTML"]]
    [:div
     {:style {:flex "1"
              :padding-left "2em"}}
     [:h2 "HTML Preview"]
     [:div
      {:style {:height "500px"}
       :dangerouslySetInnerHTML {:__html (->html @text-state)}}]]]])

(defn mount! []
  (reagent/render [app]
                  (.getElementById js/document "app")))

(defn main! []
  (mount!))

(defn reload! []
  (mount!))
