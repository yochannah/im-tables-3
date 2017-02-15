(ns im-tables.views.dashboard.manager.columns.main
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [im-tables.components.bootstrap :refer [modal]]
            [inflections.core :refer [plural]]))

(defn tree-node []
  (let [expanded-map (reagent/atom {})]
    (fn [loc class details model current-path selected views]
      (let [attributes  (get details :attributes)
            collections (get details :collections)]
        [:ul.tree-view.list-unstyled.no-select
         (into [:ul.attributes.list-unstyled]
               (map (fn [{:keys [name type]}]
                      (let [original-view? (some? (some #{(conj current-path name)} views))
                            selected?      (some? (some #{(conj current-path name)} selected))]
                        [:li
                         {:on-click (fn [e]
                                      (if-not original-view?
                                        (do
                                          (dispatch [:tree-view/toggle-selection loc (conj current-path name)])))
                                      (.stopPropagation e))}
                         [:span
                          {:class (cond
                                    original-view? "label label-default"
                                    selected? "label label-success disabled")}
                          [:i.fa.fa-tag] name]])) (vals attributes)))
         (into [:ul.collections.list-unstyled]
               (map (fn [{:keys [name referencedType name] :as collection}]
                      (let [referenced-class (get-in model [:classes (keyword referencedType)])]
                        [:li
                         {:on-click (fn [e] (.stopPropagation e) (swap! expanded-map update name not))}
                         [:span [:i.fa.fa-plus-square]
                          (plural (:displayName referenced-class))]
                         (if (get @expanded-map name)
                           [tree-node loc (keyword referencedType) referenced-class model (conj current-path name) selected])]))
                    (sort-by (comp clojure.string/upper-case :referencedType) (vals collections))))]))))

(defn tree-view []
  (fn [loc model query selected]
    (let [views (into #{} (map (fn [v] (apply conj ["Gene"] (clojure.string/split v "."))) (:select query)))]
      [:div
       [tree-node loc (keyword (:from query)) (get-in model [:classes (keyword (:from query))]) model [(:from query)] selected views]])))

(defn add-column-modal [loc]
  (let [model    (subscribe [:assets/model loc])
        selected (subscribe [:tree-view/selection loc])
        query    (subscribe [:main/query loc])]
    (fn [loc]
      [modal
       {:header
         [:h3 "Add Columns"]
        :body
          [:div.modal-body
            [tree-view loc @model @query @selected]]
        :footer
          [:div.btn-toolbar.pull-right
            [:button.btn.btn-default
              {:data-dismiss "modal"}
              "Cancel"]
            [:button.btn.btn-success
              {:data-dismiss "modal"
               :disabled (< (count @selected) 1)
               :on-click (fn [] (dispatch [:tree-view/merge-new-columns loc]))}
              (str "Add " (if (> (count @selected) 0) (str (count @selected) " ")) "columns")]]
            }]
     )))


(defn main []
  (fn [loc]
    [add-column-modal loc]))

;;;;;;;;;;;;;;;;;

; TODO

#_(defn square-node []
  (let [expanded-map (reagent/atom {})]
    (fn [class details model current-path selected views]
      (let [attributes  (get details :attributes)
            collections (get details :collections)]
        [:div
         (into [:div.grid_lg-4]
               (map (fn [[attribute-kw {:keys [name type]}]]
                      (let [original-view? (some? (some #{(conj current-path name)} views))
                            selected?      (some? (some #{(conj current-path name)} selected))]
                        [:div.col.collection
                         {:on-click (fn [e]
                                      (if-not original-view?
                                        (dispatch [:tree-view/toggle-selection (conj current-path name)]))
                                      (.stopPropagation e))}
                         [:span
                          {:class (cond
                                    original-view? "label label-default"
                                    selected? "label label-success disabled")}
                          [:i.fa.fa-tag] name]])) attributes))
         [:h4 "Collections"]
         (into [:div.grid_xs-4]
               (map (fn [{:keys [name referencedType] :as collection}]
                      (let [referenced-class (get model (keyword referencedType))]
                        [:div.col.collection (plural (:displayName referenced-class))])) (vals collections)))]))))

#_(defn square-view []
  (let [model    (subscribe [:assets/model])
        selected (subscribe [:tree-view/selection])
        query    (subscribe [:main/query])]
    (let [views (into #{} (map (fn [v] (apply conj ["Gene"] (clojure.string/split v "."))) (:select @query)))]
      [:div.square-view
       [:button.btn.btn-success
        {:on-click (fn [] (dispatch [:tree-view/merge-new-columns]))}
        (str "Add " (count @selected) " columns")]
       [square-node :Gene (get @model :Gene) @model ["Gene"] @selected views]])))
