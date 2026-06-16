package com.example.ecommerce.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddressJpa extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findByUserUserId(Long userId);
}
