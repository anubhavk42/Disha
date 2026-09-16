# Disha 🧭
**Your AI scout for better roles.**

AI-powered career matching app for India — matches your profile to jobs intelligently, automates applications, and helps you land better roles faster.

## 📱 Screenshots

<p align="center">
  <img src="screenshots/screenshot_2.jpeg" width="30%" />
  <img src="screenshots/screenshot_3.jpeg" width="30%" />
  <img src="screenshots/screenshot_4.jpeg" width="30%" />
</p>
<p align="center">
  <img src="screenshots/screenshot_5.jpeg" width="30%" />
  <img src="screenshots/screenshot_6.jpeg" width="30%" />
  <img src="screenshots/screenshot_7.jpeg" width="30%" />
</p>

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

## What's in v1

- 🎯 AI job matching — match scoring based on your profile
- 🤖 Auto apply — automate applications across LinkedIn, Indeed & Naukri
- 📄 AI-generated personalized cover letters
- 🔍 Live job listings aggregated from multiple platforms
- 💼 CV builder — professional resume PDF (₹49)
- 🔔 Smart notifications — job alerts and application updates
- 🔒 Privacy-first data handling — data never sold, always in the user's control

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

An honest list of what's missing or unverified:

- **Auto-apply automation depends on third-party platform structure** (LinkedIn, Indeed, Naukri) and can break if those platforms change their UI or add anti-automation measures — this is a standing operational risk, not a one-time bug.
- **Match-scoring accuracy hasn't been independently benchmarked** — evaluated through hands-on testing, not a measured precision rate.
- **No automated tests.**

## Development note

This project was built using AI-assisted development with [Claude Code](https://claude.com/claude-code), Anthropic's agentic coding tool.

---

**Anubhav Kapoor** — Product & Growth Associate → APM. Part of a portfolio of shipped product experiments: [Khyaal](https://github.com/anubhavk42/khyaal) · [SAAR](https://github.com/anubhavk42/saar) · [NutriLens AI](https://github.com/anubhavk42/nutrilens-ai) · [Aakash](https://github.com/anubhavk42/Aakash)
