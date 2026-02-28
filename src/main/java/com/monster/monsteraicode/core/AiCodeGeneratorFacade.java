package com.monster.monsteraicode.core;

import com.monster.monsteraicode.ai.AiCodeGeneratorService;
import com.monster.monsteraicode.ai.model.HtmlCodeResult;
import com.monster.monsteraicode.ai.model.MultiFileCodeResult;
import com.monster.monsteraicode.ai.model.enums.CodeGenTypeEnum;
import com.monster.monsteraicode.core.parser.CodeParserExecutor;
import com.monster.monsteraicode.core.saver.CodeFileSaverExecutor;
import com.monster.monsteraicode.core.saver.CodeFileSaverTemplate;
import com.monster.monsteraicode.exception.BusinessException;
import com.monster.monsteraicode.exception.ErrorCode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;
import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */


@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield  processCodeStream(result, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用代码流式处理
     *
     * @param result 代码流
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    private Flux<String> processCodeStream(Flux<String> result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(chunk -> {
                    // 实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    try {
                        String completeMultiFileCode = codeBuilder.toString();
                        Object fileResult = CodeParserExecutor.executeParser(completeMultiFileCode, codeGenTypeEnum);
                        // 保存代码到文件
                        File savedDir = CodeFileSaverExecutor.executeSaver(fileResult, codeGenTypeEnum, appId);
                        log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败: {}", e.getMessage());
                    }
                });
    }



}
