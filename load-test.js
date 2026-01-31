import http from 'k6/http';
import { check, sleep } from 'k6';

// 설정
export const options = {
  // 부하 시나리오
  stages: [
    { duration: '10s', target: 500 }, // 처음 10초 동안 500명까지 서서히 증가 (Ramp-up)
    { duration: '30s', target: 1000 }, // 이후 30초 동안 1000명 유지 (Peak Load)
    { duration: '10s', target: 0 },   // 마지막 10초 동안 0명으로 감소 (Ramp-down)
  ],
  // 임계값 설정
  thresholds: {
    http_req_duration: ['p(95)<100'], // 95%의 요청이 100ms 이내여야 함
  },
};

// 가상 유저(VU)가 수행할 행동
export default function () {
  const url = 'http://localhost:8080/api/v1/queue';

  // 유저 ID를 랜덤 혹은 순차적으로 생성 (Redis Key 충돌 방지 및 리얼한 상황 연출)
  // __VU: 가상 유저 번호, __ITER: 반복 횟수
  const userId = `user_${__VU}_${__ITER}`;

  const payload = JSON.stringify({
    userId: userId,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // POST 요청
  const res = http.post(url, payload, params);

  // 검증 (응답 코드가 200인지 확인)
  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  // 너무 미친듯이 요청하면 로컬 PC가 뻗을 수 있으므로 0.1초 정도 텀을 줌 (선택 사항)
  sleep(0.1);
}