package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @autuor 范大晨
 * @Date 2023/4/27 20:24
 * @description 课程种类菜单查询接口实现类
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> categoryTreeDtoList = courseCategoryMapper.selectTreeNodes(id);
        //要将查询到的数据进行封装
        //将list转为map 便于查询
        Map<String, CourseCategoryTreeDto> dtoMap = categoryTreeDtoList.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(item -> item.getId(), value -> value, (key1, key2) -> key2));

        List<CourseCategoryTreeDto> list = new ArrayList<>();
        //进行封装处理
        categoryTreeDtoList.stream().filter(item -> !id.equals(item.getId())).forEach(
                item -> {
                    if (id.equals(item.getParentid())) {
                        list.add(item);
                    } else {
                        CourseCategoryTreeDto dto = dtoMap.get(item.getParentid());
                        if (dto!=null) {
                            if (dto.getChildrenTreeNodes() == null) {
                                dto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                            }
                            dto.getChildrenTreeNodes().add(item);
                        }
                    }
                }
        );
        return list;
    }
}
