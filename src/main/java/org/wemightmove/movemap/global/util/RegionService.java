package org.wemightmove.movemap.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;

@Component
@RequiredArgsConstructor
public class RegionService {

    public final RegionTypeRepository regionTypeRepository;

    public String getCodeByCityNameAndDistrictName(String city, String district) {
        return regionTypeRepository.findRegionByNameAndParentName(district, city)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_FAIR)).getPrefix();
    }

    public String getDistrictNameByCode(String code) {
        return regionTypeRepository.findRegionTypeByPrefix(code)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_DISTRICT)).getName();
    }

    public String getCityNameByCode(String code) {
        return regionTypeRepository.findRegionTypeByPrefix(
                parseCityCode(code)
        ).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_CITY)).getName();
    }

    public RegionType getObjectByName(String name) {
        return regionTypeRepository.findRegionByName(name).orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public RegionType getObjectByCode(String code) {
        return regionTypeRepository.findRegionTypeByPrefix(code).orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private String parseCityCode(String regionCode) {
        return regionCode.substring(0, 2);
    }
}
