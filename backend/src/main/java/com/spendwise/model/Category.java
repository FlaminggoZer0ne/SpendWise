package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Kategori transaksi; default (milik sistem, ownerId 0) atau kustom (milik member). */
public class Category {

    private int categoryId;
    private String name;
    private TransactionType type;
    private boolean isDefault;
    private int ownerId;

    /** Untuk deserialisasi JSON request pembuatan kategori. */
    private Category() {
    }

    public Category(int categoryId, String name, TransactionType type, boolean isDefault, int ownerId) {
        this.categoryId = categoryId;
        this.name = name;
        this.type = type;
        this.isDefault = isDefault;
        this.ownerId = ownerId;
    }

    public boolean isOwnedBy(int userId) {
        return !isDefault && ownerId == userId;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public TransactionType getType() {
        return type;
    }

    @JsonProperty(value = "isDefault", access = JsonProperty.Access.READ_ONLY)
    public boolean isDefault() {
        return isDefault;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getOwnerId() {
        return ownerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    @JsonIgnore
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
