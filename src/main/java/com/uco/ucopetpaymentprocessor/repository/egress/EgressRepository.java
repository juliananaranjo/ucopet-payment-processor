package com.uco.ucopetpaymentprocessor.repository.egress;

import com.uco.ucopetpaymentprocessor.domain.egress.EgressDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EgressRepository extends JpaRepository<EgressDomain, UUID> {
}