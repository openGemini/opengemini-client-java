# opengemini-client-test-common
一个用于存放单元或集成测试所需的工具类、文件的公共模块。

简体中文 | [English](README.md)

## 目录结构
```
└─src
    └─main
        └─utils         # 测试工具类
        └─resources
           └─jks    #用于TLS相关测试的jks文件，包括keystore.jks和truststore.jks。请参阅“生成JKS文件”以在过期时生成和替换这些文件。
```

## 生成JKS文件
在终端执行以下命令:
<br>
` keytool -genkey -alias [alias] -keyalg RSA -validity 3650 -keystore [output-file]`
<br>
**注意：当命令行提示“输入密钥库口令”，请输入TestOpenGemini@#123（此密码仅作测试用，在生产环境请设置复杂度较高的密码）。**
<br><br>
示例:
<br>
` keytool -genkey -alias test -keyalg RSA -validity 3650 -keystore D:\test.jks`
