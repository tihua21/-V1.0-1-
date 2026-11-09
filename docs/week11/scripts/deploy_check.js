// TC-D1 部署走查证据：干净目录克隆部署后，登录并截图会议室列表页
const path = require('path');
const { chromium } = require('C:/Users/123/AppData/Local/pnpm/global/5/.pnpm/playwright-core@1.61.1/node_modules/playwright-core');

const CHROME_EXE = 'C:/Users/123/AppData/Local/ms-playwright/chromium-1228/chrome-win64/chrome.exe';
const EVIDENCE = path.join(__dirname, '..', 'evidence');

(async () => {
  const browser = await chromium.launch({ executablePath: CHROME_EXE, headless: true });
  const page = await (await browser.newContext({ viewport: { width: 1366, height: 768 } })).newPage();
  await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle' });
  await page.fill('input[placeholder="请输入用户名"]', 'user1');
  await page.fill('input[placeholder="请输入密码"]', '123456');
  await page.click('button.el-button--primary');
  await page.waitForURL('**/rooms', { timeout: 15000 });
  await page.waitForTimeout(1500);
  const rooms = await page.locator('.el-table__body tbody tr').count();
  await page.screenshot({ path: path.join(EVIDENCE, 'deploy_rooms_page.png'), fullPage: true });
  await browser.close();
  console.log(JSON.stringify({ roomsVisible: rooms }));
})().catch(e => { console.error(e.message); process.exit(1); });
