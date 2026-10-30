package com.spendwise.core;

/** Kontrak validasi objek sebelum disimpan. */
public interface Validatable {

    /**
     * @return {@code true} jika valid
     * @throws BusinessException berisi pesan yang jelas jika tidak valid
     */
    boolean validate();
}
