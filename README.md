# Disha 🧭
**Your AI scout for better roles.**

AI-powered career matching app for India — matches your profile to jobs intelligently, automates applications, and helps you land better roles faster.

**▶️ [Try the live demo](https://appetize.io/app/b_b3t4ncrm44fpkwie7t3pm3e7jm)** — run the app in your browser, no install needed.

## Demo

https://github.com/user-attachments/assets/cc806bbe-88f5-4fdc-98bb-a0932b9b0a78

## The problem

Job hunting in India today means juggling five different platforms, tailoring the same resume a dozen slightly different ways, and applying to roles without knowing if you're even a realistic match — most of that effort goes into repetitive manual work rather than actually deciding which roles are worth pursuing.

## Who it's for

- **Primary:** Active job seekers in India applying across multiple platforms (LinkedIn, Indeed, Naukri) who want less manual repetition and more signal on which roles actually fit.
- **Secondary:** Passive job seekers who want to be notified only when a genuinely strong match appears, rather than browsing constantly.
- **Explicitly not for:** Employers or recruiters — this is a job-seeker-side tool only, not a hiring/ATS platform.

## The key decision

The bet: **AI-scored match quality plus automated application execution removes more friction than a better search filter ever could.** The core job isn't helping someone search — it's helping them stop doing repetitive manual work across platforms.

## The trade-off

The hard choice was building **auto-apply automation** across third-party platforms (LinkedIn, Indeed, Naukri) instead of staying a pure discovery/matching tool that hands off to the user to apply manually. The alternative rejected: match-and-recommend only, leaving the actual application step to the user.

What that cost: automating applications across platforms Disha doesn't control is inherently fragile — those platforms can change their structure at any time and break the flow — and it raises a real trust bar, since users are letting the app act on their behalf, not just advise them.

## Feature breakdown

### AI job matching
Match scoring based on the user's profile against a job listing, surfaced as a ranked feed rather than a raw list.

### Auto apply (designed flow)
The intended v1 direction is automating the application step across LinkedIn, Indeed, and Naukri — see Known Limits below for what's actually wired up today versus what's the target design.

### AI-generated cover letters
Personalized cover letter drafts generated per job, rather than a single generic template reused across applications.

### Job listings
A browsable, filterable feed — currently backed by a 20-role seeded dataset spanning varied domains (not yet live-scraped; see Known Limits).

### CV builder
Generates a professional resume PDF, priced at ₹49 as a deliberate signal of demand rather than a free growth lever (see the trade-off framing above and Known Limits for payment status).

### Smart notifications
Job alerts and application status updates.

### Privacy-first data handling
Data isn't sold, and stays under the user's control by design — not an afterthought bolted on later.

### Design pass (v1.1 polish)
A follow-up pass focused on making the app feel finished rather than prototype-rough: fixed an accessibility bug where the cover-letter action button was rendering white text on a white background, replaced a hardcoded color clash on user avatars, resolved layout collisions and status-bar clipping on a few screens, redesigned the bottom navigation into a glassmorphic pill style, and — importantly — removed raw backend/internal terminology ("FCM Notification Center," "Deep Web Scraping Engine") that had been leaking into user-facing copy. None of this changed what the app does; it changed whether it reads as a finished product.

## What's deliberately not in v1

- **Employer/recruiter-side tools** — deliberately out of scope; this is a job-seeker tool, not a two-sided marketplace.
- **Interview prep / salary negotiation features** — cut to keep v1 focused on the matching-and-applying funnel rather than expanding into the full job-search journey.
- **Free CV builder** — the ₹49 pricing was kept rather than making it free, treating it as a signal of real demand rather than a pure growth lever.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Backend | Firebase |
| Architecture | MVVM |
| Database | Room + DataStore |

## Setup

Add your keys to `local.properties`.

## How I would measure it

**North star: % of AI-matched recommendations that result in a completed application.** Match scoring is only valuable if it's accurate enough that users act on it — a high match rate with a low apply-through rate would mean the scoring isn't trusted, not that the matches are good.

Supporting metrics:

- **Auto-apply success rate** — what fraction of automated applications complete without failure, given how fragile cross-platform automation inherently is.
- **CV builder conversion rate** — since it's a paid feature (₹49), conversion is a direct signal of perceived value, not just usage.
- **Repeat usage rate** — whether users come back for more matches after their first session, versus using it once and leaving.

## Known limits

An honest list of what's missing or unverified — this is a portfolio prototype, and it's more useful to say that plainly than to let the feature list imply otherwise:

- **Auth is currently simulated**, not a live Firebase Identity Toolkit integration — there's no real account system behind the login screen yet.
- **Job listings are a seeded local dataset (20 roles across varied domains)**, not live scraping from LinkedIn, Indeed, or Naukri. The UI is built to support real aggregation; the data pipeline behind it isn't connected yet.
- **The CV builder and cover letter pricing (₹49 each) is UI copy, not a working payment flow** — no payment gateway is wired in. The "How I would measure it" metrics above (conversion rate, etc.) describe what I'd track once that's real, not what's measurable today.
- **Auto-apply automation is a designed flow, not yet live** — the actual cross-platform execution described in the trade-off above is the intended v1 direction, not something currently running against real job platforms.
- **The "AI Interview Assistant" tab doesn't yet deliver on its label** — it exists in the nav but the underlying feature isn't built out.
- **No automated tests.**

## Development note

This project was built using AI-assisted development with [Claude Code](https://claude.com/claude-code), Anthropic's agentic coding tool.

---

**Anubhav Kapoor**
