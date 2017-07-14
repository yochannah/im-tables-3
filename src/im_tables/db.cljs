(ns im-tables.db)

(def subclassquery
  {:description "Returns MP terms whose names match the specified search terms."
   :tags ["im:aspect:Phenotype" "im:frontpage" "im:public"]
   :where [{:path "MPTerm.obsolete"
            :op "="
            :code "B"
            :editable "false"
            :switchable "false"
            :switched "LOCKED"
            :value "false"}
           {:path "MPTerm.name"
            :op "CONTAINS"
            :code "A"
            :editable "true"
            :switchable "false"
            :switched "LOCKED"
            :value "hemoglobin"}]
   :name "Lookup_MPhenotype"
   :title "Lookup --> Mammalian phenotypes (MP terms)"
   :constraintLogic "A and B"
   :select ["MPTerm.name" "MPTerm.identifier" "MPTerm.description"]
   :orderBy [{:MPTerm.name "ASC"}]
   :model {:name "genomic"}})

(def outer-join-query {:from "Gene"
                       :select ["secondaryIdentifier"
                                "symbol"
                                "primaryIdentifier"
                                "organism.name"
                                "publications.firstAuthor"
                                "publications.title"
                                "publications.year"
                                "publications.journal"
                                "publications.volume"
                                "publications.pages"
                                "publications.pubMedId"]
                       :joins ["publications"]
                       :size 10
                       :where [{:path "secondaryIdentifier"
                                :op "="
                                :value "AC3*"
                                :code "A"}]})

(def outer-join-query-2 {:from "Gene"
                         :select ["secondaryIdentifier"
                                  "symbol"
                                  "primaryIdentifier"
                                  "organism.name"
                                  "homologues.homologue.primaryIdentifier"
                                  "homologues.homologue.organism.shortName"
                                  "homologues.type"]
                         :joins ["homologues"]
                         :size 10
                         :where [{:path "secondaryIdentifier"
                                  :op "="
                                  :value "AC3*"
                                  :code "A"}]})

(def default-db
  {;:service  {:root "www.flymine.org/query"}
   ;:query    {:from   "Gene"
   ;           :size   10
   ;           :select ["secondaryIdentifier"
   ;                    "symbol"
   ;                    "primaryIdentifier"
   ;                    "organism.name"
   ;                    "homologues.homologue.symbol"]
   ;           :where  [{:path  "Gene"
   ;                     :op    "IN"
   ;                     :value "esyN demo list"}
   ;                    {:path  "Gene.symbol"
   ;                     :op    "="
   ;                     :value "*a*"
   ;                     :code  "B"}]}

   :settings {:buffer 2
              :pagination {:start 0
                           :limit 20}
              :links {:vocab {:mine "flymine"}
                      :on-click nil
                      :url (fn [vocab] (str "#/reportpage/"
                                            (:mine vocab) "/"
                                            (:class vocab) "/"
                                            (:objectId vocab)))}}
   :cache {:summaries {}
           :summary {}
           :selection {}
           :overlay? false
           :filters {}
           :tree-view {:selection #{}}}})
