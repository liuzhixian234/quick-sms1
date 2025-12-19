package com.drondea.sms.message;

/**
 * 包类型的抽象类
 *
 * @author 27581
 */
public interface IPackageType {
    /**
     * 获取命令id
     *
     * @return
     */
    int getCommandId();
    /**
     * 获取编解码的类
     *
     * @return
     */
    public ICodec getCodec();
}
