# AI Beginner Programs

A Spring Boot web app (Java 17, port `8335`) with three features:

1. **Crawl** public internet sources (GitHub, Hacker News, Dev.to, Reddit) for signal about
   trending, beginner-friendly AI projects.
2. **Rank & print** the most popular AI program ideas for beginners, combining a curated
   catalog of well-known project archetypes with mention counts from the crawl.
3. **Suggest** freelancing angles for those ideas: target platforms, pricing ranges, client
   pitches, and portfolio tips, plus general freelancer advice.

On startup the app automatically runs the full pipeline once and prints the ranked ideas and
freelance suggestions to the console. All three features are also available on demand via REST.

## Run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8335`.

## Endpoints

| Method | Path                        | Description                                                        |
|--------|-----------------------------|----------------------------------------------------------------------|
| GET    | `/api/crawl`                | Raw crawled items from GitHub / Hacker News / Dev.to / Reddit       |
| GET    | `/api/ideas/popular?limit=` | Ranked list of popular AI program ideas for beginners (default 10) |
| GET    | `/api/freelance/suggestions?limit=` | Freelance suggestions for the top ideas (default 5)         |
| GET    | `/api/report?ideaLimit=&freelanceLimit=` | Combined crawl + ranked ideas + freelance report       |

Each of these also logs a human-readable report to the console.

## Notes

- Each crawl source is fetched in parallel with a short timeout and fails independently, so a
  single blocked/rate-limited source (or no internet access at all) never breaks the app - it
  falls back to the curated idea catalog's baseline ranking.
- No API keys are required; all sources used are public, key-less endpoints.
