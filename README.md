####挡板系统
#####1，基本介绍
本系统面向开发者，开发阶段调试接口使用。当后端系统的接口无法调用时，可以先通过对方提供的接口文档，配置模拟返回数据，调试自己的代码，而无需等待对方开发完成接口。

#####2，参数介绍
`jmock.tcpserver.port`：本系统监听的端口

`jmock.packet.mode`：没有模拟报文存在时的处理方式。0-抛错 1-向后端系统转发

`jmock.tcpclient.ip`：当上面的参数为1时需要配置，后端系统的IP

`jmock.tcpclient.port`：当上面的参数为1时需要配置，后端系统的port


`jmock.packet.basePath`：模拟报文所在文件夹路径

`jmock.packet.response.name.prefix`：模拟报文文件名获取规则。本系统解析上送的报文，找到本参数中对应标签的字段的值，作为返回报文的名称。
eg：service.head.data[code].field 表示返回的报文名称为service标签下的head标签下的name=code的data标签下的field标签的值。
当需要通过多个字段组合时，用&拼接。eg：service.head.data[code].field&service.body.field


