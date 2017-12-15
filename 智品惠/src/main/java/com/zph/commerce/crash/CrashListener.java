package com.zph.commerce.crash;


import java.io.File;

public interface CrashListener {
    /**
     * 保存异常的日志。
     * 
     * @param file
     */
    public void afterSaveCrash(File file);
}
