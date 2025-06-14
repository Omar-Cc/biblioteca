package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.ItemOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {
}