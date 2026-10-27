package com.uco.ucopetpaymentprocessor.repository.invoice;

import com.uco.ucopetpaymentprocessor.domain.invoice.InvoiceDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceDomain, UUID> {

    boolean existsBySaleOrderId(UUID saleOrderId);

    Optional<InvoiceDomain> findTopByHeadquarterIdOrderByInvoiceNumberDesc(UUID headquarterId);
}