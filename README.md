# DX-Message-parsing-app
鼎信消防部分报文解析系统和与9605模块通讯的app

主要功能是对9605模块收发报文。

1.接收报文，对报文进行解析，得出报文中包含的如“传感器感受到温度升高”，“传感器感受到烟雾”，“消防手动报警按钮被摁下”及时间设备等信息，并显示到app中。

2.发送报文，模拟感温感烟等事件，使硬件部分的报警器等运行。

是刘老师给的企业级应用的仿写，实现了它的功能，不过只能算是一个小demo吧。以前没接触过安卓，java也只是看过两眼语法，写的时候对socket tcpip 线程什么的都一无所知，随搜随写，写的非常丑。