const logEl = document.getElementById("log");
const formEl = document.getElementById("chat-form");
const inputEl = document.getElementById("messageInput");

const sessionId = "session-" + Math.random().toString(36).slice(2);

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str ?? "";
  return div.innerHTML;
}

function appendMessage(text, who) {
  const div = document.createElement("div");
  div.className = `msg ${who}`;
  div.textContent = text;
  logEl.appendChild(div);
  logEl.scrollTop = logEl.scrollHeight;
}

async function loadGreeting() {
  try {
    const res = await fetch("/api/eliza/greeting");
    const data = await res.json();
    appendMessage(data.reply, "eliza");
  } catch (err) {
    appendMessage("Hello. I am Eliza. How are you feeling today?", "eliza");
  }
}

async function sendMessage(message) {
  appendMessage(message, "user");
  try {
    const res = await fetch("/api/eliza/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ sessionId, message })
    });
    if (!res.ok) throw new Error(`Request failed: ${res.status}`);
    const data = await res.json();
    appendMessage(data.reply, "eliza");
  } catch (err) {
    appendMessage(`(error talking to Eliza: ${err.message})`, "eliza");
  }
}

formEl.addEventListener("submit", (event) => {
  event.preventDefault();
  const message = inputEl.value.trim();
  if (!message) return;
  inputEl.value = "";
  sendMessage(message);
});

loadGreeting();
