package com.yxtech.smartlife.controller.adminweb;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.adminweb.AddressRequest;
import com.yxtech.smartlife.dto.adminweb.AdminPageRequest;
import com.yxtech.smartlife.dto.adminweb.IdRequest;
import com.yxtech.smartlife.entity.AddressOption;
import com.yxtech.smartlife.repository.AddressOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin-web/addresses")
@RequiredArgsConstructor
public class AdminWebAddressController {

    private final AddressOptionRepository addressOptionRepository;

    @PostMapping("/list")
    public Result<Map<String, Object>> list(@RequestBody AdminPageRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").descending());

        Page<AddressOption> addresses = addressOptionRepository.findAll(pageable);

        List<Map<String, Object>> list = addresses.getContent().stream().map(this::toMap).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", addresses.getTotalElements());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @PostMapping("/create")
    public Result<Map<String, Object>> create(@RequestBody AddressRequest request) {
        String city = request.getCity();
        String district = request.getDistrict();
        String street = request.getStreet();
        String communityName = request.getCommunityName();

        if (city == null || district == null || street == null || communityName == null) {
            throw new IllegalArgumentException("缺少必填字段");
        }

        boolean exists = addressOptionRepository.existsByCityAndDistrictAndStreetAndCommunityNameAndDeletedFalse(
                city, district, street, communityName);
        if (exists) {
            throw new IllegalArgumentException("该地址已存在");
        }

        AddressOption address = new AddressOption();
        address.setCity(city);
        address.setDistrict(district);
        address.setStreet(street);
        address.setCommunityName(communityName);

        addressOptionRepository.save(address);

        return Result.success(toMap(address));
    }

    @PostMapping("/update")
    public Result<Map<String, Object>> update(@RequestBody AddressRequest request) {
        Long id = request.getId();
        AddressOption address = addressOptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("address not found"));

        String communityName = request.getCommunityName();
        if (communityName != null && !communityName.isEmpty()) {
            address.setCommunityName(communityName);
        }

        addressOptionRepository.save(address);

        return Result.success(toMap(address));
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody IdRequest request) {
        Long id = request.getId();
        addressOptionRepository.deleteById(id);
        return Result.success();
    }

    private Map<String, Object> toMap(AddressOption address) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", address.getId());
        map.put("city", address.getCity());
        map.put("district", address.getDistrict());
        map.put("street", address.getStreet());
        map.put("communityName", address.getCommunityName());
        map.put("createdAt", address.getCreatedAt() != null ? address.getCreatedAt().toString() : null);
        return map;
    }
}
