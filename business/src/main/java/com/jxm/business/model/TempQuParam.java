package com.jxm.business.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class TempQuParam {
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long quId;//题目id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tempId;//模板id
    private Integer quType;//题目类型

    private Integer rateValue;//评分值
    @JsonSerialize(using = ToStringSerializer.class)
    private Long radioValue;//单选值
    private String checkValue;//复选值
    private String inputValue;//输入值
}
