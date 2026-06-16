package com.example.ecommerce.address;

import java.util.List;

public interface AddressServiceInterface {
    AddressDTO createAddress(AddressDTO addressDTO);
    List<AddressDTO> getAddressesByUserId(Long userId);
    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);
    void deleteAddress(Long addressId);
}
