# cloud-itonami-isco-7532

Open Occupation Blueprint for **ISCO-08 7532**: Garment and Related Patternmakers and Cutters.

This repository designs a forkable OSS business for a garment cutting workshop scheduling and logistics coordination practice: a workshop scheduling and supply-coordination robot manages crew/task records under a governor-gated actor, so a garment cutting workshop crew keeps its own operating records instead of renting a closed workforce-management SaaS.

ISCO-08 7532 covers patternmakers and cutters who operate rotary cutters, cutting machines and shears on fabric — a standard workshop cutting-tool hazard. Safety-concern reporting in this actor is therefore centered on cutting-tool hazards (in addition to general material-handling and equipment-condition concerns), rather than a generic residual hazard set.

**Maturity: `:implemented`.** `src/cutcoord/` implements the
`CutCoordActor` as a `langgraph.graph/state-graph`
(`cutcoord.actor`) wired to a `Cut Coordination Advisor`
(`cutcoord.advisor`) and an independent `CutCoordGovernor`
(`cutcoord.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt)
+-> :hold (:hard?)`. 25 tests / 55 assertions green (`clojure -M:test`).
HARD invariants (always hold, never
overridable): cutter provenance, workshop provenance, no-actuation
(`:effect` must be `:propose`), a closed op-allowlist
(`:log-work-record`, `:schedule-crew-operation`,
`:flag-safety-concern`, `:coordinate-supply-order` — nothing else may
ever be proposed), and a permanent, unconditional block on any
proposal that would directly finalize a pattern-cutting-execution
decision (e.g. deciding a cut is complete/final) or a
workshop-safety-clearance decision (e.g. declaring the workshop
safety-cleared), or override a shop safety officer's
judgment. Always-escalate paths (human sign-off regardless of
confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)):
`:flag-safety-concern` (always) and `:coordinate-supply-order` above
the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a workshop scheduling/logistics coordination robot performs crew scheduling, job/pattern/progress-record logging and fabric/pattern-materials supply-order coordination for a garment cutting workshop crew, under an actor that proposes actions and an independent **Cut Coordination Governor** that gates them. The governor never
dispatches hardware itself, never performs pattern-cutting work on the shop floor, and never finalizes a pattern-cutting-execution decision or a workshop-safety-clearance decision, nor overrides a shop safety officer's judgment; `:high`/`:safety-critical` actions (such as a flagged cutting-tool-hazard/material-handling/equipment-condition concern, or an above-threshold supply order) require human sign-off. **This actor coordinates workshop scheduling/logistics only — it never performs pattern-cutting work or makes safety-clearance decisions itself.**

## Core Contract

```text
crew roster + workshop registration + safety-reporting policy
        |
        v
Cut Coordination Advisor -> CutCoordGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, finalize
a pattern-cutting-execution decision, declare a workshop safety-clearance,
override a shop safety officer's judgment, suppress an operating record, or
disclose sensitive data without governor approval and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7532`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
