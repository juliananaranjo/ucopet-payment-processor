package com.uco.ucopetpaymentprocessor.repository.paymethod;

import com.uco.ucopetpaymentprocessor.domain.paymethod.PayMethodDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayMethodRepository extends JpaRepository<PayMethodDomain, UUID> {

    Optional<PayMethodDomain> findByName(String name);
}
