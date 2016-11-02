(ns im-tables.views.dashboard.save
  (:require [re-frame.core :refer [dispatch subscribe]]))

(defn main []
  (fn []
    [:div.dropdown
     [:button.btn.btn-primary.dropdown-toggle
      {:type "button"
       :data-toggle "dropdown"}
      "Save"]
     [:ul.dropdown-menu
      [:li [:a "A"]]
      [:li [:a "B"]]]]))