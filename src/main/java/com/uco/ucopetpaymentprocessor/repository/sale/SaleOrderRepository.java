package com.uco.ucopetpaymentprocessor.repository.sale;

import com.uco.ucopetpaymentprocessor.domain.sale.SaleOrderDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrderDomain, UUID> {
}