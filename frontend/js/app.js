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
  resultEl.appendChild(renderDistribution(r));
}

function pct(v) {
  return v === null || v === undefined ? "未推定" : `${(v * 100).toFixed(1)}%`;
}

function renderDistribution(r) {
  const wrap = document.createElement("div");
  const heading = document.createElement("h3");
  heading.textContent = "状態分布の予測と収束";
  wrap.appendChild(heading);

  const stationary = document.createElement("p");
  if (r.stationaryDistribution) {
    stationary.textContent =
      "定常分布： " +
      STATE_ORDER.map((s, i) => `${s} ${pct(r.stationaryDistribution[i])}`).join(" ／ ");
  } else {
    stationary.textContent =
      "定常分布： 未推定（遷移回数0の状態があるため計算できません）";
  }
  wrap.appendChild(stationary);

  const table = document.createElement("table");
  const thead = document.createElement("thead");
  const headRow = document.createElement("tr");
  headRow.appendChild(document.createElement("th"));
  for (const s of STATE_ORDER) {
    const th = document.createElement("th");
    th.textContent = STATE_LABELS[s];
    headRow.appendChild(th);
  }
  thead.appendChild(headRow);
  table.appendChild(thead);
  const tbody = document.createElement("tbody");
  for (const h of ["1", "3", "5", "10"]) {
    const dist = r.horizonDistributions ? r.horizonDistributions[h] : null;
    const tr = document.createElement("tr");
    const th = document.createElement("th");
    th.textContent = `${h}営業日後`;
    tr.appendChild(th);
    for (let i = 0; i < 3; i++) {
      const td = document.createElement("td");
      td.textContent = dist ? pct(dist[i]) : "未推定";
      tr.appendChild(td);
    }
    tbody.appendChild(tr);
  }
  table.appendChild(tbody);
  wrap.appendChild(table);

  if (r.convergence && r.convergence.length > 1) {
    const canvas = document.createElement("canvas");
    canvas.width = 820;
    canvas.height = 280;
    wrap.appendChild(canvas);
    drawConvergence(canvas, r);
    const note = document.createElement("p");
    note.textContent =
      "現在の状態から出発した状態分布が、営業日を追うごとに定常分布（破線）へ近づく様子です。" +
      "早い段階で重なってしまう場合、それより先は現在の状態の情報がほぼ失われていることを意味します。";
    wrap.appendChild(note);
  }
  return wrap;
}

function drawConvergence(canvas, r) {
  const ctx = canvas.getContext("2d");
  const W = canvas.width;
  const H = canvas.height;
  const padL = 44;
  const padR = 12;
  const padT = 20;
  const padB = 28;
  const plotW = W - padL - padR;
  const plotH = H - padT - padB;
  const points = r.convergence;
  const maxH = points[points.length - 1].h;
  const colors = { U: "#d9534f", F: "#888888", D: "#337ab7" };
  const x = (h) => padL + (h / maxH) * plotW;
  const y = (v) => padT + (1 - v) * plotH;

  ctx.font = "11px sans-serif";
  ctx.lineWidth = 1;
  for (const gv of [0, 0.25, 0.5, 0.75, 1]) {
    ctx.strokeStyle = "#dddddd";
    ctx.beginPath();
    ctx.moveTo(padL, y(gv));
    ctx.lineTo(W - padR, y(gv));
    ctx.stroke();
    ctx.fillStyle = "#666666";
    ctx.fillText(`${Math.round(gv * 100)}%`, 8, y(gv) + 4);
  }
  ctx.fillStyle = "#666666";
  ctx.fillText("0", x(0) - 3, H - 8);
  ctx.fillText(`${maxH}営業日`, x(maxH) - 36, H - 8);

  if (r.stationaryDistribution) {
    STATE_ORDER.forEach((s, i) => {
      ctx.strokeStyle = colors[s];
      ctx.setLineDash([5, 4]);
      ctx.beginPath();
      ctx.moveTo(padL, y(r.stationaryDistribution[i]));
      ctx.lineTo(W - padR, y(r.stationaryDistribution[i]));
      ctx.stroke();
    });
    ctx.setLineDash([]);
  }

  STATE_ORDER.forEach((s, i) => {
    ctx.strokeStyle = colors[s];
    ctx.lineWidth = 2;
    ctx.beginPath();
    let started = false;
    for (const p of points) {
      if (!p.distribution) {
        started = false;
        continue;
      }
      const px = x(p.h);
      const py = y(p.distribution[i]);
      if (!started) {
        ctx.moveTo(px, py);
        started = true;
      } else {
        ctx.lineTo(px, py);
      }
    }
    ctx.stroke();
  });

  ctx.lineWidth = 2;
  STATE_ORDER.forEach((s, i) => {
    const lx = padL + i * 130;
    ctx.strokeStyle = colors[s];
    ctx.beginPath();
    ctx.moveTo(lx, 10);
    ctx.lineTo(lx + 22, 10);
    ctx.stroke();
    ctx.fillStyle = "#333333";
    ctx.fillText(STATE_LABELS[s], lx + 27, 14);
  });
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

const MODEL_ORDER = ["markov", "majority", "persistence"];
const MODEL_LABELS = {
  markov: "マルコフ",
  majority: "Majority（最頻状態）",
  persistence: "Persistence（前日と同じ）",
};

const backtestForm = document.getElementById("backtest-form");
const backtestMessageEl = document.getElementById("backtest-message");
const backtestResultEl = document.getElementById("backtest-result");
const savedBacktestsEl = document.getElementById("saved-backtests");

function score(v, digits) {
  return v === null || v === undefined ? "—" : v.toFixed(digits);
}

function renderBacktest(r) {
  backtestResultEl.textContent = "";
  const meta = document.createElement("p");
  meta.textContent =
    `${r.ticker} ／ データ期間 ${r.dataStart} 〜 ${r.dataEnd} ／ 観測数 ${r.observations} ／ ` +
    `評価ステップ数 ${r.steps} ／ 最小学習数 ${r.minTrain} ／ θ = ${(r.theta * 100).toFixed(1)}%`;
  backtestResultEl.appendChild(meta);

  const table = document.createElement("table");
  const thead = document.createElement("thead");
  const headRow = document.createElement("tr");
  for (const label of ["モデル", "予測数", "Accuracy", "Precision", "Recall", "Brier", "Log Loss"]) {
    const th = document.createElement("th");
    th.textContent = label;
    headRow.appendChild(th);
  }
  thead.appendChild(headRow);
  table.appendChild(thead);
  const tbody = document.createElement("tbody");
  for (const key of MODEL_ORDER) {
    const m = r.models[key];
    if (!m) continue;
    const tr = document.createElement("tr");
    const th = document.createElement("th");
    th.textContent = MODEL_LABELS[key];
    tr.appendChild(th);
    const cells = [
      String(m.predictions),
      m.accuracy === null ? "—" : `${(m.accuracy * 100).toFixed(1)}%`,
      score(m.precisionMacro, 3),
      score(m.recallMacro, 3),
      score(m.brierScore, 3),
      score(m.logLoss, 3),
    ];
    for (const text of cells) {
      const td = document.createElement("td");
      td.textContent = text;
      tr.appendChild(td);
    }
    tbody.appendChild(tr);
  }
  table.appendChild(tbody);
  backtestResultEl.appendChild(table);

  const markov = r.models.markov;
  if (markov && markov.skipped > 0) {
    const skip = document.createElement("p");
    skip.textContent =
      `マルコフは、現在の状態の遷移行が未推定だった ${markov.skipped} ステップを予測せずにスキップしました。`;
    backtestResultEl.appendChild(skip);
  }
  if (markov) {
    backtestResultEl.appendChild(
      matrixTable("混同行列（マルコフ）", markov.confusionMatrix, (v) => String(v))
    );
    const note = document.createElement("p");
    note.textContent = "混同行列は、行が実際の状態、列が予測した状態です。対角成分が正解数です。";
    backtestResultEl.appendChild(note);
  }
  const note = document.createElement("p");
  note.textContent =
    "Precision / Recall は3状態のマクロ平均です。Brier と Log Loss は小さいほど良い指標です。";
  backtestResultEl.appendChild(note);
}

async function loadSavedBacktests() {
  savedBacktestsEl.textContent = "";
  try {
    const res = await fetch("/api/backtests");
    if (!res.ok) return;
    const items = await res.json();
    if (items.length === 0) {
      const li = document.createElement("li");
      li.textContent = "保存済みのバックテストはまだありません。";
      savedBacktestsEl.appendChild(li);
      return;
    }
    for (const item of items) {
      const li = document.createElement("li");
      const button = document.createElement("button");
      button.type = "button";
      const acc = item.models.markov.accuracy;
      button.textContent =
        `${item.ticker} ${item.dataStart}〜${item.dataEnd}` +
        `（θ=${(item.theta * 100).toFixed(1)}%、Accuracy ${acc === null ? "—" : (acc * 100).toFixed(1) + "%"}）`;
      button.addEventListener("click", async () => {
        const res = await fetch(`/api/backtests/${item.id}`);
        if (res.ok) renderBacktest(await res.json());
      });
      li.appendChild(button);
      savedBacktestsEl.appendChild(li);
    }
  } catch (e) {
    // Backend not reachable yet (e.g. static preview): leave the list empty.
  }
}

backtestForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  backtestMessageEl.textContent = "バックテスト実行中…";
  const payload = {
    ticker: document.getElementById("ticker").value.trim(),
    start: document.getElementById("start").value,
    end: document.getElementById("end").value,
    theta: Number(document.getElementById("theta").value) / 100,
    minTrain: Number(document.getElementById("min-train").value),
  };
  try {
    const res = await fetch("/api/backtests", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      throw new Error(body.error || `エラー（${res.status}）`);
    }
    renderBacktest(await res.json());
    backtestMessageEl.textContent = "バックテストが完了し、結果を保存しました。";
    loadSavedBacktests();
  } catch (e) {
    backtestMessageEl.textContent = `バックテストを実行できませんでした： ${e.message}`;
  }
});

initDefaults();
loadSaved();
loadSavedBacktests();
