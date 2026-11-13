
import http from 'k6/http';
import { Counter } from 'k6/metrics';

const accepted = new Counter('order_accepted');
const rejected = new Counter('order_rejected');

const BASE_URL = __ENV.BASE_URL;
const TOKEN = __ENV.TOKEN;
const DEAL_ID = Number(__ENV.DEAL_ID);

export const options = {
    scenarios: {
        concurrency_test: {
            executor: 'per-vu-iterations',
            vus: 100,
            iterations: 1,
            maxDuration: '30s',
            gracefulStop: '0s',
        },
    },
};

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
            timeout: '15s',
        }
    );

    if (response.status === 202) {
        accepted.add(1);
    } else {
        rejected.add(1);

        if (__VU <= 5) {
            console.log(`HTTP ${response.status} | ${response.body}`);
        }
    }
}
