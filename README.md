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
>`bot quiet <time>`禁言自己,time为时间,具体格式为<数值>[单位],单位可以是[dhm],分别代表天时分,不加为秒,后面时间格式类似(需要机器人拥有管理员权限)
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
>>发送之后机器人会提示再次使用该命令录入正文,此时T为正文内容,标题截断长度80字
>>
>>`bot msg help`显示机器人内部的帮助信息

>## 超级命令功能
>
>sc开头的超级命令
>
>仅管理员可用且需要机器人拥有管理员权限
>
>`sc bl <ID>`将某人列入黑名单,该操作会直接踢出改成员并记录QQ号,再次加入的时候秒踢,ID可以是直接@某人或某人的QQ号
>
>`sc bl ls`列出记录的黑名单列表
>
>`sc bl rm <ID>`将ID移除黑名单,ID可以是直接@某人或某人的QQ号
>
>`sc shutup <ID> <time>`禁言某人time时间,时间格式同`quiet`格式

>## 其他功能
>
>>### 刷屏禁言
>>一个人连续发同样的消息超过10次自动禁言30分钟
>
>>### STFW和你还有什么需要补充的吗自动回复
>>针对不会提问的憨批
>>
>>STFW是全匹配,补充的匹配规则是`.*有什么.?补充.*`
>
>>### 二维码拦截
>>发现图片带有二维码之后尝试撤回并禁言发送者,并后台通知管理员进行处理

## 使用方法
编写CQHTTP插件配置,放在酷Q目录下`app\io.github.richardchien.coolqhttpapi\config.ini`
```
[general]
host=0.0.0.0
port=5701
post_url=http://127.0.0.1:5702
```

编写机器人配置xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<KDRobotConfig>
    <Group ID="123456789">
        <!--可选选项,管理QQ号,需要和机器人有好友,会私聊发送一些通知,不填就不会发送-->
        <Admin ID="987654321"/>
        <!--必填选项,数据存储路径-->
        <WorkSpace PATH="D:\xxx\xxx"/>
    </Group>
    <!--可以有多个-->
    <Group ID="2222">
        <WorkSpace PATH="D:\xxx\xxx"/>
    </Group>
</KDRobotConfig>
```
然后启动机器人

```bash
java -jar <Config> [color]
```
Config为上述配置文件路径 color为颜色设置,直接输入color启动颜色,命令行参数功能目前尚不完善,不能调换顺序