package com.monster.monsteraicode;

import com.monster.monsteraicode.ai.AiCodeGeneratorService;
import com.monster.monsteraicode.ai.model.HtmlCodeResult;
import com.monster.monsteraicode.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个程序员的工作记录小工具，行数不超过100");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个程序员的留言板,行数不超过100");
        Assertions.assertNotNull(multiFileCode);
    }
}
