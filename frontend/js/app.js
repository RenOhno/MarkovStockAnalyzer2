"use strict";

const STATE_LABELS = { U: "U（上昇）", F: "F（横ばい）", D: "D（下落）" };
const STATE_ORDER = ["U", "F", "D"];

const form = document.getElementById("analysis-form");
const messageEl = document.getElementById("message");
const resultEl = document.getElementById("result");
const savedListEl = document.getElementById("saved-list");

function isoDate(d) {
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function initDefaults() {
  const end = new Date();
  end.setDate(end.getDate() + 1); // end is exclusive: tomorrow includes today
  const start = new Date(end);
  start.setFullYear(start.getFullYear() - 3);
  document.getElementById("end").value = isoDate(end);
  document.getElementById("start").value = isoDate(start);
}

function matrixTable(title, matrix, format) {
  const wrap = document.createElement("div");
  const heading = document.createElement("h3");
  heading.textContent = title;
  wrap.appendChild(heading);
  const table = document.createElement("table");
  const thead = document.createElement("thead");
  const headRow = document.createElement("tr");
  headRow.appendChild(document.createElement("th"));
  for (const s of STATE_ORDER) {
    const th = document.createElement("th");
    th.textContent = `→ ${STATE_LABELS[s]}`;
    headRow.appendChild(th);
  }
  thead.appendChild(headRow);
  table.appendChild(thead);
  const tbody = document.createElement("tbody");
  matrix.forEach((row, i) => {
    const tr = document.createElement("tr");
    const th = document.createElement("th");
    th.textContent = STATE_LABELS[STATE_ORDER[i]];
    tr.appendChild(th);
    row.forEach((value) => {
      const td = document.createElement("td");
      td.textContent = format(value);
      tr.appendChild(td);
    });
    tbody.appendChild(tr);
  });
  table.appendChild(tbody);
  wrap.appendChild(table);
  return wrap;
}

function renderResult(r) {
  resultEl.textContent = "";
  const meta = document.createElement("p");
  meta.textContent =
    `${r.ticker} ／ データ期間 ${r.dataStart} 〜 ${r.dataEnd} ／ 観測数 ${r.observations} ／ ` +
    `現在の状態 ${STATE_LABELS[r.currentState]} ／ θ = ${(r.theta * 100).toFixed(1)}%`;
  resultEl.appendChild(meta);

  const counts = document.createElement("p");
  counts.textContent =
    `状態の内訳： U ${r.stateCounts.U} ／ F ${r.stateCounts.F} ／ D ${r.stateCounts.D}`;
  resultEl.appendChild(counts);

  resultEl.appendChild(matrixTable("遷移回数", r.transitionCounts, (v) => String(v)));
  resultEl.appendChild(
    matrixTable("遷移確率（最尤推定）", r.transitionProbabilities, (v) =>
      v === null ? "未推定" : `${(v * 100).toFixed(1)}%`
    )
  );
}

async function loadSaved() {
  savedListEl.textContent = "";
  try {
    const res = await fetch("/api/analyses");
    if (!res.ok) return;
    const items = await res.json();
    if (items.length === 0) {
      const li = document.createElement("li");
      li.textContent = "保存済みの分析はまだありません。";
      savedListEl.appendChild(li);
      return;
    }
    for (const item of items) {
      const li = document.createElement("li");
      const button = document.createElement("button");
      button.type = "button";
      button.textContent =
        `${item.ticker} ${item.dataStart}〜${item.dataEnd}（θ=${(item.theta * 100).toFixed(1)}%）`;
      button.addEventListener("click", async () => {
        const res = await fetch(`/api/analyses/${item.id}`);
        if (res.ok) renderResult(await res.json());
      });
      li.appendChild(button);
      savedListEl.appendChild(li);
    }
  } catch (e) {
    // Backend not reachable yet (e.g. static preview): leave the list empty.
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  messageEl.textContent = "分析中…";
  const payload = {
    ticker: document.getElementById("ticker").value.trim(),
    start: document.getElementById("start").value,
    end: document.getElementById("end").value,
    theta: Number(document.getElementById("theta").value) / 100,
  };
  try {
    const res = await fetch("/api/analyses", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      throw new Error(body.error || `エラー（${res.status}）`);
    }
    renderResult(await res.json());
    messageEl.textContent = "分析が完了し、結果を保存しました。";
    loadSaved();
  } catch (e) {
    messageEl.textContent = `分析できませんでした： ${e.message}`;
  }
});

initDefaults();
loadSaved();
