package com.monster.monsteraicode.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.monster.monsteraicode.ai.model.enums.CodeGenTypeEnum;
import com.monster.monsteraicode.constant.AppConstant;
import com.monster.monsteraicode.exception.BusinessException;
import com.monster.monsteraicode.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;


/**
 * 抽象代码文件保存器-模板方法模式
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {
    // 文件保存根目录
    protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 保存代码标准流程
     */
    public final File saveCode(T resualt, Long appId) {
        //1.开启输入校验
        validateInput(resualt);
        //2.生成路径

        String baseDirPath = buildUniqueDir(appId);
        //3.写入文件
        saveFiles(resualt, baseDirPath);

        return new File(baseDirPath);
    }



    protected void validateInput(T resualt) {
        if(resualt == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码对象不能为空");
        }
    };

    private String buildUniqueDir(Long appId) {
        if(appId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用Id不能为空");
        }
        String bizType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", bizType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    protected void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 获取代码类型，由子类实现
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件
     *
     */
    protected abstract void saveFiles(T resualt, String baseDirPath);


}
