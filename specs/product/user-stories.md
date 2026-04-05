# User Stories — Mental Health Platform

## Epic 1: User Identity & Authentication

### US-1.1: Anonymous Registration
**As a** seeker
**I want to** register with my email but choose an anonymous display name
**So that** I can participate in discussions without revealing my identity

**Acceptance Criteria:**
- [ ] User provides email, password, and anonymous display name
- [ ] Email is verified via confirmation link
- [ ] Display name is checked for uniqueness
- [ ] Display name cannot contain real names (basic heuristic check)
- [ ] No real name or PII is shown anywhere in the forum UI
- [ ] User receives a unique anonymous ID (e.g., `anon-xxxxx`)

**Technical Notes:**
- Email stored encrypted at rest, never exposed via API
- Anonymous ID is a separate, non-reversible identifier
- JWT token contains only anonymous ID, roles — never email

---

### US-1.2: Login
**As a** registered user
**I want to** log in with email and password
**So that** I can access my account

**Acceptance Criteria:**
- [ ] Login with email + password returns JWT access token + refresh token
- [ ] Access token expires in 15 minutes
- [ ] Refresh token expires in 7 days
- [ ] Failed login after 5 attempts locks account for 15 minutes
- [ ] Login response includes anonymous display name and roles

---

### US-1.3: Profile Management
**As a** registered user
**I want to** update my anonymous display name and notification preferences
**So that** I can control my identity and communication

**Acceptance Criteria:**
- [ ] User can change display name (uniqueness check applies)
- [ ] User can toggle email notifications (digest, mentions, replies)
- [ ] User can delete their account (soft delete, anonymizes all posts)
- [ ] Profile endpoint never returns email or PII

---

## Epic 2: Forum Discussions

### US-2.1: Browse Categories
**As a** seeker
**I want to** browse forum categories
**So that** I can find discussions relevant to my concerns

**Acceptance Criteria:**
- [ ] Categories displayed: Anxiety, Depression, Relationships, Work Stress, General Wellness, Recovery Stories
- [ ] Each category shows thread count and latest activity timestamp
- [ ] Categories are viewable without authentication
- [ ] Pinned/featured threads appear at top of each category

---

### US-2.2: Create Thread
**As a** registered user
**I want to** create a new discussion thread in a category
**So that** I can share my experience or ask for support

**Acceptance Criteria:**
- [ ] Thread has: title (5-200 chars), body (10-10000 chars), category
- [ ] Thread displays author's anonymous display name
- [ ] Thread is immediately visible after creation (pre-moderation for flagged content)
- [ ] Thread body supports basic markdown (bold, italic, lists)
- [ ] Thread creation triggers content safety scan (async)
- [ ] If crisis keywords detected, response includes crisis resources banner

**Crisis Keywords (initial set):**
`suicide`, `kill myself`, `end it all`, `self-harm`, `cutting`, `overdose`, `don't want to live`, `no reason to live`

---

### US-2.3: Reply to Thread
**As a** registered user
**I want to** reply to a discussion thread
**So that** I can offer support or share my perspective

**Acceptance Criteria:**
- [ ] Reply has: body (10-5000 chars)
- [ ] Reply displays author's anonymous display name and timestamp
- [ ] Replies are ordered chronologically
- [ ] Reply triggers same content safety scan as thread creation
- [ ] Thread author is notified of new replies (if notifications enabled)
- [ ] Nested replies (1 level deep only) supported

---

### US-2.4: Search Threads
**As a** user
**I want to** search for threads by keyword
**So that** I can find existing discussions on my topic

**Acceptance Criteria:**
- [ ] Full-text search across thread titles and bodies
- [ ] Results ranked by relevance, then recency
- [ ] Search available without authentication
- [ ] Results show title, category, reply count, snippet with highlighted match
- [ ] Search respects content that has been moderation-removed (excluded)

---

### US-2.5: Report Content
**As a** registered user
**I want to** report a thread or reply that violates community guidelines
**So that** moderators can review and take action

**Acceptance Criteria:**
- [ ] Report reasons: Harassment, Misinformation, Spam, Crisis Risk, Other
- [ ] Reporter provides optional description (max 500 chars)
- [ ] Reporter identity is never revealed to the reported user
- [ ] Report creates a moderation queue entry
- [ ] Duplicate reports on same content are aggregated

---

## Epic 3: Content Moderation

### US-3.1: Moderation Queue
**As a** moderator/admin
**I want to** review flagged content in a queue
**So that** I can ensure community safety

**Acceptance Criteria:**
- [ ] Queue shows: content snippet, flag reason, flag count, reporter count, timestamp
- [ ] Moderator actions: Approve, Remove, Warn User, Ban User
- [ ] Removed content is replaced with "[Removed by moderator]" placeholder
- [ ] All moderation actions are audit-logged
- [ ] Queue is prioritized: Crisis Risk > Harassment > Misinformation > Spam > Other

---

### US-3.2: Automated Content Scanning
**As the** platform
**I want to** automatically scan all user content for safety concerns
**So that** dangerous content is flagged before it causes harm

**Acceptance Criteria:**
- [ ] Every thread and reply is scanned asynchronously after creation
- [ ] Scan classifies content: SAFE, NEEDS_REVIEW, CRISIS, TOXIC
- [ ] CRISIS content immediately triggers: crisis resource banner + moderator alert
- [ ] TOXIC content is hidden pending moderator review
- [ ] NEEDS_REVIEW content remains visible but enters moderation queue
- [ ] Classification confidence score stored for quality tracking

---

## Epic 4: Expert Integration (Phase 3)

### US-4.1: Expert Registration
**As a** mental health professional
**I want to** register as a verified expert
**So that** I can provide guidance to platform users

**Acceptance Criteria:**
- [ ] Expert provides: name, credentials, license number, specializations
- [ ] Credential verification workflow (manual review by admin)
- [ ] Expert profile shows: display name, specializations, verified badge
- [ ] Expert can set availability hours
- [ ] Expert identity is verified but displayed separately from seeker identities

---

### US-4.2: Ask an Expert
**As a** seeker
**I want to** post a question in the "Ask an Expert" section
**So that** I can get guidance from a verified professional

**Acceptance Criteria:**
- [ ] Questions are anonymous (same as forum)
- [ ] Questions are routed to experts based on specialization match
- [ ] Rate limit: 2 expert questions per user per week
- [ ] Expert responses marked with verified badge
- [ ] AI drafts initial response for expert review (Phase 4)

---

## Epic 5: AI-Assisted Suggestions (Phase 4)

### US-5.1: AI Draft Suggestions
**As an** expert
**I want** AI to draft initial responses to seeker questions
**So that** I can review, edit, and approve them faster

**Acceptance Criteria:**
- [ ] AI generates draft based on: question content + expert's knowledge base + clinical guidelines
- [ ] Draft is NEVER shown to seeker until expert approves
- [ ] Expert can: approve as-is, edit then approve, reject and write from scratch
- [ ] AI confidence score displayed to expert
- [ ] All AI drafts are audit-logged with version history

---

### US-5.2: Resource Recommendations
**As a** seeker
**I want to** receive personalized resource recommendations
**So that** I can find self-help materials relevant to my situation

**Acceptance Criteria:**
- [ ] Recommendations based on: threads read, categories browsed, questions asked
- [ ] Resources include: articles, worksheets, breathing exercises, crisis lines
- [ ] All resources are expert-curated (not AI-generated content)
- [ ] User can dismiss or save recommendations
- [ ] Recommendation engine respects user privacy (on-device where possible)
