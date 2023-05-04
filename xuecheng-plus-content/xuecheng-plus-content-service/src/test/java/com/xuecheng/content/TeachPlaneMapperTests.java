package com.xuecheng.content;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/5/1 11:50
 * @description TODO
 */
@SpringBootTest
public class TeachPlaneMapperTests {
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Test
    void selectTreeNodesTest() {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachplanDtos);
    }
}
