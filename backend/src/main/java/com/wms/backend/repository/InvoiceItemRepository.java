package com.wms.backend.repository;

import com.wms.backend.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceItemRepository
        extends JpaRepository<InvoiceItem, UUID> {

    List<InvoiceItem> findAllByInvoiceId(UUID invoiceId);
}