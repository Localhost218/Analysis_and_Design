package com.ngtukien.demo.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CooperationStatus {
    ACTIVE("Active"), INACTIVE("Inactive");
    private final String label;
}