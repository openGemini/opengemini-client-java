# opengemini-client-test-common

A common module for storing tools and files required for unit or integration testing.

English | [简体中文](README_CN.md)

## Structure

```
└─src
    └─main
        └─utils         # test utils
        └─resources
           └─jks        # jks files for tls testing, include keystore.jks and truststore.jks. See "Generate JKS File" to generate and replace these files if expires.
```

## Generate JKS File

Execute the following command in terminal:
<br>
` keytool -genkey -alias [alias] -keyalg RSA -validity 3650 -keystore [output-file]`
<br>
**Note: When the command line prompts "Enter Keystore Password", please enter TestOpenGemini@#123 (This password is for
testing purposes only, please set a password with a high complexity in the production environment)**
<br><br>
example:
<br>
` keytool -genkey -alias test -keyalg RSA -validity 3650 -keystore D:\test.jks`
