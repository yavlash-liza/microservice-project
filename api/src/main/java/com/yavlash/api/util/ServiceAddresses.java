package com.yavlash.api.util;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ServiceAddresses {
    private String cmp;
    private String pro;
    private String rev;
    private String rec;
}