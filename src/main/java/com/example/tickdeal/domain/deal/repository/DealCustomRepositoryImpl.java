package com.example.tickdeal.domain.deal.repository;

import com.example.tickdeal.domain.deal.dto.DealDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example.tickdeal.domain.deal.entity.QDeal.deal;

@RequiredArgsConstructor
public class DealCustomRepositoryImpl implements DealCustomRepository {

    private final JPAQueryFactory queryFactory;

    private BooleanExpression nameCondition(String title) {
        return title != null ? deal.product.name.contains(title) : null;
    }

    @Override
    public Page<DealDto.Response> searchDealByMultiCondition(String name, Pageable pageable) {
        List<DealDto.Response> response = queryFactory
                .select(
                        Projections.constructor(
                                DealDto.Response.class,
                                deal.id,
                                deal.product.id,
                                deal.product.name,
                                deal.product.price,
                                deal.dealPrice,
                                deal.stock,
                                deal.startAt,
                                deal.endAt,
                                deal.status,
                                deal.createdAt,
                                deal.modifiedAt
                        )
                )
                .from(deal)
                .where(
                        nameCondition(name)
                )
                .orderBy(deal.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(deal.count())
                .from(deal)
                .where(nameCondition(name))
                .fetchOne();

        return new PageImpl<>(
                response,
                pageable,
                total != null ? total : 0L
        );
    }
}
