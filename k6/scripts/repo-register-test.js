import http from 'k6/http';
import {check, sleep} from 'k6';
import {Trend, Counter} from 'k6/metrics';

const JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjYsImlhdCI6MTc3MjU1MDg1NywiZXhwIjoxNzczMTU1NjU3fQ.-kNKsRkNkc67pNCN566UHEzR_3aDaAIRBuwtjWU6R7E";

const BASE_URL = "http://3.36.97.19:8080"

const REPO_NAME = "Java-Algorithm";

// 레포 등록 API의 응답 시간 추적
const registerDuration = new Trend("repo_register_duration");

// 성공/실패 카운트
const successCount = new Counter("register_success");
const failCount = new Counter("register_fail");

export const options = {
  vus: 1,
  iterations: 1,
}

export default function () {
  const url = `${BASE_URL}/auth/repo`;

  const payload = JSON.stringify({
    name: REPO_NAME,
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${JWT_TOKEN}`,
    },
    timeout: "300s",

  };

  const startTime = Date.now();

  const res = http.post(url, payload, params);

  const duration = Date.now() - startTime;

  registerDuration.add(duration);

  const passed = check(res, {
    "status is 200": (r) => r.status === 200,
    "response has success": (r) => r.body && r.body.includes("완료"),
  });

  if (passed) {
    successCount.add(1);
  } else {
    failCount.add(1);
    console.log(`실패 — status: ${res.status}, body: ${res.body}`);
  }

  console.log(`응답 시간: ${duration}ms, status: ${res.status}`);
}