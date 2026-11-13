
import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// ================================
// 환경 변수
// ================================
const BASE_URL = __ENV.BASE_URL;
const TOKEN = __ENV.TOKEN;
const DEAL_ID = Number(__ENV.DEAL_ID);

// ================================
// 커스텀 메트릭
// ================================
const acceptedOrders = new Counter('accepted_orders');
const stockRejected = new Counter('stock_rejected_orders');
const unexpectedResponses = new Counter('unexpected_responses');

const orderSuccessRate = new Rate('order_success_rate');
const serverErrorRate = new Rate('server_error_rate');
const orderLatency = new Trend('order_latency');

// ================================
// 테스트 설정
// ================================
export const options = {
    scenarios: {
        load_test: {
            executor: 'ramping-vus',

            startVUs: 0,

            stages: [
                { duration: '10s', target: 20 },
                { duration: '20s', target: 100 },
                { duration: '30s', target: 100 },
                { duration: '10s', target: 0 },
            ],

            gracefulRampDown: '5s',
        },
    },

    thresholds: {
        http_req_duration: ['p(95)<1000', 'p(99)<2000'],
        server_error_rate: ['rate<0.01'],
    },
};

// ================================
// 주문 요청
// ================================
export default function () {
    const response = http.post(
        `${BASE_URL}/api/orders`,
        JSON.stringify({
            dealId: DEAL_ID,
            quantity: 1,
        }),
        {
            headers: {
                Authorization: `Bearer ${TOKEN}`,
                'Content-Type': 'application/json',
            },
            timeout: '10s',
        }
    );

    const success = response.status === 202;
    const stockFailure = response.status === 409;
    const serverFailure = response.status >= 500;

    if (success) {
        acceptedOrders.add(1);
    } else if (stockFailure) {
        stockRejected.add(1);
    } else {
        unexpectedResponses.add(1);
    }

    orderSuccessRate.add(success);
    serverErrorRate.add(serverFailure);
    orderLatency.add(response.timings.duration);

    check(response, {
        'order accepted or stock rejected': (r) =>
            r.status === 202 || r.status === 409,
    });
}
