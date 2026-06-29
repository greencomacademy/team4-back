package com.deliveryinsider.domain.menus.projections;

import com.deliveryinsider.global.enums.PlatformType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MenuSalesQuantityProjection {

    private Long menuId;

    private PlatformType platformType;

    private Integer salesQuantity;
}
