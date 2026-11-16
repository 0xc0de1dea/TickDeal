
/**
 * TickDeal 주문 관련 화면
 *
 * 사용하는 페이지:
 * - order-processing.html
 * - order-complete.html
 * - order-failed.html
 * - orders.html
 *
 * 공통 함수는 common.js에서 제공:
 * api, token, $, params, won, escapeHtml, toast, API
 */

// ========================================
// 1. 공통 주문 API
// ========================================

async function getOrder(number) {
    if (!number) {
        throw new Error('주문번호가 없습니다.');
    }

    return await api(
        '/orders/' + encodeURIComponent(number)
    );
}

// ========================================
// 2. 주문 상태별 페이지 경로
// ========================================

function orderPage(status) {
    switch (status) {
        case 'COMPLETED':
            return 'order-complete.html';

        case 'FAILED':
        case 'CANCELLED':
        case 'REJECTED':
            return 'order-failed.html';

        case 'PENDING':
        case 'ACCEPTED':
        default:
            return 'order-processing.html';
    }
}

function orderUrl(order) {
    return orderPage(order.status)
        + '?number='
        + encodeURIComponent(order.orderNumber);
}

/**
 * 현재 페이지와 실제 주문 상태가 다를 때만 이동
 */
function redirectToCorrectPage(order) {
    const correctPage = orderPage(order.status);

    const currentPage =
        location.pathname.split('/').pop();

    if (currentPage !== correctPage) {
        location.replace(orderUrl(order));
        return true;
    }

    return false;
}

// ========================================
// 3. 주문 처리 중 화면
// ========================================

async function processing() {
    const target = $('#processing');

    if (!target) return;

    if (!token()) {
        location.href =
            'login.html?next='
            + encodeURIComponent(
                location.pathname + location.search
            );
        return;
    }

    const number = params.get('number');

    if (!number) {
        toast('주문번호가 없습니다.');
        return;
    }

    let stopped = false;
    let polling = false;
    let timer = null;

    /**
     * 서버에서 주문 상태를 다시 조회
     */
    const poll = async () => {
        if (stopped || polling) return;

        polling = true;

        try {
            const order = await getOrder(number);

            // 완료 또는 실패 상태라면 해당 페이지로 이동
            if (redirectToCorrectPage(order)) {
                stopped = true;
                clearInterval(timer);
                return;
            }

            const name = $('#order-name');
            const price = $('#order-price');
            const orderNumber = $('#order-number');
            const message = $('#order-message');

            if (name) {
                name.textContent =
                    order.productName || '주문 상품';
            }

            if (price) {
                price.textContent = won(order.orderPrice);
            }

            if (orderNumber) {
                orderNumber.textContent =
                    order.orderNumber;
            }

            if (message) {
                message.textContent =
                    '현재 주문 상태: ' + order.status;
            }

        } catch (error) {
            const message = $('#order-message');

            if (message) {
                message.textContent =
                    '주문 상태 확인 중: ' + error.message;
            }
        } finally {
            polling = false;
        }
    };

    // 최초 조회
    await poll();

    if (stopped) return;

    // SSE 연결이 끊어져도 2.5초마다 상태 확인
    timer = setInterval(poll, 2500);

    // ========================================
    // SSE 주문 상태 알림
    // ========================================

    const controller = new AbortController();

    window.addEventListener('beforeunload', () => {
        stopped = true;
        clearInterval(timer);
        controller.abort();
    });

    if (!window.ReadableStream) return;

    (async () => {
        try {
            const response = await fetch(
                API + '/notifications/subscribe',
                {
                    method: 'GET',
                    headers: {
                        Authorization:
                            'Bearer ' + token(),
                        Accept: 'text/event-stream'
                    },
                    signal: controller.signal
                }
            );

            if (!response.ok || !response.body) {
                return;
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            let buffer = '';

            while (!stopped) {
                const { done, value } =
                    await reader.read();

                if (done) break;

                buffer += decoder.decode(
                    value,
                    { stream: true }
                );

                // CRLF / LF 모두 처리
                buffer = buffer.replace(/\r\n/g, '\n');

                let index;

                while (
                    (index = buffer.indexOf('\n\n')) >= 0
                    ) {
                    const block = buffer.slice(0, index);

                    buffer = buffer.slice(index + 2);

                    const lines = block.split('\n');

                    const event = lines
                        .find(line =>
                            line.startsWith('event:')
                        )
                        ?.slice(6)
                        .trim();

                    const data = lines
                        .filter(line =>
                            line.startsWith('data:')
                        )
                        .map(line => line.slice(5).trim())
                        .join('\n');

                    if (event !== 'order' || !data) {
                        continue;
                    }

                    try {
                        const payload = JSON.parse(data);

                        if (
                            payload.orderNumber === number
                        ) {
                            await poll();
                        }
                    } catch (error) {
                        console.warn(
                            'SSE 데이터 처리 실패:',
                            error
                        );
                    }
                }
            }

        } catch (error) {
            if (!controller.signal.aborted) {
                console.warn(
                    'SSE 연결 종료, 주기적 조회 유지:',
                    error
                );
            }
        }
    })();
}

// ========================================
// 4. 주문 완료 화면
// ========================================

async function complete() {
    // 처리 중 페이지에서는 실행하지 않음
    const target = $('#order-result');

    if (!target) return;

    const currentPage =
        location.pathname.split('/').pop();

    if (currentPage !== 'order-complete.html') {
        return;
    }

    const number = params.get('number');

    try {
        const order = await getOrder(number);

        // 실제 주문 상태가 COMPLETED가 아니면 이동
        if (redirectToCorrectPage(order)) {
            return;
        }

        target.innerHTML = `
            <div class="status-icon">✅</div>

            <h1>좋은 선택, 주문이 완료됐어요!</h1>

            <p class="muted">
                주문 처리가 정상적으로 완료되었습니다.
            </p>

            <div class="box">
                <b>주문 정보</b>

                <div class="detail-row">
                    <span>주문번호</span>
                    <b>${escapeHtml(order.orderNumber)}</b>
                </div>

                <div class="detail-row">
                    <span>상품</span>
                    <b>${escapeHtml(order.productName)}</b>
                </div>

                <div class="detail-row">
                    <span>수량</span>
                    <b>${escapeHtml(order.quantity)}개</b>
                </div>

                <div class="detail-row">
                    <span>주문 금액</span>
                    <b>${won(order.orderPrice)}</b>
                </div>

                <div class="detail-row">
                    <span>주문 상태</span>
                    <b>COMPLETED</b>
                </div>
            </div>

            <div class="status-actions">
                <a class="btn" href="deals.html">
                    쇼핑 계속하기
                </a>

                <a class="btn btn-red" href="orders.html">
                    주문 내역 확인하기
                </a>
            </div>
        `;

    } catch (error) {
        target.innerHTML = `
            <div class="notice">
                ${escapeHtml(error.message)}
            </div>
        `;
    }
}

// ========================================
// 5. 주문 실패 화면
// ========================================

async function failed() {
    const target = $('#order-result');

    if (!target) return;

    const currentPage =
        location.pathname.split('/').pop();

    if (currentPage !== 'order-failed.html') {
        return;
    }

    const number = params.get('number');

    try {
        const order = await getOrder(number);

        // COMPLETED이면 완료 페이지로 이동
        // PENDING이면 처리 중 페이지로 이동
        if (redirectToCorrectPage(order)) {
            return;
        }

        // 실제 실패 상태인 경우에만 출력
        target.innerHTML = `
            <div class="status-icon">❌</div>

            <h1>주문을 완료하지 못했어요</h1>

            <p class="muted">
                최종 주문 상태:
                ${escapeHtml(order.status)}
            </p>

            <div class="box">
                <b>${escapeHtml(order.productName)}</b>

                <div class="detail-row">
                    <span>주문번호</span>
                    <span>
                        ${escapeHtml(order.orderNumber)}
                    </span>
                </div>

                <div class="detail-row">
                    <span>수량</span>
                    <b>${escapeHtml(order.quantity)}개</b>
                </div>

                <div class="detail-row">
                    <span>주문 금액</span>
                    <b>${won(order.orderPrice)}</b>
                </div>
            </div>

            <div class="status-actions">
                <a class="btn" href="orders.html">
                    주문 내역 보기
                </a>

                <a class="btn btn-red" href="deals.html">
                    다른 딜 보러가기
                </a>
            </div>
        `;

    } catch (error) {
        target.innerHTML = `
            <div class="notice">
                ${escapeHtml(error.message)}
            </div>
        `;
    }
}

// ========================================
// 6. 주문 내역 조회
// ========================================

async function orderList() {
    const target = $('#order-list');

    if (!target) return;

    if (!token()) {
        location.href = 'login.html?next=/orders.html';
        return;
    }

    try {
        const data = await api(
            '/orders?page=0&size=30'
        );

        const list = data?.content || [];

        const count = $('#order-count');

        if (count) {
            count.textContent =
                `총 ${
                    data?.pageInfo?.totalElements
                    ?? list.length
                }건`;
        }

        if (list.length === 0) {
            target.innerHTML = `
                <div class="empty">
                    아직 주문 내역이 없습니다.
                </div>
            `;
            return;
        }

        target.innerHTML = list.map(order => {

            const detailUrl = orderUrl(order);

            return `
                <div class="box" style="margin:14px 0">

                    <div class="split">
                        <b>
                            ${escapeHtml(
                order.createdAt?.slice(0, 10)
                || ''
            )}
                        </b>

                        <span class="red">
                            ${escapeHtml(order.status)}
                        </span>
                    </div>

                    <div class="order-item">

                        <div class="product-art">
                            📦
                        </div>

                        <div style="flex:1">

                            <b>
                                ${escapeHtml(
                order.productName
            )}
                            </b>

                            <div class="muted small">
                                주문번호
                                ${escapeHtml(
                order.orderNumber
            )}
                            </div>

                            <strong>
                                ${won(order.orderPrice)}
                            </strong>

                        </div>

                        <a
                            class="btn"
                            href="${detailUrl}"
                        >
                            상세 보기
                        </a>

                    </div>
                </div>
            `;
        }).join('');

    } catch (error) {
        target.innerHTML = `
            <div class="notice">
                ${escapeHtml(error.message)}
            </div>
        `;
    }
}

// ========================================
// 7. 페이지 초기화
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    processing();
    complete();
    failed();
    orderList();
});
