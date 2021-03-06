(ns im-tables.views
  (:require [re-frame.core :as re-frame :refer [dispatch]]
            [im-tables.views.core :as main-view]
            [reagent.core :as r]
            [reagent.dom.server :as server]
            [oops.core :refer [ocall]]))

(def some-im-tables-config {:service {:root "https://yeastmine.yeastgenome.org/yeastmine"}
                            :query {:from "Protein"
                                    :select ["symbol"
                                             "secondaryIdentifier"
                                             "primaryIdentifier"]}
                            :settings {:pagination {:limit 10}
                                       :links {:vocab {:mine "BananaMine"}
                                               :url (fn [vocab] (str "#/reportpage/"
                                                                     (:mine vocab) "/"
                                                                     (:class vocab) "/"
                                                                     (:id vocab)))}}})

(def testmine-config {:service {:root "localhost:8080/intermine-demo"}
                      :query {:from "Employee"
                              :select ["name"
                                       "department.name"]}
                      :settings {:pagination {:limit 10}
                                 :links {:vocab {:mine "BananaMine"}
                                         :url (fn [vocab] (str "#/reportpage/"
                                                               (:mine vocab) "/"
                                                               (:class vocab) "/"
                                                               (:objectId vocab)))}}})



; This function is used for testing purposes.
; When using im-tables in real life, you could call the view like so:
; [im-tables.views.core/main {:location ... :service ... :query ...}]


(defn main-panel []
  ; Increase these range to produce N number of tables on the same page
  ; (useful for stress testing)
  (let [number-of-tables 2
        reboot-tables-fn (fn [] (dotimes [n number-of-tables]
                                  (dispatch [:im-tables/load [:test :location n] some-im-tables-config])))]
    (r/create-class
     {:component-did-mount reboot-tables-fn
      :reagent-render (let [show? (r/atom true)]
                        (fn []
                          [:div.container-fluid
                           [:div.container
                            [:div.panel.panel-info
                             [:div.panel-heading (str "Global Test Controls for " number-of-tables " tables")]
                             [:div.panel-body
                              [:div.btn-toolbar
                               [:div.btn-group
                                [:button.btn.btn-default {:on-click reboot-tables-fn}
                                 "Reboot Tables"]]
                               [:div.btn-group
                                [:button.btn.btn-default {:on-click (fn [] (swap! show? not))}
                                 (if @show? "Unmount Tables" "Mount Tables")]]]]]]
                           (when @show?
                             (into [:div]
                                   (->> (range 0 number-of-tables)
                                        (map (fn [n] [main-view/main [:test :location n]])))))]))})))
