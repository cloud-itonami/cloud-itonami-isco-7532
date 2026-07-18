# Security Policy

This project handles garment cutting workshop operating workflows. Treat
vulnerabilities as potentially high impact even when the demo data is
synthetic — this domain's failure modes include physical worker-safety
risk from rotary-cutter/cutting-machine/shears injury and fabric
material-handling hazards.

## Do Not Disclose Publicly

Report privately before opening public issues for:

- credential exposure
- real cutter, workshop or operator data exposure
- authorization bypass
- CutCoordGovernor bypass
- audit-ledger tampering
- over-disclosure in reports or exports
- unsafe robot action dispatch
- any path that lets a proposal reach a pattern-cutting-execution
  decision, a workshop-safety-clearance decision, or a
  shop-safety-officer-override decision

## Reporting

Use GitHub private vulnerability reporting when available for the repository.
If that is unavailable, contact the repository maintainers through the
cloud-itonami organization before publishing details.

Include:

- affected commit or version
- reproduction steps
- expected and actual behavior
- impact on cutter/workshop data, policy enforcement or audit logging
- suggested fix, if known

## Production Guidance

- Store secrets outside Git.
- Keep real cutter/workshop/operator data outside this repository.
- Run policy tests before deployment.
- Export and review audit logs regularly.
- Use least privilege for operators and service accounts.
