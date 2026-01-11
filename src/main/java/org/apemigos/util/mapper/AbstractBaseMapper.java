package org.apemigos.util.mapper;

import java.util.List;
import java.util.Set;

public interface AbstractBaseMapper<E, D> {

    D toDto(E entity);

    E toEntity(D dto);

    List<D> toDto(List<E> entities);

    List<E> toEntity(List<D> dtos);

    Set<E> toDto(Set<E> entities);

    Set<E> toEntity(Set<D> dtos);

}
