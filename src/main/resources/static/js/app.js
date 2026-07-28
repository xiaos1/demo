const summaryEl = document.getElementById("summary");
const ideasEl = document.getElementById("ideas");
const freelanceEl = document.getElementById("freelance");
const tipsEl = document.getElementById("tips");
const statusEl = document.getElementById("status");
const refreshBtn = document.getElementById("refreshBtn");

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str ?? "";
  return div.innerHTML;
}

function renderSummary(report) {
  summaryEl.innerHTML =
    `<strong>${report.crawledItemCount}</strong> items crawled from public sources just now ` +
    `(GitHub, Hacker News, Dev.to, Reddit) - ranked against a curated catalog of beginner AI project archetypes below.`;
}

function renderIdeas(ideas) {
  if (!ideas.length) {
    ideasEl.innerHTML = `<p class="empty">No ideas returned.</p>`;
    return;
  }
  ideasEl.innerHTML = ideas.map(idea => `
    <div class="idea-card">
      <h3><span class="rank">#${idea.rank}</span>${escapeHtml(idea.name)}</h3>
      <p>${escapeHtml(idea.description)}</p>
      <div>
        <span class="badge">${escapeHtml(idea.difficulty)}</span>
        <span class="badge">score ${idea.popularityScore}</span>
      </div>
      <div class="tech">Stack: ${idea.techStack.map(escapeHtml).join(", ")}</div>
    </div>
  `).join("");
}

function renderFreelance(report) {
  const suggestions = report.ideaSuggestions || [];
  if (!suggestions.length) {
    freelanceEl.innerHTML = `<p class="empty">No suggestions returned.</p>`;
  } else {
    freelanceEl.innerHTML = suggestions.map(s => `
      <div class="freelance-card">
        <h3>${escapeHtml(s.ideaName)}</h3>
        <div><span class="badge">${escapeHtml(s.pricingRange)}</span></div>
        <p>${escapeHtml(s.clientPitch)}</p>
        <div class="tech">Platforms: ${s.platforms.map(escapeHtml).join(", ")}</div>
        <ul>${s.portfolioTips.map(t => `<li>${escapeHtml(t)}</li>`).join("")}</ul>
      </div>
    `).join("");
  }

  tipsEl.innerHTML = (report.generalTips || []).map(t => `<li>${escapeHtml(t)}</li>`).join("");
}

async function loadReport() {
  refreshBtn.disabled = true;
  statusEl.textContent = "Loading...";
  try {
    const res = await fetch("/api/report?ideaLimit=10&freelanceLimit=5");
    if (!res.ok) throw new Error(`Request failed: ${res.status}`);
    const report = await res.json();
    renderSummary(report);
    renderIdeas(report.topIdeas || []);
    renderFreelance(report.freelanceReport || {});
    statusEl.textContent = `Updated ${new Date().toLocaleTimeString()}`;
  } catch (err) {
    statusEl.textContent = `Failed to load: ${err.message}`;
  } finally {
    refreshBtn.disabled = false;
  }
}

refreshBtn.addEventListener("click", loadReport);
loadReport();
