package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @autuor 范大晨
 * @Date 2023/4/20 16:36
 * @description 分页查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PageParams {
    //当前页码
    @ApiModelProperty(value = "当前页码")
    private Long pageNo = 1L;

    //每页记录数默认值
    @ApiModelProperty(value = "每页记录数")
    private Long pageSize =10L;

}
