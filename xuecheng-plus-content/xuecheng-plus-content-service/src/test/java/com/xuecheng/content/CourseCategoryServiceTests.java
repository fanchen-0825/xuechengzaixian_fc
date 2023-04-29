package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import com.xuecheng.content.service.impl.CourseCategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @autuor 范大晨
 * @Date 2023/4/27 20:26
 * @description TODO
 */
@SpringBootTest
public class CourseCategoryServiceTests {

    @Autowired
    private CourseCategoryService service;

    @Autowired
    public  CourseCategoryMapper mapper;


    @Test
    void test() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = service.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
//        List<CourseCategoryTreeDto> courseCategoryTreeDtos = mapper.selectTreeNodes("1");
//        System.out.println(courseCategoryTreeDtos);
    }

    @Test
    void test2() {
        List<CourseCategoryTreeDto> categoryTreeDtoList = mapper.selectAll();
        String id="1";
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
        System.out.println(list);
    }
}
