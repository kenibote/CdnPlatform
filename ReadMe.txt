Creat in 2018-01-05
By KeniPotter

issue-1:
    0）基础配置信息：优先本地服务地址，（重定向由本地服务地址给出）
    1）user端可以接收monitor端的参数配置：请求速率，喜好程度分布，请求速率变化速度；
    1.1）user端可以根据monitor端的参数配置，按照指定时间轴和指定内容请求数据；
      --> 这里要优化一下信息的结构；JSON? 还是传输指定配置文件？
    2）user端可以接收monitor的控制指令，开始请求，暂停请求；
      --> 如果按照指定时间轴和指定内容请求数据，则应该不需要暂停操作；
    3）user端具备多线程请求数据功能；
    4）user端统计每个数据下载的时间；
    5）user端将统计数据回传给monitor；
      --> 这里也要优化一下信息的结构；JSON?
    6） 一些基础命令，清除所有缓存，设置等……

CODE信息：
ERROR_001 : KEY错误，鉴权错误
ERROR_002 : 未知任务类型


TASK信息：
TASK_001 : 通用hello包
TASK_002 : 设置ID，优先IP，等参数……包


monitor --> user的通信格式：
KEY:USER
TYPE:TASK_001
其余为可选内容

user --> monitor的通信格式：
CODE:TASK000/ERROR000
(STATE：SUCCESS/FAIL ) 如果code是error的话，state信息是没有的
DEVICE:USER
ID:NULL/1/2/...


issue-2:
    基础服务之文件下载功能：
    FileServerControl类负责子线程的创建，启动，关闭等服务；
      -->1）在使用该类的时候，需要先确定连续的端口号
      -->2）接着初始化连接池，之后启动服务
      -->3）关闭服务之后，如果想重新启动，则需要重复以上步骤
    FileServer为具体的子线程，负责文件的下载，具有异步关闭窗口的功能；
      -->在启动该线程的时候，需要传递进入port号
    FileServerInfo类为记录子线程的一些基础信息，其中一个重要的操作是记录该线程的可用性；
    FileServerStatus是一个枚举类
