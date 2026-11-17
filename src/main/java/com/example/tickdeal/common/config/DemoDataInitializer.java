package com.example.tickdeal.common.config;

import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.repository.DealRepository;
import com.example.tickdeal.domain.inventory.service.InventoryService;
import com.example.tickdeal.domain.product.entity.Product;
import com.example.tickdeal.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "tickdeal.demo",
        name = "enabled",
        havingValue = "true"
)
public class DemoDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final DealRepository dealRepository;
    private final InventoryService inventoryService;

    private record DemoProduct(
            String name,
            String description,
            long originalPrice,
            long dealPrice,
            int stock,
            String image
    ) {}

    @Override
    public void run(String... args) {

        // 기존 샘플 데이터가 있다면 다시 생성하지 않음
        boolean alreadyExists = productRepository.findAll()
                .stream()
                .anyMatch(p -> p.getName().startsWith("[DEMO]"));

        if (alreadyExists) {
            System.out.println("TickDeal 샘플 데이터가 이미 존재합니다.");
            return;
        }

        List<DemoProduct> samples = List.of(
                new DemoProduct(
                        "소노 무선 노이즈 캔슬링 헤드폰",
                        "프리미엄 사운드와 편안한 착용감",
                        198000, 99000, 500,
                        "/images/demo/headphones.jpg"
                ),
                new DemoProduct(
                        "루미 프리미엄 테이블 램프",
                        "공간에 따뜻함을 더하는 감성 조명",
                        69000, 39000, 35,
                        "/images/demo/lamp.jpg"
                ),
                new DemoProduct(
                        "모노 미니 블루투스 스피커",
                        "작은 크기에 담긴 풍부한 사운드",
                        89000, 49000, 40,
                        "/images/demo/speaker.jpg"
                ),
                new DemoProduct(
                        "모노 온도조절 전기포트 0.8L",
                        "정밀 온도 조절이 가능한 전기포트",
                        99000, 59000, 30,
                        "/images/demo/kettle.jpg"
                ),
                new DemoProduct(
                        "클라우드 무선 마우스",
                        "부드러운 그립감의 무선 마우스",
                        39000, 24900, 45,
                        "/images/demo/mouse.jpg"
                ),
                new DemoProduct(
                        "오브 스테인리스 텀블러 500ml",
                        "오래 유지되는 보온과 보냉",
                        35000, 18900, 60,
                        "/images/demo/tumbler.jpg"
                ),
                new DemoProduct(
                        "포레스트 디퓨저 200ml",
                        "은은한 향기가 머무는 공간",
                        45000, 22900, 10,
                        "/images/demo/diffuser.jpg"
                ),
                new DemoProduct(
                        "루프 스마트 충전기 65W",
                        "작고 빠른 멀티 충전기",
                        59000, 29000, 50,
                        "/images/demo/charger.jpg"
                )
        );

        LocalDateTime now = LocalDateTime.now();

        for (DemoProduct sample : samples) {

            Product product = productRepository.save(
                    new Product(
                            "[DEMO] " + sample.name(),
                            sample.description(),
                            sample.originalPrice(),
                            sample.image()
                    )
            );

            Deal deal = new Deal(
                    product,
                    sample.dealPrice(),
                    sample.stock(),
                    now.minusMinutes(5),
                    now.plusDays(7)
            );

            // 생성자의 기본 상태는 SCHEDULED
            deal.activate();

            Deal savedDeal = dealRepository.save(deal);

            inventoryService.initializeStock(
                    savedDeal.getId(),
                    savedDeal.getStock()
            );

            System.out.println(
                    "샘플 딜 등록: " + sample.name()
                            + " / Deal ID: " + savedDeal.getId()
            );
        }

        System.out.println("TickDeal 샘플 데이터 8개 생성 완료!");
    }
}
