package com.monster.monsteraicode.core.saver;

import cn.hutool.core.util.StrUtil;
import com.monster.monsteraicode.ai.model.HtmlCodeResult;
import com.monster.monsteraicode.ai.model.enums.CodeGenTypeEnum;
import com.monster.monsteraicode.exception.BusinessException;
import com.monster.monsteraicode.exception.ErrorCode;

public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());

    }

    @Override
    protected void validateInput(HtmlCodeResult result){
        super.validateInput(result);
        //代码不能为空
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }

    }
}
