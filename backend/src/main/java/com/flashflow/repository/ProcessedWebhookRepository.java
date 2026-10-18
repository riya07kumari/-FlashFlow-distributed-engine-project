package com.flashflow.repository;

import com.flashflow.model.ProcessedWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessedWebhookRepository extends JpaRepository<ProcessedWebhook, String> {

    List<ProcessedWebhook> findByOrderNumber(String orderNumber);
}
