# Staff-Level Prep — system design + leadership signal

Everything else in this repo is coding-round prep. A Staff loop usually adds one or two
**system design** rounds and one or two **behavioral/leadership** rounds — and typically
weights them *higher* than the coding round. This file won't manufacture years of scope
and impact overnight; no document can. What it can do in one evening: give you a
repeatable script for the design round so you never freeze on structure, a vocabulary of
building blocks so you're not deriving them live, and a framework to get your *real*
stories out fast for the behavioral round. Bring your own experience — this is the
scaffolding, not the substance.

## Part 1 — System Design

### The script (run this in every design interview, in order)

1. **Clarify requirements** — functional (what must it do) and non-functional (scale,
   latency, consistency needs). Ask, don't assume. ~2-3 min. Interviewers actively judge
   whether you ask at all.
2. **Estimate scale** — users, QPS, storage, bandwidth (see the math below). This is what
   forces every later decision — "1M users" and "1B users" are different systems.
3. **Define the API** — 3-5 key endpoints/methods with inputs and outputs. Grounds the
   rest of the discussion in something concrete.
4. **High-level design** — boxes and arrows: client, load balancer, service(s), cache,
   database, queue. Get *something* up before going deep on any one box.
5. **Deep dive** — the interviewer will steer you, or you pick the hardest 1-2
   components (usually the data model or the main scaling bottleneck). This is where
   Staff-level judgment shows: name the tradeoff explicitly, pick a side, justify it in
   one sentence. Don't just list options — commit to one.
6. **Bottlenecks & failure modes** — single point of failure, hot shard/key, thundering
   herd, cache stampede, retry storms. Raise these yourself before being asked.
7. **Wrap up** — summarize the tradeoffs you made and what you'd revisit with more time
   or more information.

### Back-of-envelope numbers

Approximate, order-of-magnitude — precision doesn't matter, having *a* number does:

| Quantity | Value |
|---|---|
| Seconds per day | ~86,400 (~10^5) |
| 1M requests/day | ~12 req/sec average |
| Peak QPS vs average | ~2-3x average, as a rule of thumb |
| 1 KB × 1M | ~1 GB |
| Single app server | ~1K-10K QPS (depends on work per request) |
| Single DB, simple indexed query | ~1K-10K QPS |

Classic latency numbers (order of magnitude, not exact):

| Operation | Latency |
|---|---|
| L1 cache reference | ~1 ns |
| Main memory reference | ~100 ns |
| Send 1 KB over 1 Gbps network | ~10 µs |
| Read 4 KB randomly from SSD | ~150 µs |
| Round trip inside same datacenter | ~0.5 ms |
| Read 1 MB sequentially from disk | ~20 ms |
| Round trip, cross-continent | ~150 ms |

The one-line takeaway to say out loud: **memory and same-datacenter calls are cheap;
disk seeks and cross-region calls are the expensive things to design around.**

### Building-block glossary — what to reach for, when

| Need | Reach for | One-liner |
|---|---|---|
| Spread traffic across servers | Load balancer (L4 fast/dumb, L7 smart/slower) | L7 can route by URL/header and terminate SSL; L4 just forwards packets. |
| Serve static/media content fast globally | CDN | Cache content near the user; cuts latency and origin load. |
| Reduce repeated expensive reads | Cache (cache-aside is the default answer) | App checks cache first, falls back to DB, populates cache on miss. Evict via LRU or TTL — [same idea as `LinkedHashMap` here](02-hashing/01-hashmap/). |
| Absorb spikes / decouple services | Message queue (Kafka for log/replay, SQS/RabbitMQ for simpler pub-sub) | Producer doesn't wait on consumer; also gives you retry and backpressure. |
| Split data across many machines | Sharding/partitioning (hash-based or range-based) | The hard parts to mention: hot shards, and resharding when you add nodes. |
| Rebalance shards without reshuffling everything | Consistent hashing | Adding/removing one node only remaps ~1/N of the keys, not all of them. |
| Choose a database | SQL (transactions, joins, strong schema) vs NoSQL (horizontal scale, flexible schema, specific access pattern) | Name the access pattern first, then the DB — never the reverse. |
| Survive a node failure / scale reads | Replication (leader-follower is the default answer) | Multi-leader/leaderless buys write availability across regions at the cost of conflict resolution. |
| Limit abuse / protect downstream | Rate limiting — **token bucket** is the default good answer | Allows controlled bursts up to the bucket size, refills at a fixed rate. Leaky bucket smooths instead; fixed/sliding window counters are the simpler-but-leakier alternatives. |
| Reasoning about a network partition | CAP theorem | During a partition you pick Consistency or Availability, not both. Most consumer-facing systems pick AP; payments/inventory usually pick CP. |
| Safe retries on writes | Idempotency keys | Client attaches a unique key per logical operation; server dedupes retries of the same key. |

### Classic problems — the one key insight each

Not full designs — just the thing that, if you say it, signals you've actually thought
about this problem before:

- **URL shortener** — base62-encode an auto-incrementing ID (or hash + collision check).
  Read-heavy: cache + CDN in front, 301/302 redirect.
- **Rate limiter** — token bucket counters live in a shared store (Redis), not per-server
  memory, or every server enforces its own separate limit. Use a Lua script/atomic op to
  avoid a check-then-increment race.
- **News feed / timeline** — fan-out-on-write (push to followers' feeds) for normal
  users; fan-out-on-read (pull + merge at read time) for celebrities with huge follower
  counts. The hybrid is the "I've thought about this" answer.
- **Chat / messaging** — WebSockets for live delivery, a queue for offline delivery,
  separate the append-only message store from the presence/connection service. Order
  messages with a per-conversation sequence number, not wall-clock time.
- **Distributed cache / key-value store** — consistent hashing to place keys,
  replication for durability, tunable read/write quorums if you want Dynamo-style
  consistency knobs.
- **Notification system** — queue + worker pool, one adapter per channel (push/email/
  SMS), dedupe and per-user rate-limit before sending, exponential backoff + dead-letter
  queue on failure.
- **Web crawler** — BFS from seed URLs; a URL frontier queue with per-domain politeness
  (rate limit per host); dedupe visited URLs with a Bloom filter at scale.
- **Distributed unique ID generator** — Twitter Snowflake pattern: pack
  timestamp + machine-id + sequence into one 64-bit long. Sortable, no central
  coordinator needed.

## Part 2 — Leadership & Behavioral

### The framework: STAR-L

**S**ituation, **T**ask, **A**ction, **R**esult, **L**earning. The **L** is what
separates a Senior answer from a Staff one — the reflection: what you generalized from
it, what you'd do differently, how it changed your approach on the next project.
Interviewers are listening for evidence of *judgment*, not just a good outcome.

Keep Result concrete wherever you can — a number, a before/after, an org-level change —
not just "it went well."

### Staff-specific themes — you need one real story per row

Staff behavioral questions probe **scope and influence**, not just individual execution.
For each row below, have one real example ready — it's fine, and efficient, to reuse the
same strong story for 2-3 different rows.

| Theme | The question is really asking |
|---|---|
| Influence without authority | Did you get another team to change direction without being their manager? |
| Driving technical strategy | Did you set a direction for more than your own team, and why that one? |
| Technical disagreement / disagree-and-commit | Can you hold a strong position, lose the argument gracefully, and still execute? |
| Mentoring / multiplying others | Did you make other engineers better, not just ship code yourself? |
| A failure or a time you were wrong | Do you own mistakes, and what actually changed afterward? |
| Navigating ambiguity | Given a vague problem, how did you scope it yourself? |
| Technical debt vs. velocity | Can you defend a prioritization call with reasoning, not just an opinion? |
| Cross-team conflict over priorities/resources | How do you resolve conflict without escalating every time? |
| Pushing back on leadership/deadlines | Can you say no upward, backed by data? |

### Story bank template

Fill this in tonight with your **own** real examples — 5-6 rows is usually enough to
cover every theme above through reuse.

| Story (short name) | Situation (1 line) | Your specific action | Result (numbers if you have them) | Themes it covers |
|---|---|---|---|---|
| | | | | |
| | | | | |
| | | | | |
| | | | | |
| | | | | |

## Time-boxed plan for the rest of today

- **System design** (1-1.5 hr): read Part 1 once, then pick 2 of the classic problems
  above and actually talk through the 7-step script out loud, from memory, without
  looking.
- **Behavioral** (1-1.5 hr): fill in the story bank table with real examples. Say each
  one out loud once — STAR-L, under 2 minutes.
- **Coding** (remaining time): [templates.md](templates.md) for recall speed, plus
  whichever [ROADMAP.md](ROADMAP.md) self-tests you can't answer cleanly yet. Don't
  reread topics you already have cold.
- **Sleep** — genuinely part of the plan, not a throwaway line. Fatigue costs more
  points than one extra hour of review does, at every one of these rounds.
