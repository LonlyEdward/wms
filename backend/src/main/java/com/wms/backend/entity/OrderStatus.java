package com.wms.backend.entity;

public enum OrderStatus {
    NEW,
    CONFIRMED,
    PROCESSING,
    DISPATCHED,
    DELIVERED,
    INVOICED,
    CLOSED,
    CANCELLED
}