(ns cutcoord.advisor
  "Cut Coordination Advisor — proposing a garment cutting workshop
  scheduling/logistics coordination operation (log a work record,
  schedule a crew operation, flag a safety concern, coordinate a
  fabric/pattern-materials supply order) from a crew roster, workshop
  registration and safety-reporting policy. Swappable mock/llm; the
  advisor ONLY proposes — `cutcoord.governor` independently gates
  every proposal and always escalates safety concerns and
  above-threshold supply orders. ISCO-08 7532 (Garment and Related
  Patternmakers and Cutters) operates rotary cutters, cutting
  machines and shears on fabric — a standard workshop cutting-tool
  hazard — so hazard reporting here centers cutting-tool-hazard /
  material-handling-hazard / equipment-condition. The advisor never
  proposes to directly finalize a pattern-cutting-execution decision
  (e.g. deciding a cut is complete/final) or a workshop-safety-
  clearance decision (e.g. declaring the workshop safety-cleared), or
  to override a shop safety officer's judgment — those stay
  permanently out of this actor's scope. This actor coordinates
  WORKSHOP SCHEDULING/LOGISTICS ONLY — it never performs
  pattern-cutting work itself and never makes safety-clearance
  decisions itself. Modeled closely on cloud-itonami-isco-7319's
  craftcoord.advisor (closest published, tested generic workshop
  scheduling/logistics coordination pattern).

  A proposal: {:op :log-work-record|:schedule-crew-operation|
               :flag-safety-concern|:coordinate-supply-order
               :effect :propose :cutter-id str :workshop-id str
               :cost number :hazard-type kw :task str :stake kw
               :confidence n :rationale str}")

(defprotocol Advisor
  (-advise [advisor store request] "request -> proposal map"))

(defn- rationale-for [op cutter-id workshop-id hazard-type]
  (case op
    :log-work-record
    (str "logged work record for cutter " cutter-id " at workshop " workshop-id)

    :schedule-crew-operation
    (str "scheduled crew operation for garment cutting task at workshop " workshop-id)

    :flag-safety-concern
    (str "flagged " (name (or hazard-type :cutting-tool-hazard)) " concern for cutter "
         cutter-id " at workshop " workshop-id " — routed for shop safety officer review")

    :coordinate-supply-order
    (str "coordinated fabric/pattern-materials supply order for cutter " cutter-id
         " at workshop " workshop-id)

    (str "proposed " (name op) " for cutter " cutter-id " at workshop " workshop-id)))

(defn- infer [_store {:keys [op stake cutter-id workshop-id cost hazard-type task]
                       :as request}]
  {:op op
   :effect :propose
   :cutter-id cutter-id
   :workshop-id workshop-id
   :cost cost
   :hazard-type hazard-type
   :task task
   :stake (or stake :low)
   :confidence (case (or stake :low) :high 0.7 :medium 0.85 :low 0.95)
   :rationale (rationale-for op cutter-id workshop-id hazard-type)})

(defn mock-advisor []
  (reify Advisor
    (-advise [_ store request] (infer store request))))

(def ^:private system-prompt
  "You are a garment cutting workshop scheduling/logistics
   coordination advisor covering ISCO-08 7532 (Garment and Related
   Patternmakers and Cutters) — a workshop that operates rotary
   cutters, cutting machines and shears on fabric, a standard
   workshop cutting-tool hazard. Given a request, propose an :op (one
   of :log-work-record, :schedule-crew-operation,
   :flag-safety-concern, :coordinate-supply-order), the :cutter-id,
   :workshop-id, and any :cost/:hazard-type/:task fields, an honest
   :confidence and a :stake. Never propose an op outside this closed
   list, and never propose to directly finalize a
   pattern-cutting-execution decision (e.g. deciding a cut is
   complete/final) or a workshop-safety-clearance decision (e.g.
   declaring the workshop safety-cleared), or to override a shop
   safety officer's judgment — those are always out of this actor's
   scope; it coordinates workshop scheduling/logistics only and never
   performs pattern-cutting work or makes safety-clearance decisions
   itself. Safety concerns always require human sign-off regardless
   of confidence.")

(defn- parse-proposal [content]
  (try
    (let [p (read-string content)]
      (if (map? p)
        (assoc p :effect :propose)
        {:op :unknown :effect :propose :confidence 0.0 :stake :high
         :rationale "unparseable LLM response"}))
    (catch #?(:clj Exception :cljs js/Error) _
      {:op :unknown :effect :propose :confidence 0.0 :stake :high
       :rationale "LLM response parse failure"})))

(defn llm-advisor
  [chat-model model-generate-fn gen-opts]
  (reify Advisor
    (-advise [_ _store request]
      (let [msgs [{:role :system :content system-prompt}
                  {:role :user :content (str "operation request: " (pr-str request))}]
            resp (model-generate-fn chat-model msgs gen-opts)]
        (parse-proposal (:content resp))))))
