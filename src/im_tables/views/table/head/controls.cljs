(ns im-tables.views.table.head.controls
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [im-tables.views.graphs.histogram :as histogram]
            [oops.core :refer [oget ocall!]]))


(defn filter-input []
  (fn [view val]
    [:input.form-control
     {:type      "text"
      :value     val
      :on-change (fn [e]
                   (dispatch [:select/set-text-filter
                              view
                              (oget e :target :value)]))}]))

(defn force-close
  "Force a dropdown to close "
  [component]
  (-> (js/$ (reagent/dom-node component))
      (ocall! "closest" ".dropdown-menu")
      (ocall! "parent")
      (ocall! "removeClass" "open")))


(defn has-text?
  "Return true if a label contains a string"
  [needle haystack]
  (if needle
    (if-let [text-to-search (:item haystack)]
      (re-find (re-pattern (str "(?i)" needle)) (:item haystack))
      false)
    true))

(defn constraint-has-path? [view constraint]
  (= view (:path constraint)))


(defn constraint-dropdown []
  (fn [{:keys [value on-change]}]
    [:select.form-control
     {:value     (if value value "=")
      :on-change (fn [e] (on-change {:op (.. e -target -value)}))}
     [:option {:value ">"} "greater than"]
     [:option {:value "<"} "less than"]
     [:option {:value "="} "equal to"]
     [:option {:value "ONE OF"} "one of"]]))

(defn constraint-text []
  (fn [{:keys [value on-change]}]
    [:input.form-control {:type      "text"
                          :on-change (fn [e] (on-change {:value (.. e -target -value)}))
                          :value     value}]))

(defn blank-constraint [path]
  (let [state (reagent/atom {:path path :op "=" :value nil})]
    (fn []
      [:div.container-fluid
       [:div.row
        [:div.col-xs-4
         [constraint-dropdown
          {:value     (:op @state)
           :on-change (fn [v] (swap! state assoc :op (:op v)))}]]
        [:div.col-xs-6
         [:input.form-control {:type "text"
                               :value (:value @state)
                               :on-change (fn [e] (swap! state assoc :value (.. e -target -value)))}]]
        [:div.col-xs-2
         [:button.btn.btn-success
          {:on-click (fn [] (dispatch
                              [:filters/add-constraint @state]
                              (reset! state {:path path :op "=" :value nil})))
           :type     "button"} [:i.fa.fa-plus]]]]])))

(defn constraint []
  (fn [{:keys [path op value code] :as const}]
    (letfn [(on-change [new-value] (dispatch [:filters/update-constraint (merge const new-value)]))]
      [:div.container-fluid
       [:div.row
        [:div.col-xs-4
         [constraint-dropdown {:value     op
                               :on-change on-change}]]
        [:div.col-xs-6
         [constraint-text {:value     value
                           :on-change on-change}]]
        [:div.col-xs-2
         [:button.btn.btn-danger
          {:on-click (fn [] (dispatch [:filters/remove-constraint const]))
           :type     "button"} [:i.fa.fa-times]]]]])))

(defn filter-view [view]
  (let [response   (subscribe [:selection/response view])
        selections (subscribe [:selection/selections view])
        query      (subscribe [:main/temp-query view])]
    (fn [view]
      [:div
       [:div.alert.alert-success
        [:div.container-fluid
         [:form.form
          [:h4 "Filters"]
          (into [:div] (map (fn [c] [constraint c]) (filter (partial constraint-has-path? view) (:where @query))))]]]
       [:div.alert.alert-default
        [:div.container-fluid
         [:h4 "Add..."]
         [blank-constraint view]]]
       [:div.container-fluid
        [:div.btn-toolbar.pull-right
         [:button.btn.btn-default
          {:type        "button"
           :data-toggle "dropdown"} "Cancel"]
         [:button.btn.btn-primary
          {:type        "button"
           :data-toggle "dropdown"
           :on-click    (fn [] (dispatch [:filters/save-changes]))} "Apply"]]]])))

(defn column-summary [view]
  (let [response    (subscribe [:selection/response view])
        selections  (subscribe [:selection/selections view])
        text-filter (subscribe [:selection/text-filter view])]
    (reagent/create-class
      {:component-will-mount
       (fn [])
       :component-will-update
       (fn [])
       :reagent-render
       (fn [view]
         (let [close-fn (partial force-close (reagent/current-component))]
           [:form.form.min-width-275
            [histogram/main (:results @response)]
            [filter-input view @text-filter]
            [:div.max-height-400
             [:table.table.table-striped.table-condensed
              [:thead [:tr [:th] [:th "Item"] [:th "Count"]]]
              (into [:tbody]
                    (->> (filter (partial has-text? @text-filter) (:results @response))
                         (map (fn [{:keys [count item]}]
                                [:tr.hoverable
                                 {:on-click (fn [e] (dispatch [:select/toggle-selection view item]))}
                                 [:td [:div
                                       [:label
                                        [:input
                                         {:on-change (fn [])
                                          :checked   (contains? @selections item)
                                          :type      "checkbox"}]]]]
                                 [:td (if item item [:i.fa.fa-ban.mostly-transparent])]
                                 [:td
                                  [:div count]]]))))]]
            [:div.btn-toolbar
             [:button.btn.btn-primary
              {:type     "button"
               :on-click (fn []
                           (dispatch [:main/apply-summary-filter view])
                           (close-fn))}
              [:span
               [:i.fa.fa-filter]
               (str " Filter (" (count (keys @selections)) ")")]]
             (if (empty? @selections)
               [:button.btn.btn-default
                {:type     "button"
                 :on-click (fn [] (dispatch [:select/select-all view]))}
                [:span [:i.fa.fa-check-square-o] " All"]]
               [:button.btn.btn-default
                {:type     "button"
                 :disabled (empty? @selections)
                 :on-click (fn [] (dispatch [:select/clear-selection view]))}
                [:span [:i.fa.fa-square-o] " Clear"]])]]))})))

(defn toolbar []
  (fn [view]
    [:div.summary-toolbar
     [:i.fa.fa-sort
      {:on-click (fn [] (dispatch [:main/sort-by view]))}]
     [:i.fa.fa-times
      {:on-click (fn [] (dispatch [:main/remove-view view]))}]
     [:span.dropdown
      [:i.fa.fa-filter.dropdown-toggle
       {:on-click    (fn [] (dispatch [:main/set-temp-query]))
        :data-toggle "dropdown"}]
      [:div.dropdown-menu
       {:style {:min-width "400px"}}
       [filter-view view]]]
     [:span.dropdown
      [:i.fa.fa-bar-chart.dropdown-toggle {:data-toggle "dropdown"}]
      [:div.dropdown-menu
       {:style {:min-width "400px"}}
       [column-summary view]]]]))

(defn main []
  (fn [view]
    [:div
     [toolbar view]]))