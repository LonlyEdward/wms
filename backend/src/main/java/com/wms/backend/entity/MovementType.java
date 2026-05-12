package com.wms.backend.entity;

public enum MovementType {
    IN,           // stock received into warehouse
    OUT,          // stock dispatched to customer
    ADJUSTMENT,   // manual correction
    RESERVATION,  // stock reserved against a confirmed order
    RELEASE       // reservation/order cancelled
}