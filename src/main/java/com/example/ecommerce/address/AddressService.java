package com.example.ecommerce.address;

import com.example.ecommerce.exception.AddressNotFoundException;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.user.UserEntity;
import com.example.ecommerce.user.UserJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressService implements AddressServiceInterface {

    @Autowired
    private AddressJpa addressJpa;

    @Autowired
    private UserJpa userJpa;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        UserEntity user = userJpa.findById(addressDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + addressDTO.getUserId()));

        AddressEntity addressEntity = AddressEntity.builder()
                .user(user)
                .addressLine1(addressDTO.getAddressLine1())
                .addressLine2(addressDTO.getAddressLine2())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .pincode(addressDTO.getPincode())
                .country(addressDTO.getCountry())
                .build();

        AddressEntity savedAddress = addressJpa.save(addressEntity);
        return convertToDTO(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        if (!userJpa.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        return addressJpa.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        AddressEntity address = addressJpa.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId));

        address.setAddressLine1(addressDTO.getAddressLine1());
        address.setAddressLine2(addressDTO.getAddressLine2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPincode(addressDTO.getPincode());
        address.setCountry(addressDTO.getCountry());

        AddressEntity updated = addressJpa.save(address);
        return convertToDTO(updated);
    }

    @Override
    public void deleteAddress(Long addressId) {
        AddressEntity address = addressJpa.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId));
        addressJpa.delete(address);
    }

    private AddressDTO convertToDTO(AddressEntity entity) {
        return AddressDTO.builder()
                .addressId(entity.getAddressId())
                .userId(entity.getUser().getUserId())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .pincode(entity.getPincode())
                .country(entity.getCountry())
                .build();
    }
}
