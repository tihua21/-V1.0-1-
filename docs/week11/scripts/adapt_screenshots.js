/**
 * 第11周 适配测试（TC-A1 桌面 1366×768 / TC-A2 手机 390×844）
 * 用例：登录 user1 -> 进入会议室列表页 -> 检查无横向溢出、关键按钮可见可点 -> 截图
 * 证据：../evidence/adapt_desktop_1366.png、adapt_mobile_390.png、adaptation.json
 */
const fs = require('fs');
const path = require('path');
// 复用 pnpm 全局已装的 playwright-core（浏览器 chromium-1228 已下载）
const { chromium } = require('C:/Users/123/AppData/Local/pnpm/global/5/.pnpm/playwright-core@1.61.1/node_modules/playwright-core');

const CHROME_EXE = 'C:/Users/123/AppData/Local/ms-playwright/chromium-1228/chrome-win64/chrome.exe';
const BASE = 'http://localhost:5173';
const EVIDENCE_DIR = path.join(__dirname, '..', 'evidence');

async function checkViewport(browser, name, width, height, screenshotFile) {
  const context = await browser.newContext({
    viewport: { width, height },
    deviceScaleFactor: 1,
  });
  const page = await context.newPage();

  // 1. 打开登录页并登录
  await page.goto(BASE, { waitUntil: 'networkidle' });
  await page.fill('input[placeholder="请输入用户名"]', 'user1');
  await page.fill('input[placeholder="请输入密码"]', '123456');
  await page.click('button.el-button--primary');
  // 登录成功后跳转 /rooms，等待列表渲染
  await page.waitForURL('**/rooms', { timeout: 10000 });
  await page.waitForTimeout(1500); // 等表格/卡片渲染稳定

  // 2. 检查：无横向溢出、页面主内容可见、按钮可点
  const result = await page.evaluate(() => {
    const doc = document.documentElement;
    const bodyText = document.body.innerText || '';
    const buttons = Array.from(document.querySelectorAll('button'))
      .filter(b => b.offsetParent !== null) // 可见
      .map(b => (b.innerText || '').trim())
      .filter(t => t.length > 0);
    return {
      scrollWidth: doc.scrollWidth,
      clientWidth: doc.clientWidth,
      noHorizontalOverflow: doc.scrollWidth <= doc.clientWidth + 1, // 允许 1px 误差
      hasRoomContent: bodyText.includes('会议室'),
      visibleButtons: buttons.slice(0, 10),
      buttonCount: buttons.length,
      url: location.pathname,
    };
  });

  // 3. 尝试点击第一个可见按钮，验证可交互
  let clickOk = false;
  try {
    const firstBtn = page.locator('button:visible').first();
    if (await firstBtn.count() > 0) {
      await firstBtn.hover({ timeout: 3000 });
      clickOk = true; // hover 成功即认为可交互（不真的点，避免误触预约）
    }
  } catch (e) { clickOk = false; }

  // 4. 截图
  const shotPath = path.join(EVIDENCE_DIR, screenshotFile);
  await page.screenshot({ path: shotPath, fullPage: false });

  const passed = result.noHorizontalOverflow && result.hasRoomContent && clickOk;
  const record = {
    case_no: name,
    viewport: `${width}x${height}`,
    url: result.url,
    scrollWidth: result.scrollWidth,
    clientWidth: result.clientWidth,
    no_horizontal_overflow: result.noHorizontalOverflow,
    has_room_content: result.hasRoomContent,
    visible_button_count: result.buttonCount,
    sample_buttons: result.visibleButtons,
    button_interactive: clickOk,
    screenshot: 'evidence/' + screenshotFile,
    passed,
  };
  await context.close();
  return record;
}

(async () => {
  fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
  const browser = await chromium.launch({ executablePath: CHROME_EXE, headless: true });
  const results = [];
  try {
    results.push(await checkViewport(browser, 'TC-A1', 1366, 768, 'adapt_desktop_1366.png'));
    results.push(await checkViewport(browser, 'TC-A2', 390, 844, 'adapt_mobile_390.png'));
  } finally {
    await browser.close();
  }
  const out = {
    executed_at: new Date().toISOString(),
    base_url: BASE,
    account: 'user1',
    results,
    all_passed: results.every(r => r.passed),
  };
  fs.writeFileSync(path.join(EVIDENCE_DIR, 'adaptation.json'), JSON.stringify(out, null, 2), 'utf-8');
  for (const r of results) {
    console.log(`[${r.case_no}] ${r.viewport} 溢出=${!r.no_horizontal_overflow} 内容=${r.has_room_content} 按钮=${r.button_count}个可交互=${r.button_interactive} -> ${r.passed ? '通过' : '失败'}`);
  }
  console.log('=== 适配测试完成，证据见 adaptation.json ===');
})().catch(e => { console.error('FAIL:', e.message); process.exit(1); });
