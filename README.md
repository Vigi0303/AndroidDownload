AndroidDownload
=================

### 简介

关于下载库我有一些特殊的点子，加上这些能让下载变得更灵活，更方便。

github上现在有一些下载的库，但是都没有完全符合这些特点的，甚至有一些已经不再维护还在用过时的HttpClient。

所以想从零写了这个repo，初步想实现几个特点：

### 特点

* 下载任务描述或进度的东西需要持久化，把这部分的逻辑抽离出来，由开发者来决定是用数据库存储还是文件存储。
  或者提供两种具体实现，默认使用数据库，开发者可以更改。【demo中已经实现文件存储但是结构可能还需要调整】
* 提供和HttpUrlConnection不同的setTimeOut方式，实现不管处在任何网络环境下都会等待并重试直到time out。
  这个想法源于有时候用户的网络环境可能会是各种各样的情况。比如路由器不稳定，wifi会出现突然断开过两秒又重新连接的情况；
  比如用户想从昂贵的3G网切换到wifi下载时，过渡时间内网络不可用的情况。
  然而http连接会在当前明显无法连接网络的时候立刻返回错误，这会导致任务的下载失败，然后需要用户手动操作开始。
  这是不合理的，我们应该帮助用户处理这些偶发的情况，让用户安心干别的事情。（没错以上这些我都碰到过）
  【这部分已经完成】
* 关于检测用户手机的存储空间。当前的想法是交给开发者来处理，这样可以实现更灵活的业务逻辑。但是库也会在开始
  下载时（获得Content-Length时）创建一个空文件预留出精确的存储空间，避免下载过程中的存储空间变化导致开发者
  开始的预判检测失效。【这部分基本完成】
* 一些基本的下载库应该有的特点（callback，断点续传等等）。

### 一些还没有实现的功能点

有些其他的下载库有的功能，但我觉得不是特别重要的，暂时还没有开发：

* 单个文件的多线程同时下载。
* 多任务同时下载。任务管理方面写得比较灵活，原则上改个参数就可实现。

### 还没解决的问题

这个repo暂停了，是因为碰到个问题暂时还没解开。
在小米4手机6.0系统上开始下载，按home键切出，过几秒会下载失败，日志显示在写文件上报异常，但是错误内容太简单，无法定位具体原因。在考虑要不要换nio+更多的io线程处理试一试。
