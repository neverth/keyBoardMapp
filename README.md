## keyBoardMapp
一个简单的键盘映射小工具

存在3个模块

服务端
* KeyBoardMappServer.java netty服务端，接收由KeyboardInputClient.html客户端的键盘输入，并映射至KeyboardOutputClient.ja ]va客户端。

客户端
* KeyboardOutputClient.java 接受输入并复现
* KeyboardInputClient.html 检测输入并上传至服务端