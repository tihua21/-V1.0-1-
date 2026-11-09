/** 临时诊断：列出 390px 视口下超出视口宽度的元素（用于 BUG-W11-03 定位） */
const { chromium } = require('C:/Users/123/AppData/Local/pnpm/global/5/.pnpm/playwright-core@1.61.1/node_modules/playwright-core');
const CHROME_EXE = 'C:/Users/123/AppData/Local/ms-playwright/chromium-1228/chrome-win64/chrome.exe';

(async () => {
  const browser = await chromium.launch({ executablePath: CHROME_EXE, headless: true });
  const page = await (await browser.newContext({ viewport: { width: 390, height: 844 } })).newPage();
  await page.goto('http://localhost:5173', { waitUntil: 'networkidle' });
  await page.fill('input[placeholder="请输入用户名"]', 'user1');
  await page.fill('input[placeholder="请输入密码"]', '123456');
  await page.click('button.el-button--primary');
  await page.waitForURL('**/rooms', { timeout: 10000 });
  await page.waitForTimeout(1500);

  const info = await page.evaluate(() => {
    const vw = document.documentElement.clientWidth;
    const bad = [];
    document.querySelectorAll('*').forEach(el => {
      const r = el.getBoundingClientRect();
      if (r.width > vw + 1 || r.right > vw + 1) {
        const cls = (el.className && el.className.baseVal !== undefined ? el.className.baseVal : el.className || '').toString().slice(0, 60);
        bad.push({ tag: el.tagName, cls, w: Math.round(r.width), right: Math.round(r.right) });
      }
    });
    return { vw, docScrollW: document.documentElement.scrollWidth, count: bad.length, top: bad.slice(0, 15) };
  });
  console.log(JSON.stringify(info, null, 2));
  await browser.close();
})();
