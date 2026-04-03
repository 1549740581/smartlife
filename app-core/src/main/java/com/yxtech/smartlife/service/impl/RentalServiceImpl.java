package com.yxtech.smartlife.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.HouseDetail;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.ReviewRecord;
import com.yxtech.smartlife.exception.NotFoundException;
import com.yxtech.smartlife.repository.HouseDetailRepository;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.ReviewRecordRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.AddressService;
import com.yxtech.smartlife.service.RentalService;
import com.yxtech.smartlife.service.command.CreateHouseDetailCommand;
import com.yxtech.smartlife.service.command.CreateRentalCommand;
import com.yxtech.smartlife.service.command.UpdateRentalCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalInfoRepository rentalInfoRepository;
    private final HouseDetailRepository houseDetailRepository;
    private final ReviewRecordRepository reviewRecordRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RentalInfo createRental(CreateRentalCommand command) {
        userRepository.findByIdAndDeletedFalse(command.getPublisherUserId())
                .orElseThrow(() -> new NotFoundException("publisher user not found"));

        String city = normalizeCity(command.getCity());
        String district = trimToNull(command.getDistrict());
        String street = trimToNull(command.getStreet());
        String communityName = trimToNull(command.getCommunityName());
        if (!addressService.exists(city, district, street, communityName)) {
            throw new IllegalArgumentException("selected address is not supported");
        }

        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setPublisherUserId(command.getPublisherUserId());
        rentalInfo.setRentalType(command.getRentalType());
        rentalInfo.setTitle(trimToNull(command.getTitle()));
        rentalInfo.setDescription(trimToNull(command.getDescription()));
        rentalInfo.setPrice(command.getPrice());
        rentalInfo.setContactName(trimToNull(command.getContactName()));
        rentalInfo.setContactPhone(trimToNull(command.getContactPhone()));
        rentalInfo.setCity(city);
        rentalInfo.setDistrict(district);
        rentalInfo.setStreet(street);
        rentalInfo.setCommunityName(communityName);
        rentalInfo.setImageUrls(toJson(command.getImageUrls()));
        rentalInfo.setStatus(Boolean.TRUE.equals(command.getIsDraft()) 
                ? RentalInfo.RentalStatus.DRAFT 
                : RentalInfo.RentalStatus.PENDING);
        RentalInfo saved = rentalInfoRepository.save(rentalInfo);

        if (command.getRentalType() == RentalInfo.RentalType.HOUSE) {
            saveHouseDetail(saved.getId(), command.getHouseDetail());
        }

        return saved;
    }

    @Override
    @Transactional
    public RentalInfo updateRental(UpdateRentalCommand command) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndPublisherUserIdAndDeletedFalse(
                        command.getRentalId(), command.getUserId())
                .orElseThrow(() -> new NotFoundException("租赁信息不存在"));

        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.DRAFT 
                && rentalInfo.getStatus() != RentalInfo.RentalStatus.REJECTED) {
            throw new IllegalArgumentException("只有草稿或被驳回的信息可以编辑");
        }

        String city = normalizeCity(command.getCity());
        String district = trimToNull(command.getDistrict());
        String street = trimToNull(command.getStreet());
        String communityName = trimToNull(command.getCommunityName());
        if (!addressService.exists(city, district, street, communityName)) {
            throw new IllegalArgumentException("所选地址不支持");
        }

        rentalInfo.setRentalType(command.getRentalType());
        rentalInfo.setTitle(trimToNull(command.getTitle()));
        rentalInfo.setDescription(trimToNull(command.getDescription()));
        rentalInfo.setPrice(command.getPrice());
        rentalInfo.setContactName(trimToNull(command.getContactName()));
        rentalInfo.setContactPhone(trimToNull(command.getContactPhone()));
        rentalInfo.setCity(city);
        rentalInfo.setDistrict(district);
        rentalInfo.setStreet(street);
        rentalInfo.setCommunityName(communityName);
        rentalInfo.setImageUrls(toJson(command.getImageUrls()));
        rentalInfo.setStatus(Boolean.TRUE.equals(command.getIsDraft()) 
                ? RentalInfo.RentalStatus.DRAFT 
                : RentalInfo.RentalStatus.PENDING);
        rentalInfo.setRejectReason(null);
        RentalInfo saved = rentalInfoRepository.save(rentalInfo);

        if (command.getRentalType() == RentalInfo.RentalType.HOUSE) {
            updateHouseDetail(saved.getId(), command.getHouseDetail());
        } else {
            deleteHouseDetailIfExists(saved.getId());
        }

        return saved;
    }

    private void updateHouseDetail(Long rentalInfoId, CreateHouseDetailCommand cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("房屋类型必须填写房屋详情");
        }
        validateHouseDetail(cmd);

        HouseDetail detail = houseDetailRepository.findByRentalInfoIdAndDeletedFalse(rentalInfoId)
                .orElseGet(() -> {
                    HouseDetail newDetail = new HouseDetail();
                    newDetail.setRentalInfoId(rentalInfoId);
                    return newDetail;
                });

        detail.setFloor(cmd.getFloor());
        detail.setBedroomCount(cmd.getBedroomCount());
        detail.setLivingRoomCount(cmd.getLivingRoomCount());
        detail.setKitchenCount(cmd.getKitchenCount());
        detail.setBathroomCount(cmd.getBathroomCount());
        detail.setOrientation(cmd.getOrientation());
        detail.setHasBalcony(cmd.getHasBalcony());
        detail.setAppliances(toJson(cmd.getAppliances()));
        detail.setHasElevator(cmd.getHasElevator());
        detail.setPropertyFee(cmd.getPropertyFee());
        detail.setWaterFee(cmd.getWaterFee());
        detail.setElectricityFee(cmd.getElectricityFee());
        detail.setExtraInfo(trimToNull(cmd.getExtraInfo()));
        houseDetailRepository.save(detail);
    }

    private void deleteHouseDetailIfExists(Long rentalInfoId) {
        houseDetailRepository.findByRentalInfoIdAndDeletedFalse(rentalInfoId)
                .ifPresent(detail -> {
                    detail.setDeleted(true);
                    houseDetailRepository.save(detail);
                });
    }

    private void saveHouseDetail(Long rentalInfoId, CreateHouseDetailCommand cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("房屋类型必须填写房屋详情");
        }
        validateHouseDetail(cmd);

        HouseDetail detail = new HouseDetail();
        detail.setRentalInfoId(rentalInfoId);
        detail.setFloor(cmd.getFloor());
        detail.setBedroomCount(cmd.getBedroomCount());
        detail.setLivingRoomCount(cmd.getLivingRoomCount());
        detail.setKitchenCount(cmd.getKitchenCount());
        detail.setBathroomCount(cmd.getBathroomCount());
        detail.setOrientation(cmd.getOrientation());
        detail.setHasBalcony(cmd.getHasBalcony());
        detail.setAppliances(toJson(cmd.getAppliances()));
        detail.setHasElevator(cmd.getHasElevator());
        detail.setPropertyFee(cmd.getPropertyFee());
        detail.setWaterFee(cmd.getWaterFee());
        detail.setElectricityFee(cmd.getElectricityFee());
        detail.setExtraInfo(trimToNull(cmd.getExtraInfo()));
        houseDetailRepository.save(detail);
    }

    private void validateHouseDetail(CreateHouseDetailCommand cmd) {
        if (cmd.getFloor() == null || cmd.getFloor() < 0 || cmd.getFloor() > 40) {
            throw new IllegalArgumentException("楼层必须在0~40之间");
        }
        if (cmd.getBedroomCount() == null || cmd.getBedroomCount() < 0) {
            throw new IllegalArgumentException("请填写卧室数量");
        }
        if (cmd.getLivingRoomCount() == null || cmd.getLivingRoomCount() < 0) {
            throw new IllegalArgumentException("请填写客厅数量");
        }
        if (cmd.getKitchenCount() == null || cmd.getKitchenCount() < 0) {
            throw new IllegalArgumentException("请填写厨房数量");
        }
        if (cmd.getBathroomCount() == null || cmd.getBathroomCount() < 0) {
            throw new IllegalArgumentException("请填写卫生间数量");
        }
        if (cmd.getOrientation() == null) {
            throw new IllegalArgumentException("请选择朝向");
        }
        if (cmd.getHasBalcony() == null) {
            throw new IllegalArgumentException("请选择是否有阳台");
        }
        if (cmd.getAppliances() == null || cmd.getAppliances().isEmpty()) {
            throw new IllegalArgumentException("请选择家电家具");
        }
        if (cmd.getAppliances().contains(HouseDetail.Appliance.NONE) && cmd.getAppliances().size() > 1) {
            throw new IllegalArgumentException("选择【无】时不能选择其他家电家具");
        }
        if (cmd.getHasElevator() == null) {
            throw new IllegalArgumentException("请选择是否有电梯");
        }
        if (cmd.getPropertyFee() == null || cmd.getPropertyFee().signum() < 0) {
            throw new IllegalArgumentException("物业费不能为负数");
        }
        if (cmd.getWaterFee() == null) {
            throw new IllegalArgumentException("请填写水费");
        }
        if (cmd.getElectricityFee() == null) {
            throw new IllegalArgumentException("请填写电费");
        }
    }

    @Override
    public List<RentalInfo> findPublicRentals() {
        return searchPublicRentals(null, null, null, null, null, null);
    }

    @Override
    public List<RentalInfo> searchPublicRentals(
            String keyword,
            RentalInfo.RentalType rentalType,
            String city,
            String district,
            String street,
            String communityName
    ) {
        return rentalInfoRepository.searchPublicRentals(
                RentalInfo.RentalStatus.APPROVED,
                trimToNull(keyword),
                rentalType,
                trimToNull(city),
                trimToNull(district),
                trimToNull(street),
                trimToNull(communityName)
        );
    }

    @Override
    public RentalInfo findPublicRentalById(Long rentalId) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));
        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.APPROVED) {
            throw new NotFoundException("rental not found");
        }
        return rentalInfo;
    }

    @Override
    public List<RentalInfo> findPublicRentalsByType(RentalInfo.RentalType rentalType) {
        return searchPublicRentals(null, rentalType, null, null, null, null);
    }

    @Override
    public List<RentalInfo> findUserRentals(Long userId) {
        return rentalInfoRepository.findByPublisherUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public RentalInfo findUserRentalById(Long userId, Long rentalId) {
        return rentalInfoRepository.findByIdAndPublisherUserIdAndDeletedFalse(rentalId, userId)
                .orElseThrow(() -> new NotFoundException("rental not found"));
    }

    @Override
    public List<RentalInfo> findAllRentals() {
        return rentalInfoRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    public RentalInfo findRentalById(Long rentalId) {
        return rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));
    }

    @Override
    public List<RentalInfo> findPendingRentals() {
        return rentalInfoRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(RentalInfo.RentalStatus.PENDING);
    }

    @Override
    @Transactional
    public RentalInfo reviewRental(Long rentalId, Long adminId, boolean approved, String reason) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));

        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.PENDING) {
            throw new IllegalArgumentException("only pending rental can be reviewed");
        }
        if (!approved && !StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("reject reason must not be blank");
        }

        RentalInfo.RentalStatus fromStatus = rentalInfo.getStatus();
        rentalInfo.setReviewedBy(adminId);
        rentalInfo.setReviewedAt(LocalDateTime.now());
        if (approved) {
            rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
            rentalInfo.setRejectReason(null);
        } else {
            rentalInfo.setStatus(RentalInfo.RentalStatus.REJECTED);
            rentalInfo.setRejectReason(reason);
        }
        RentalInfo saved = rentalInfoRepository.save(rentalInfo);

        saveReviewRecord(saved.getId(), approved ? "APPROVE" : "REJECT", fromStatus, saved.getStatus(), reason, adminId);
        return saved;
    }

    @Override
    @Transactional
    public RentalInfo userOnlineRental(Long rentalId, Long userId) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndPublisherUserIdAndDeletedFalse(rentalId, userId)
                .orElseThrow(() -> new NotFoundException("rental not found"));

        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.OFFLINE) {
            throw new IllegalArgumentException("only offline rental can be online by owner");
        }

        RentalInfo.RentalStatus fromStatus = rentalInfo.getStatus();
        rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
        RentalInfo saved = rentalInfoRepository.save(rentalInfo);

        // 记录一条用户重新上架操作审计
        saveReviewRecord(saved.getId(), "USER_ONLINE", fromStatus, saved.getStatus(), null, userId);
        return saved;
    }

    @Override
    @Transactional
    public RentalInfo userOfflineRental(Long rentalId, Long userId) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndPublisherUserIdAndDeletedFalse(rentalId, userId)
                .orElseThrow(() -> new NotFoundException("rental not found"));

        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.APPROVED) {
            throw new IllegalArgumentException("only approved rental can be offline by owner");
        }

        RentalInfo.RentalStatus fromStatus = rentalInfo.getStatus();
        rentalInfo.setStatus(RentalInfo.RentalStatus.OFFLINE);
        RentalInfo saved = rentalInfoRepository.save(rentalInfo);

        // 记录一条用户下架操作审计
        saveReviewRecord(saved.getId(), "USER_OFFLINE", fromStatus, saved.getStatus(), null, userId);
        return saved;
    }

    @Override
    @Transactional
    public RentalInfo offlineRental(Long rentalId, Long adminId, String reason) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));

        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.APPROVED) {
            throw new IllegalArgumentException("only approved rental can be offline");
        }

        RentalInfo.RentalStatus fromStatus = rentalInfo.getStatus();
        rentalInfo.setStatus(RentalInfo.RentalStatus.OFFLINE);
        rentalInfo.setReviewedBy(adminId);
        rentalInfo.setReviewedAt(LocalDateTime.now());
        rentalInfo.setRejectReason(reason);
        RentalInfo saved = rentalInfoRepository.save(rentalInfo);

        saveReviewRecord(saved.getId(), "OFFLINE", fromStatus, saved.getStatus(), reason, adminId);
        return saved;
    }

    private void saveReviewRecord(
            Long rentalId,
            String action,
            RentalInfo.RentalStatus fromStatus,
            RentalInfo.RentalStatus toStatus,
            String reason,
            Long adminId
    ) {
        ReviewRecord reviewRecord = new ReviewRecord();
        reviewRecord.setRentalInfoId(rentalId);
        reviewRecord.setAction(action);
        reviewRecord.setFromStatus(fromStatus.name());
        reviewRecord.setToStatus(toStatus.name());
        reviewRecord.setReason(reason);
        reviewRecord.setOperatorId(adminId);
        reviewRecordRepository.save(reviewRecord);
    }

    private <T> String toJson(List<T> list) {
        List<T> safeList = list == null ? Collections.emptyList() : list;
        try {
            return objectMapper.writeValueAsString(safeList);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("json serialization failed");
        }
    }

    private String normalizeCity(String city) {
        String normalized = trimToNull(city);
        if (!StringUtils.hasText(normalized)) {
            return "杭州";
        }
        if (!"杭州".equals(normalized)) {
            throw new IllegalArgumentException("currently only Hangzhou is supported");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
