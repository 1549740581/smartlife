package com.yxtech.smartlife.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressTreeNodeDTO {

    private String label;
    private String value;
    private List<AddressTreeNodeDTO> children;
}
