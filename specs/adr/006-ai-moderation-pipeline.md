# ADR-006: AI-Powered Content Moderation Pipeline

**Status**: Accepted
**Date**: 2026-04-05
**Decision Makers**: Core Team

## Context
A mental health forum requires content moderation that is both sensitive to the domain (people discussing difficult emotions is expected and healthy) and vigilant for genuinely harmful content (harassment, misinformation) and crisis situations (suicidal ideation).

## Decision
Implement a **phased moderation pipeline**: keyword-based in Phase 1, AI-powered (Claude API) in Phase 2, with human moderator review always in the loop.

## Pipeline Architecture

```
User Post → [Publish Event] → Moderation Service
                                    │
                          ┌─────────┴─────────┐
                          │ Phase 1: Keywords  │
                          │ Phase 2: Claude AI │
                          └─────────┬─────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                  SAFE         NEEDS_REVIEW      CRISIS/TOXIC
                    │               │               │
              (no action)    → Mod Queue       → Immediate Action
                                    │               │
                              Human Review     Hide + Alert Mods
                                                + Crisis Resources
```

## Why Claude API for AI Moderation
- **Context sensitivity**: Claude understands that "I've been struggling with suicidal thoughts" is a cry for help, not harmful content
- **Nuance**: Can distinguish between "I want to die" (crisis) and "this workload is killing me" (venting)
- **Structured output**: Returns JSON classification with confidence scores
- **Cost-effective**: Claude Haiku for high-throughput scanning, Sonnet for complex cases

## Model Selection
| Use Case | Model | Rationale |
|----------|-------|-----------|
| Content scanning (high volume) | `claude-haiku-4-5` | Fast, cheap, good for classification |
| Complex/ambiguous content | `claude-sonnet-4-6` | Better nuance for edge cases |
| Draft suggestions (Phase 4) | `claude-sonnet-4-6` | Quality matters for health suggestions |

## Safety Guarantees
1. **AI never auto-removes content** — it only flags for human review
2. **Exception**: CRISIS classification with >0.95 confidence triggers auto-hide + moderator alert
3. **False positive tolerance**: Better to over-flag than miss a crisis
4. **Audit trail**: Every AI classification is logged with model ID, confidence, reasoning
5. **Fallback**: If Claude API is unavailable, fall back to keyword scanner

## Consequences
- **Positive**: Scales moderation beyond what humans alone can handle
- **Positive**: Faster crisis detection (<15 seconds vs manual review)
- **Positive**: Domain-aware classification reduces false positives
- **Negative**: AI API cost (~$0.001 per scan with Haiku)
- **Negative**: Latency added to content scanning pipeline
- **Mitigation**: Async scanning — posts are visible immediately, scanned in background
