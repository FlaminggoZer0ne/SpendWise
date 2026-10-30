package com.spendwise.core;

import java.util.List;

/** Kontrak CRUD generik yang di-implement oleh semua DAO. */
public interface Repository<T> {

    boolean save(T item);

    boolean update(T item);

    boolean delete(int id);

    T findById(int id);

    List<T> findAll();
}
