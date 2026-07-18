(ns cutcoord.store
  "SSoT for the ISCO-08 7532 garment cutting workshop
  scheduling/logistics coordination actor (itonami actor pattern,
  ADR-2607121000 / CLAUDE.md Actors section; README's 'Robotics
  premise' — a workshop scheduling/logistics coordination robot
  performs crew scheduling, job/pattern/progress-record logging and
  fabric/pattern-materials supply-order coordination for a garment
  cutting workshop under this advisor/governor pair, which never
  dispatches hardware itself, never performs pattern-cutting work
  itself, and never finalizes a pattern-cutting-execution decision or
  a workshop-safety-clearance decision, nor overrides a shop safety
  officer's judgment — those remain the shop safety officer's
  exclusive judgment). ISCO-08 7532 (Garment and Related
  Patternmakers and Cutters) operates rotary cutters, cutting
  machines and shears on fabric — a standard workshop cutting-tool
  hazard — so this store's domain shape centers a job/pattern record
  and a cutting-tool-hazard-aware safety concern, not a
  quality-clearance record. Modeled closely on
  cloud-itonami-isco-7319's craftcoord.store (closest published,
  tested generic workshop scheduling/logistics coordination pattern).

  Domain:

    cutter   — a registered garment cutting workshop crew member
               (patternmaker/cutter role; :cutter-id, :name)
    workshop — a registered garment cutting workshop site
               {:workshop-id :name :max-supply-cost}. `:max-supply-cost`
               is an informational registered ceiling used only to
               decide whether a `:coordinate-supply-order` proposal
               escalates to human sign-off (the governor never blocks
               a within-threshold order outright; it only decides
               commit vs. escalate).
    record   — a committed operating record (a logged job/pattern/
               progress entry, a scheduled crew/task operation, a
               flagged safety concern, or a coordinated fabric/
               pattern-materials supply order) — written ONLY via
               commit-record!.
    ledger   — append-only audit trail, commit or hold.")

(defprotocol Store
  (cutter [s cutter-id])
  (workshop [s workshop-id])
  (records-of [s cutter-id])
  (ledger [s])
  (register-cutter! [s c])
  (register-workshop! [s w])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (cutter [_ cutter-id] (get-in @a [:cutters cutter-id]))
  (workshop [_ workshop-id] (get-in @a [:workshops workshop-id]))
  (records-of [_ cutter-id] (filter #(= cutter-id (:cutter-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-cutter! [s c]
    (swap! a assoc-in [:cutters (:cutter-id c)] c) s)
  (register-workshop! [s w]
    (swap! a assoc-in [:workshops (:workshop-id w)] w) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:cutters {} :workshops {} :records [] :ledger []}
                                    seed)))))
