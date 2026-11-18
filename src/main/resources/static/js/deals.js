
// QueryDSL 검색 API에서 페이지네이션 처리
const DEAL_PAGE_SIZE = 12;


// 현재 URL에서 검색 상태 가져오기
function dealSearchState() {

    const requestedPage = Number(params.get('page') ?? 0);

    const filterParam = params.get('filter') || 'all';

    return {
        name: (params.get('q') || '').trim(),

        page: Number.isSafeInteger(requestedPage)
        && requestedPage >= 0
            ? requestedPage
            : 0,

        filter: ['all', 'active', 'sold'].includes(filterParam)
            ? filterParam
            : 'all'
    };
}


// 검색 상태에 따른 프론트 URL 생성
function dealSearchUrl({
                           name = '',
                           page = 0,
                           filter = 'all'
                       }) {

    const query = new URLSearchParams();

    if (name) {
        query.set('q', name);
    }

    if (page > 0) {
        query.set('page', String(page));
    }

    if (filter !== 'all') {
        query.set('filter', filter);
    }

    const suffix = query.toString();

    return 'deals.html' + (suffix ? '?' + suffix : '');
}


// 현재 페이지에서 상태별 필터링
function filteredDealsOnPage(deals, filter) {

    if (filter === 'active') {
        return deals.filter(
            d => dealStatus(d) === 'ACTIVE'
        );
    }

    if (filter === 'sold') {
        return deals.filter(
            d => dealStatus(d) === 'SOLD_OUT'
        );
    }

    return deals;
}


// 페이지네이션 렌더링
function renderDealPagination(pageInfo, state) {

    const container = $('#deal-pagination');

    if (!container) return;

    const totalPages = Number(
        pageInfo?.totalPages || 0
    );

    const page = Number(
        pageInfo?.page ?? state.page
    );

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    const start = Math.max(
        0,
        Math.min(page - 2, totalPages - 5)
    );

    const end = Math.min(
        totalPages - 1,
        start + 4
    );

    const parts = [];

    const pageLink = (p, label, current = false) => {

        const href = escapeHtml(
            dealSearchUrl({
                ...state,
                page: p
            })
        );

        const currentAttribute = current
            ? ' aria-current="page"'
            : '';

        return `
            <a href="${href}"${currentAttribute}>
                ${label}
            </a>
        `;
    };

    if (page > 0) {
        parts.push(
            pageLink(page - 1, '이전')
        );
    }

    for (let p = start; p <= end; p++) {

        parts.push(
            pageLink(
                p,
                String(p + 1),
                p === page
            )
        );
    }

    if (page + 1 < totalPages) {
        parts.push(
            pageLink(page + 1, '다음')
        );
    }

    container.innerHTML = parts.join('');
}


// QueryDSL 기반 타임딜 검색
async function loadDeals() {

    const grid = $('#deal-grid');

    if (!grid) return;

    const state = dealSearchState();

    const searchInput = $('#deal-search-input');

    if (searchInput) {
        searchInput.value = state.name;
    }

    // 검색 초기화 버튼
    const clearLink = $('#clear-search');

    if (clearLink) {
        clearLink.classList.toggle(
            'hidden',
            !state.name
        );
    }

    // 상태별 필터 버튼
    document.querySelectorAll(
        '.pill[data-filter]'
    ).forEach(button => {

        button.classList.toggle(
            'active',
            button.dataset.filter === state.filter
        );
    });

    // 검색어 출력
    const description = $('#search-description');

    if (description) {
        description.textContent = state.name
            ? `“${state.name}”에 대한 검색 결과`
            : '모든 타임딜을 표시합니다.';
    }

    const pagination = $('#deal-pagination');

    if (pagination) {
        pagination.innerHTML = '';
    }

    grid.innerHTML = `
        <div class="loading">
            타임딜을 검색하는 중...
        </div>
    `;

    try {

        // 서버로 전달할 검색 조건
        const query = new URLSearchParams({
            name: state.name,
            page: String(state.page),
            size: String(DEAL_PAGE_SIZE)
        });

        // QueryDSL 검색 API 호출
        const result = await api(
            '/deals/search?' + query.toString()
        );

        // 응답 데이터 검증
        if (
            !result ||
            !Array.isArray(result.content) ||
            !result.pageInfo
        ) {
            throw new Error(
                '검색 API 응답 형식이 올바르지 않습니다. '
                + 'data.content와 data.pageInfo를 확인하세요.'
            );
        }

        // 현재 페이지의 상태별 필터
        const matches = filteredDealsOnPage(
            result.content,
            state.filter
        );

        // 전체 검색 결과 개수
        const total = Number(
            result.pageInfo.totalElements || 0
        );

        const countElement = $('#deal-count');

        if (countElement) {
            countElement.textContent =
                `검색 결과 총 ${total.toLocaleString('ko-KR')}개`;
        }

        if (
            description &&
            state.filter !== 'all'
        ) {
            description.textContent +=
                ' · 상태 필터는 현재 페이지에 적용됩니다.';
        }

        // 상품 카드 렌더링
        if (matches.length > 0) {

            grid.innerHTML = matches
                .map(dealCard)
                .join('');

        } else {

            const emptyMessage = state.filter === 'all'
                ? '검색 조건에 맞는 타임딜이 없습니다.'
                : '현재 페이지에서 해당 상태의 타임딜이 없습니다. '
                + '다른 페이지 또는 전체 필터를 확인해 주세요.';

            grid.innerHTML = `
                <div class="empty">
                    ${emptyMessage}
                </div>
            `;
        }

        // 페이지네이션 출력
        renderDealPagination(
            result.pageInfo,
            state
        );

    } catch (error) {

        console.error(
            '딜 검색 실패:',
            error
        );

        const countElement = $('#deal-count');

        if (countElement) {
            countElement.textContent = '딜 검색 실패';
        }

        grid.innerHTML = `
            <div class="notice">
                ${escapeHtml(error.message)}
            </div>
        `;
    }
}


// 기존 타임딜 상세 조회 및 주문 기능
async function loadDetail() {

    const target = $('#detail');

    if (!target) return;

    const id = params.get('id');

    if (!id) {
        target.innerHTML = `
            <div class="notice">
                딜 ID가 없습니다.
            </div>
        `;
        return;
    }

    try {

        // Deal 상세 조회
        const d = await api(
            '/deals/' + encodeURIComponent(id)
        );

        let stock = d.stock;

        // Redis 실시간 재고 조회
        try {

            const inv = await api(
                '/inventory/' + encodeURIComponent(id)
            );

            stock = inv.stock;

        } catch {
            // 재고 API 실패 시 Deal 재고 사용
        }

        // 할인율 계산
        const pct = d.originalPrice
            ? Math.round(
                (1 - d.dealPrice / d.originalPrice) * 100
            )
            : 0;

        const status = dealStatus({
            ...d,
            stock
        });

        // 상세 페이지 렌더링
        target.innerHTML = `
            <div>
                <div class="product-art">
                    ${art(d)}
                </div>

                <p class="muted small">
                    실제 상품 이미지는
                    Product.imageUrl을 등록하면 표시됩니다.
                </p>
            </div>

            <div>
                <div class="card-sub">
                    TickDeal · 한정 타임딜
                </div>

                <h1>
                    ${escapeHtml(d.productName)}
                </h1>

                <p class="muted">
                    실시간 재고와 한정 기간으로 만나는 특별한 가격
                </p>

                <div>
                    <span class="red">
                        ${pct}%
                    </span>

                    <span class="detail-price">
                        ${won(d.dealPrice)}
                    </span>

                    <span class="old">
                        ${won(d.originalPrice)}
                    </span>
                </div>

                <div class="banner">
                    ⏱ 남은 시간
                    <b id="clock">
                        ${remaining(d.endAt)}
                    </b>
                </div>

                <div class="detail-row">
                    <span>남은 수량</span>
                    <b id="stock">
                        ${Number(stock).toLocaleString('ko-KR')}개
                    </b>
                </div>

                <div class="detail-row">
                    <span>판매 상태</span>
                    <b>${escapeHtml(status)}</b>
                </div>

                <div class="detail-row">
                    <span>수량</span>

                    <div class="qty">
                        <button id="minus">−</button>
                        <b id="quantity">1</b>
                        <button id="plus">+</button>
                    </div>
                </div>

                <div class="detail-row">
                    <span>총 주문 금액</span>
                    <b id="total">
                        ${won(d.dealPrice)}
                    </b>
                </div>

                <div class="detail-actions">

                    <a
                        class="btn center"
                        href="deals.html"
                    >
                        목록으로
                    </a>

                    <button
                        class="btn btn-red"
                        id="buy"
                        ${stock <= 0 ? 'disabled' : ''}
                    >
                        ${stock <= 0 ? '품절' : '지금 주문하기'}
                    </button>
                </div>

                <p class="muted small">
                    주문은 Kafka로 비동기 처리되며
                    최종 상태를 별도로 확인합니다.
                </p>
            </div>
        `;

        let qty = 1;

        // 주문 수량과 가격 갱신
        const refresh = () => {

            $('#quantity').textContent = qty;

            $('#total').textContent = won(
                d.dealPrice * qty
            );
        };

        // 수량 감소
        $('#minus').onclick = () => {

            qty = Math.max(
                1,
                qty - 1
            );

            refresh();
        };

        // 수량 증가
        $('#plus').onclick = () => {

            qty = Math.min(
                Math.max(1, stock),
                qty + 1
            );

            refresh();
        };

        // 주문하기
        $('#buy').onclick = async () => {

            if (!token()) {

                location.href =
                    'login.html?next='
                    + encodeURIComponent(
                        location.pathname + location.search
                    );

                return;
            }

            $('#buy').disabled = true;
            $('#buy').textContent = '주문 접수 중...';

            try {

                // Kafka 비동기 주문 접수
                const order = await api('/orders', {
                    method: 'POST',
                    body: JSON.stringify({
                        dealId: Number(id),
                        quantity: qty
                    })
                });

                if (!order.orderNumber) {
                    throw Error(
                        '주문번호가 응답에 없습니다.'
                    );
                }

                // 주문 처리 상태 페이지 이동
                location.href =
                    'order-processing.html?number='
                    + encodeURIComponent(
                        order.orderNumber
                    );

            } catch (error) {

                $('#buy').disabled = false;

                $('#buy').textContent =
                    '지금 주문하기';

                toast(error.message);
            }
        };

        // 남은 시간 1초마다 갱신
        setInterval(() => {

            if ($('#clock')) {

                $('#clock').textContent =
                    remaining(d.endAt);
            }

        }, 1000);

    } catch (error) {

        target.innerHTML = `
            <div class="notice">
                ${escapeHtml(error.message)}
            </div>
        `;
    }
}


// 페이지 초기화
document.addEventListener('DOMContentLoaded', () => {

    // 목록 페이지일 경우
    if ($('#deal-grid')) {

        const form = $('#deal-search-form');

        // 검색 버튼 / Enter 키
        form?.addEventListener('submit', event => {

            event.preventDefault();

            const name = (
                $('#deal-search-input')?.value || ''
            ).trim();

            location.href = dealSearchUrl({
                name,
                page: 0,
                filter: 'all'
            });
        });

        // 상태별 필터 버튼
        document.querySelectorAll(
            '.pill[data-filter]'
        ).forEach(button => {

            button.addEventListener('click', () => {

                const state = dealSearchState();

                location.href = dealSearchUrl({
                    ...state,
                    filter: button.dataset.filter
                });
            });
        });

        // 검색 목록 로드
        loadDeals();
    }

    // 상세 페이지인 경우
    loadDetail();
});
