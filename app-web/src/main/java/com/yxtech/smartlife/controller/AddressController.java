package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.AddressTreeNodeDTO;
import com.yxtech.smartlife.entity.AddressOption;
import com.yxtech.smartlife.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/tree")
    public Result<List<AddressTreeNodeDTO>> getAddressTree() {
        return Result.success(buildTree(addressService.findAllOptions()));
    }

    private List<AddressTreeNodeDTO> buildTree(List<AddressOption> options) {
        Map<String, CityNode> cityMap = new LinkedHashMap<>();
        for (AddressOption option : options) {
            CityNode cityNode = cityMap.computeIfAbsent(option.getCity(), CityNode::new);
            DistrictNode districtNode = cityNode.districts.computeIfAbsent(option.getDistrict(), DistrictNode::new);
            StreetNode streetNode = districtNode.streets.computeIfAbsent(option.getStreet(), StreetNode::new);
            streetNode.communities.putIfAbsent(
                    option.getCommunityName(),
                    AddressTreeNodeDTO.builder()
                            .label(option.getCommunityName())
                            .value(option.getCommunityName())
                            .children(List.of())
                            .build()
            );
        }

        List<AddressTreeNodeDTO> result = new ArrayList<>();
        for (CityNode cityNode : cityMap.values()) {
            result.add(cityNode.toDto());
        }
        return result;
    }

    private static final class CityNode {
        private final String name;
        private final Map<String, DistrictNode> districts = new LinkedHashMap<>();

        private CityNode(String name) {
            this.name = name;
        }

        private AddressTreeNodeDTO toDto() {
            List<AddressTreeNodeDTO> districtChildren = new ArrayList<>();
            for (DistrictNode districtNode : districts.values()) {
                districtChildren.add(districtNode.toDto());
            }
            return AddressTreeNodeDTO.builder()
                    .label(name)
                    .value(name)
                    .children(districtChildren)
                    .build();
        }
    }

    private static final class DistrictNode {
        private final String name;
        private final Map<String, StreetNode> streets = new LinkedHashMap<>();

        private DistrictNode(String name) {
            this.name = name;
        }

        private AddressTreeNodeDTO toDto() {
            List<AddressTreeNodeDTO> streetChildren = new ArrayList<>();
            for (StreetNode streetNode : streets.values()) {
                streetChildren.add(streetNode.toDto());
            }
            return AddressTreeNodeDTO.builder()
                    .label(name)
                    .value(name)
                    .children(streetChildren)
                    .build();
        }
    }

    private static final class StreetNode {
        private final String name;
        private final Map<String, AddressTreeNodeDTO> communities = new LinkedHashMap<>();

        private StreetNode(String name) {
            this.name = name;
        }

        private AddressTreeNodeDTO toDto() {
            return AddressTreeNodeDTO.builder()
                    .label(name)
                    .value(name)
                    .children(new ArrayList<>(communities.values()))
                    .build();
        }
    }
}
