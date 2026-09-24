
package com.example.backend.repository;
import com.example.backend.entity.GpfSanctionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;


public interface GpfSanctionOrderRepository
        extends JpaRepository<GpfSanctionOrder, Long> {

            @Modifying
@Query(value = """
INSERT INTO gpf.gpf_sanction_order
(application_id, generated_by, generated_on, order_number, order_pdf, order_text)
VALUES (:applicationId, :generatedBy, :generatedOn, :orderNumber, :orderPdf, :orderText)
""", nativeQuery = true)
void insertSanctionOrder(
    @Param("applicationId") Long applicationId,
    @Param("generatedBy") String generatedBy,
    @Param("generatedOn") LocalDateTime generatedOn,
    @Param("orderNumber") String orderNumber,
    @Param("orderPdf") byte[] orderPdf,
    @Param("orderText") String orderText
);
    Optional<GpfSanctionOrder> findByApplicationId(Long applicationId);

}