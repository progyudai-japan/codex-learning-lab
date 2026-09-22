const $ = id => document.getElementById(id);
const STORAGE = 'code-quest-progress-v1';
let quests = [], current = 0, selected = null, busy = false, solved = false;
let completed = new Set();
try {
  const saved = JSON.parse(localStorage.getItem(STORAGE) || '[]');
  if (Array.isArray(saved)) completed = new Set(saved.filter(x => typeof x === 'string'));
} catch { /* Storage may be unavailable; the game still works in memory. */ }
function save() {
  try { localStorage.setItem(STORAGE, JSON.stringify([...completed])); }
  catch { $('feedback').textContent += ' 進捗の保存ができないため、この画面を閉じるとリセットされます。'; }
}
function updateProgress() {
  const count = completed.size;
  $('xp').innerHTML = `${count * 100} <small>XP</small>`;
  $('rank').innerHTML = `Lv. ${Math.floor(count / 3) + 1} <small>${count === quests.length ? '開発の勇者' : count >= 6 ? 'コードの冒険者' : '見習い'}</small>`;
  $('progress').max = quests.length;
  $('progress').value = count;
  $('progress-label').textContent = `クリアしたクエスト　${count} / ${quests.length}`;
  $('complete').hidden = count !== quests.length;
  const map = $('map'); map.replaceChildren();
  let group, previous;
  quests.forEach((quest, index) => {
    if (quest.chapter !== previous) {
      group = document.createElement('div'); group.className = 'chapter-group';
      const title = document.createElement('h3'); title.className = 'chapter-title'; title.textContent = quest.chapter;
      group.append(title); map.append(group); previous = quest.chapter;
    }
    const button = document.createElement('button');
    button.className = `${index === current ? 'active' : ''} ${completed.has(quest.id) ? 'done' : ''}`;
    if (index === current) button.setAttribute('aria-current', 'step');
    const node = document.createElement('span'); node.className = 'node'; node.textContent = completed.has(quest.id) ? '✓' : index + 1;
    button.append(node, document.createTextNode(quest.title));
    button.disabled = busy; button.onclick = () => { current = index; render(); };
    group.append(button);
  });
}
function render() {
  const quest = quests[current]; selected = null; solved = false;
  $('chapter').textContent = quest.chapter;
  $('quest-number').textContent = `QUEST ${String(current + 1).padStart(2, '0')} / ${quests.length}`;
  $('quest-title').textContent = quest.title;
  $('question').textContent = quest.question;
  $('code').textContent = quest.code;
  $('hint').textContent = quest.hint; $('hint').hidden = true;
  $('hint-button').textContent = '✧ ヒントを見る';
  $('feedback').hidden = true; $('next').hidden = true; $('submit').disabled = true;
  $('submit').textContent = '答えをチェック →';
  const options = $('options'); options.disabled = false; options.replaceChildren();
  const legend = document.createElement('legend'); legend.textContent = '答えを選んでください'; options.append(legend);
  quest.options.forEach((option, index) => {
    const label = document.createElement('label'); label.className = 'option';
    const radio = document.createElement('input'); radio.type = 'radio'; radio.name = 'answer'; radio.value = index;
    radio.onchange = () => { selected = index; $('submit').disabled = false; $('feedback').hidden = true; };
    label.append(radio, document.createTextNode(option)); options.append(label);
  });
  updateProgress();
}
$('hint-button').onclick = () => { $('hint').hidden = !$('hint').hidden; $('hint-button').textContent = $('hint').hidden ? '✧ ヒントを見る' : 'ヒントを閉じる'; };
$('submit').onclick = async () => {
  if (selected === null || busy || solved) return;
  busy = true; $('submit').disabled = true; $('options').disabled = true; $('reset').disabled = true;
  $('submit').textContent = 'チェック中…'; updateProgress();
  try {
    const response = await fetch(`/api/quests/${quests[current].id}/answers`, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({option:selected})});
    if (!response.ok) throw new Error();
    const result = await response.json();
    $('feedback').className = result.correct ? 'success' : '';
    const already = completed.has(quests[current].id);
    $('feedback').textContent = `${result.correct ? (already ? '✓ 正解！復習もばっちりです。' : `✓ 正解！ +${result.xp} XP　`) : '惜しい！ '}${result.explanation}`;
    $('feedback').hidden = false;
    if (result.correct) {
      solved = true; completed.add(quests[current].id); save();
      $('next').hidden = false;
      $('next').textContent = completed.size === quests.length ? 'もう一度、復習する →' : '次のクエストへ →';
    }
  } catch { $('feedback').className = ''; $('feedback').textContent = '通信できませんでした。サーバーを確認して、もう一度チェックしてください。'; $('feedback').hidden = false; }
  finally { busy = false; $('submit').disabled = solved; $('options').disabled = solved; $('reset').disabled = false; $('submit').textContent = solved ? 'クリア済み ✓' : '答えをチェック →'; updateProgress(); }
};
$('next').onclick = () => {
  const remaining = quests.findIndex((q, index) => index > current && !completed.has(q.id));
  current = remaining >= 0 ? remaining : Math.max(0, quests.findIndex(q => !completed.has(q.id)));
  $('options').disabled = false; render(); $('quest-title').setAttribute('tabindex', '-1'); $('quest-title').focus();
};
$('reset').onclick = () => { if (confirm('すべての経験値とクリア記録をリセットしますか？')) { completed.clear(); save(); current = 0; $('options').disabled = false; render(); } };
$('retry').onclick = () => location.reload();
async function init() {
  try {
    const response = await fetch('/api/quests'); if (!response.ok) throw new Error();
    quests = await response.json(); if (!quests.length) throw new Error();
    completed = new Set([...completed].filter(id => quests.some(q => q.id === id)));
    current = Math.max(0, quests.findIndex(q => !completed.has(q.id))); render();
  } catch { $('quest-title').textContent = 'クエストを読み込めませんでした'; $('question').textContent = 'Spring Bootを起動してから、再読み込みしてください。'; $('retry').hidden = false; }
}
init();
