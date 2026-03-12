package com.yxtech.smartlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "address_option")
public class AddressOption extends BaseEntity {

    @Column(name = "city", nullable = false, length = 64)
    private String city;

    @Column(name = "district", nullable = false, length = 64)
    private String district;

    @Column(name = "street", nullable = false, length = 128)
    private String street;

    @Column(name = "community_name", nullable = false, length = 128)
    private String communityName;
}
