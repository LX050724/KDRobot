# KDRobot

基于酷Q CQHTTP插件的群管理型QQ机器人

>## 基础功能
>
>基础功能使用bot开头标识机器人命令
>
>`bot about`显示机器人关于信息
>
>`bot t <msg>`和图灵机器人聊天,msg为内容
>
>`bot help`显示帮助信息
>
>`bot quiet <time>`禁言自己,time为时间,具体格式为<数值>[单位],单位可以是[dhm],分别代表天时分,不加为秒,后面时间格式类似(需要机器人拥有管理员权限)(已关闭)
>
>> ### Top
>>水群排行统计功能
>>
>>`bot top`列出机器人记录的水群总排行
>>
>>`bot top today`列出当天水群前10名
>>
>>`bot top check [ID]`查看某人的总水群排名,参数ID不加为查询自己,ID可以是直接@某人或某人的QQ号
>>
>>`bot top checktoday [ID]`查看某人的当天水群排名,参数ID不加为查询自己,ID可以是直接@某人或某人的QQ号
>>
>>`bot top report <ID>`投诉某人,ID可以是直接@某人或某人的QQ号,当累计的票数达到一定数量时就会给予惩罚并通过私聊通知管理员,
>>每人每天一次投票机会,每天0点刷新,每人被投票数量每天0点自动减三,不能投诉管理员
>>
>>`bot top help`显示机器人内部的帮助信息
>
>
>> ### msg
>> 留言板功能,每条留言保留三天
>>
>>`bot msg ls [Page]`列出帖子列表没有第三参数为查看留言总数和页数,加Page参数为列出列表那一页
>>
>>`bot msg look <ID>`查看留言
>>
>>`bot msg del <ID>`删除留言,管理员可以删除任何人的留言,非管理员只能删除自己的留言
>>
>>`bot msg push <T>`发标题为T的留言,标题截断长度20字,
>>发送之后机器人会提示再次使用该命令录入正文,此时T为正文内容,标题截断长度255字
>>
>>`bot msg help`显示机器人内部的帮助信息
>>
>>`bot verify <QQ>`身份验证,属于反机器人反广告功能,注意该功能实际位于超级命令模块中
>
>> ### baike
>> `bot baike <Keyword>` 查询Keyword的百度百科

>## 超级命令功能
>
>sc开头的超级命令
>
>仅管理员可用且需要机器人拥有管理员权限
>
>`sc bl <ID> [up]`将某人列入黑名单,该操作会直接踢出改成员并记录QQ号,再次加入的时候秒踢,ID可以是直接@某人或某人的QQ号,
>up选项是向上查找邀请者的参数,开启后如果是普通成员邀请进来的会向邀请者发出身份检查
>
>`sc bl ls`列出记录的黑名单列表
>
>`sc bl rm <ID>`将ID移除黑名单,ID可以是直接@某人或某人的QQ号
>
>`sc bl verify <ID>` 手动验证ID的身份。
>
>`sc bl celverify <ID>`取消ID的验证
>
>`sc shutup <ID> <time>`禁言某人time时间,时间格式同`quiet`格式
>
>`sc sql <direction> <cmd>`直接操作数据库，direction操作方向：'w'写数据库，'r'读数据库，cmd：MySQL命令。
>
>`sc cfg set <varibale> <value>`修改机器人的配置，具体可配置的属性查看可配置类中的属性列表
>
>`sc cfg read <varibale>`读取机器人配置  

>## 其他功能
>
>>### 刷屏禁言
>>一个人连续发同样的消息超过6次自动禁言15分钟
>
>>### STFW和你还有什么需要补充的吗自动回复
>>针对不会提问的憨批
>>
>>STFW是全匹配,补充的匹配规则是`.*有什么.?补充.*`
>
>>### 二维码拦截
>>发现图片带有二维码之后尝试撤回并禁言发送者,并后台通知管理员进行处理(已关闭)
>>### 群分享拦截
>>发现非管理员发送分享群（本的群的分享没关系）自动bl起飞

## 使用方法
编写CQHTTP插件配置,放在酷Q目录下`app\io.github.richardchien.coolqhttpapi\config.ini`端口和IP可以改,但是需要和机器人命令行参数的保持一致
```
[general]
host=0.0.0.0
port=5701
post_url=http://127.0.0.1:5702
```

编写机器人配置json
```json5
{
  "DataBase": {
    //数据库链接，用户名，密码
    "URL": "jdbc:mysql://xxx.xxx.xxx.xxx:xxxx?useSSL=false&serverTimezone=GMT%2B8",
    "NAME": "xxxx",
    "PASSWORD": "xxxx"
  },
  "Group": [
    {
      "ID": 123456,         //群号,必填项
      "Admin": 654321,      //机器人管理QQ号,选填,需要和机器人有好友,带有管理权限和操作数据库的权限,会私聊发送举报消息,不填就不会发送
      "TurlingKey": "xx..." //图灵机器人Key,选填,不填图灵机器人失效
    },
    {
      "ID": 789654,
      "Admin": 456987
    }
    // 可以有多组...
  ]
}
```
然后启动机器人(注意数据库要在根目录下,每个注册的群会各自新建数据库)
使用`java -Dfile.encoding=UTF-8 -jar CQRobot-release-Vx.x-jar-with-dependencies.jar -help`查看参数列表

```bash
java -Dfile.encoding=UTF-8 -jar CQRobot-release-Vx.x-jar-with-dependencies.jar -Config <Config> [-Color]
```
`-Dfile.encoding=UTF-8`必填,Config为上述配置文件路径 -Color为启用控制台颜色
