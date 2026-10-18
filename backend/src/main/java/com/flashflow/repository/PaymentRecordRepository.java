package com.flashflow.repository;

import com.flashflow.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord> findByPaymentId(String paymentId);

    List<PaymentRecord> findByOrderNumber(String orderNumber);

    Optional<PaymentRecord> findByIdempotencyKey(String idempotencyKey);
}
