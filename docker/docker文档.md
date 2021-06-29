

# 第1章 docker简介

## 1.1 什么是docker

​	最初 dotCloud 公司创始人 Solomon Hykes 在法国发起的内部项目，dotCloud 公司云服务技术的革新，于 2013 年 3 月以 Apache 2.0 授权协议开源，主要项目代码在 GitHub 上进行维护。Docker 项目后来还加入了 Linux 基金会，并成立推动开放容器联盟（OCI）。

Go 语言开发，基于 Linux 内核的 cgroup，namespace，以及 OverlayFS 类的 Union FS 等技术，对进程进行封装隔离，属于操作系统层面的虚拟化技术。

由于隔离的进程独立于宿主和其它的隔离的进程，因此也称其为容器。

Docker 在容器的基础上，进行了进一步的封装，从文件系统、网络互联到进程隔离等等，极大的简化了容器的创建和维护。使得 Docker 技术比虚拟机技术更为轻便、快捷。

## 1.2为什么要用docker

### 1.2.1docker的优势

更高效的利用系统资源
由于容器不需要进行硬件虚拟以及运行完整操作系统等额外开销，Docker 对系统资源的利用率更高。

更快速的启动时间
虚拟机技术启动应用服务往往需要数分钟，而 Docker 容器应用，由于直接运行于宿主内核，无需启动完整的操作系统，因此可以做到秒级、甚至毫秒级的启动时间。

一致的运行环境
开发过程中一个常见的问题是环境一致性问题。由于开发环境、测试环境、生产环境不一致，导致有些 bug 并未在开发过程中被发现。而 Docker 的镜像提供了除内核外完整的运行时环境，确保了应用运行环境一致性

持续交付和部署
对开发和运维（DevOps）人员来说，最希望的就是一次创建或配置，可以在任意地方正常运行。

使用 Docker 可以通过定制应用镜像来实现持续集成、持续交付、部署。开发人员可以通过 Dockerfile 来进行镜像构建

而且使用 Dockerfile 使镜像构建透明化，不仅仅开发团队可以理解应用运行环境，也方便运维团队理解应用运行所需条件，帮助更好的生产环境中部署该镜像。

更轻松的迁移
由于 Docker 确保了执行环境的一致性，使得应用的迁移更加容易。Docker 可以在很多平台上运行，无论是物理机、虚拟机、公有云、私有云，甚至是笔记本，其运行结果是一致的。

更轻松的维护和扩展
Docker 使用的分层存储以及镜像的技术，使得应用重复部分的复用更为容易，也使得应用的维护更新更加简单，基于基础镜像进一步扩展镜像也变得非常简单。此外，Docker 团队同各个开源项目团队一起维护了一大批高质量的 官方镜像，既可以直接在生产环境使用，又可以作为基础进一步定制，大大的降低了应用服务的镜像制作成本。

### 1.2.2docker与传统虚拟机的比较

| **特性**   | **docker**         | **虚拟机** |
| ---------- | ------------------ | ---------- |
| 启动       | 秒级               | 分钟级     |
| 硬盘使用   | 一般为 MB          | 一般为 GB  |
| 性能       | 接近原生           | 弱于       |
| 系统支持量 | 单机支持上千个容器 | 一般几十个 |
| 隔离性     | 安全隔离           | 完全隔离   |

# 第2章 docker的核心概念

Docker 包括三个基本概念
镜像（Image）
容器（Container）
仓库（Repository）



## 2.1镜像（Image）

Docker镜像类似(xxx.iso), 一个只读模板!
	

例如一个镜像可以包含一个基本的操作系统环境, 里面仅安装了 Apache应用程序，可以把它称为Apache镜像.

镜像是创建Docker容器的基础.



## 2.2容器（Container）

Docker 容器类似于一个轻量级的沙箱， Docker 利用容器来运行和隔离应用。 
	

容器是从镜像创建的应用运行实例。它可以启动、开始、停止、删除，而这些容器都是彼此相互隔离、互不可见的。 

可以把容器看作一个简易版的 Linux 系统环境（包括 root 用户权限、进程空间、用户空间和网络空间等）以及运行在其中的应用程序打包而成的盒子。

注意: 	
镜像自身是只读的。 
容器从镜像启动的时候，会在镜像的最上层创建一个可写层。

## 2.3仓库（Repository）

Docker 仓库类似于代码仓库，是 Docker 集中存放镜像文件的场所。
	

根据所存储的镜像公开分享与否， Docker 仓库可以分为公开仓库（Public)和私有仓库(Private)两种形式。

目前，最大的公开仓库是官方提供的 Docker Hub ，其中存放着数量庞大的镜像供用户下载。国内不少云服务提供商（如腾讯云 、 阿里云等）也提供了仓库的本地源，可以提供稳定的国内访问 。

当然，用户如果不希望公开分享自己的镜像文件， Docker 也支持用户在本地网络内创建一个只能自己访问的私有仓库。

当用户创建了自己的镜像之后就可以使用 push 命令将它上传到指定的公有或者私有仓库。 这样用户下次在另外一台机器上使用该镜像时，只需要将其从仓库上 pull 下来就可以了。

第3章 Centos安装Docker引擎
	Docker 目前支持 CentOS 7 及以后的版本, 内核版本必须是3.10.
查看操作系统版本: cat /etc/redhat-release

查看内核版本: uname -r


3.1卸载旧版本
	旧版本的 Docker 被叫做 docker 或 docker-engine，如果安装了旧版本的 Docker ，需要卸载掉它。
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

3.2安装Docker
3.2.1安装方法1: 手动安装
1.安装依赖的软件包
sudo yum install -y yum-utils device-mapper-persistent-data lvm2

2.添加Docker文档版本的yum源
官方的yum源安装docker比较慢, 我们配置国内比较快的yum源(阿里云)
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

3.安装最新版本的docker引擎(社区版) 
sudo yum -y install docker-ce docker-ce-cli containerd.io
3.2.2安装方法2: 使用脚本自动安装
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun

注意: 
1.自动安装脚本会自动检测系统信息并进行相应配置
2.安装方法1和安装方法2二者选其一即可

3.2.3启动Docker服务
sudo systemctl start docker

3.2.4验证Docker是否可用
sudo docker run hello-world


3.2.5把普通用户添加到docker 组
	每次使用docker的时候都需要使用root用户, 比较麻烦.   可用把普通用户添加到docker组, 避免每次都添加sudo.
 sudo usermod -aG docker atguigu

	退出当前shell, 重新进入shell, 使刚才的配置生效
第4章镜像基本操作
	镜像是 Docker 三大核心概念中最重要的，自 Docker 诞生之日起镜像就是相关社区最为热门的关键词。 Docker 运行容器前需要本地存在对应的镜像，如果镜像不存在Docker 会尝试先从默认镜像仓库下载，用户也可以通过配置， 使用自定义的镜像仓库。
4.1列出本机镜像
docker images

说明:
REPOSITORY: 来源仓库
TAG: 镜像的标签信息, 表示镜像的版本. 只是标记, 并不能表示镜像内容
IMAGE ID: 镜像id, 唯一表示一个镜像. 如果两个镜像的 ID 相同， 说明它们实际上指向了同一 个镜像， 只是具有不同标签名称而已；
CREATED: 镜像的最后更新时间.  
SIZE:镜像大小
4.2获取一个新镜像
	当我们在本地主机上使用一个不存在的镜像时 Docker 就会自动下载这个镜像。如果我们想预先下载这个镜像，我们可以使用 docker pull 命令来下载它。 
	
docker pull hello-world



下载完成后, 可以使用这个镜像来运行容器
4.3配置国内镜像源地址
	下载镜像的时候, 默认是从官方地址下载, 服务器在国外, 速度比较慢, 可以换成国内镜像.

国内常用加速地址:
网易
		http://hub-mirror.c.163.com

中国科技大学
		https://docker.mirrors.ustc.edu.cn

阿里云容器服务
		https://cr.console.aliyun.com/
		首页点击“创建我的容器镜像”  得到一个专属的镜像加速地址，类似于
		https://abcdef.mirror.aliyuncs.com

配置方法:
sudo vim /etc/docker/daemon.json

{
    "registry-mirrors": [
                                "http://hub-mirror.c.163.com",
                                "https://docker.mirrors.ustc.edu.cn",
                                "https://813o9nus.mirror.aliyuncs.com"
                         ]
}
	
说明: 
1.关于阿里云地址, 参考: 
https://help.aliyun.com/document_detail/60750.html?spm=a2c4g.11186623.6.550.469742c75wmmC8
2.重启docker: sudo systemctl restart docker
3.查看是否配置成功: docker info

4.4搜索镜像
docker search hello-world

4.5删除镜像
使用tag删除镜像
docker rmi hello-world:latest


注意: 
1.如果删除的时候报错: 有容器使用了该镜像, 则需要先删除使用过该镜像的容器, 才能删除该镜像.

2.删除容器, 再删镜像


使用id删除镜像
docker rmi bf756fb1ae65


清理镜像
使用 Docker 一段时间后，系统中可能会遗留一些临时的镜像文件，以及一些没有被使用的镜像，可以通过 docker image prune -f 命令来进行清理。

第5章容器基本操作
	容器是 Docker 的另一个核心概念。 简单来说，容器是镜像的一个运行实例。所不同的是，镜像是静态的只读文件，而容器带有运行时需要的可写文件层，同时，容器中的应用进程处于运行状态。

	如果认为虚拟机是模拟运行的一整套操作系统（包括内核、 应用运行态环境和其他系统环境）和跑在上面的应用。 那么 Docker 容器就是独立运行的一个（或一组）应用，以及它们必需的运行环境。
5.1创建容器
1.获取centos:7.5.1804的镜像
docker pull centos:7.5.1804                                                                                          


2.创建容器 
docker create -i -t centos:7.5.1804  /bin/bash


说明:
I.创建一个交互式的容器
II.-i: 允许你对容器内的标准输入 (STDIN) 进行交互
III.-t: 在新容器内指定一个伪终端或终端。

3.启动容器 
docker start bcc


4.新建并启动容器
前面的操作是先创建容器, 然后再启动容器. 也可以使用run来直接新建并启动容器

启动一个交互式的centos容器
docker run -it centos:7.5.1804 /bin/bash
	说明:
1.检查本地是否存在指定的镜像，不存在就从公有仓库下载；
2.利用镜像创建一个容器，并启动该容器；
3.分配一个文件系统给容器，并在只读的镜像层外面挂载一层可读写层 ；

5.查看有哪些容器
1.查看启动的容器
docker ps

2.查看所有容器
docker ps -a



6.启动后台进程
docker run -itd centos:7.5.1804 /bin/bash
5.2停止容器
docker stop bcc

5.3进入容器
	在使用 -d 参数时，容器启动后会进入后台(有些容器默认就是后台, 比如centos容器)。此时想要进入容器，可以通过以下指令进入：
docker exec -it bcc /bin/bash

	通过指定 -it参数来保持标准输入打开， 并且分配一个伪终端。可以看到会打开一个新的 bash 终端，在不影响容器内其他应用的前提下，用户可以与容器进行交五。
5.4删除容器
1.删除已经停止的容器
docker rm ea5c


2.删除正在运行的容器
先停止, 再删除
docker rm -f bcc
5.5导入和导出容器
	某些时候，需要将容器从一个系统迁移到另外一个系统，此时可以使用 Docker 的导人 和导出功能，这也是 Docker 自身提供的一个重要特性。

	为了测试容器是否导出和导入成功, 我们在centos容器中创建一个新的文件

5.5.1导出容器
	导出容器是指，导出一个已经创建的容器到一个文件，不管此时这个容器是否处于运行状态.

docker export -o '/home/atguigu/test_for_centos.tar' 9fa

可以把导出的tar文件, 传输到其他设备, 再通过导入命令导入, 实现容器的迁移.
5.5.2导入容器
将上节导出的容器导入之后会成为镜像.
docker import test_for_centos.tar -- test/mycentos:1.0



使用新的镜像启动容器:

刚才创建的文件还在
5.6查看容器
5.6.1查看容器详情
docker container inspect 9fa

会以 json 格式返回包括容器 Id、创建时间、路径、状态、镜像、配置等在内的各项信息
5.6.2查看容器内进程
docker top 9fa
这个子命令类似于 Linux 系统中的 top 命令， 会打印出容器内的进程信息， 包括 PID 、 用户、时间、命令等

5.6.3查看统计信息
docker stats --no-stream 9fa
会显示 CPU 、内存、存储、网络等使用情况的统计信息

5.7容器和主机之间复制文件
容器和主机之间进行文件复制的时候, 要保证容器已经启动.
5.7.1从主机复制到容器
docker cp test.txt 9fa:/

5.7.2从容器复制到主机
docker cp 9fa:/a.txt ./

第6章镜像高级操作
本章在第4章的基础上, 介绍镜像高级操作
6.1创建镜像
创建镜像的方法主要有2种：
基于已有容器创建。
基于Dockerfile 创建

6.1.1.基于已有容器创建
docker commit -m 'add new file : a.txt' -a 'atguigu'  9fa  new_centos:1.0
说明:
1.-m 提交信息
2.-a  作者
3.9fa  旧有的容器
4.new_centos:1.0 新的镜像





6.1.2.基于Dockerfile 创建
	基于 Dockerfile 创建是最常见的方式。 Dockerfile 是一个文本文件， 利用给定的指令描述基于某个父镜像创建新镜像的过程。

	下面使用Dockerfile创建一个基于centos的java开发环境:

1.把jdk安装转包copy到 /home/atguigu 目录下
cd ~
cp jdk-8u212-linux-x64.tar.gz ./
2.在/home/atguigu上创建Dockerfile文件
vim Dockerfile

FROM centos:7.5.1804
RUN mkdir -p /opt/software
RUN mkdir -p /opt/module
COPY jdk-8u212-linux-x64.tar.gz /opt/software/
RUN tar -zxvf /opt/software/jdk-8u212-linux-x64.tar.gz -C /opt/module
RUN rm -rf /opt/software/jdk-8u212-linux-x64.tar.gz
ENV JAVA_HOME=/opt/module/jdk1.8.0_212
ENV PATH=$JAVA_HOME/bin:$PATH
说明:
每一个指令都会在镜像上创建一个新的层，每一个指令的前缀都必须是大写的。
第一条FROM，指定使用哪个镜像源
RUN 指令告诉docker 在镜像内执行命令，安装了什么。。。
COPY 是把文件copy到镜像中.  源文件必须是相对路径不能是绝对路径
ENV 在镜像中设置环境变量

3.创建镜像, 并等待创建成功
docker build -t centos_java8:1.0 .

说明:
1.-t 指明镜像名字和标签
2.. 表示Dockfile所在目录

4.测试镜像是否可以正常工作
docker run centos_java8:1.0 java -version
	
	
6.2保存和加载镜像
使用保存和加载功能可以把本机的镜像发给其他人使用
6.2.1.保存镜像
docker save -o atguigu_centos_java8.tar centos_java8:1.0
6.2.2.加载镜像
把刚刚保存的镜像发给别人(hadoop103), 然后加载导入.
sudo docker load -i atguigu_centos_java8.tar


第7章为镜像添加ssh服务
很多镜像是不带ssh服务的, 管理镜像非常的不方便, 本章介绍如何给镜像添加ssh服务
7.1创建镜像
Dockerfile:

# 设置继承镜像
FROM centos_java8:1.0
# 提供作者信息
MAINTAINER atguigu_lzc (lizhenchao@atguigu.com)

# 更换国内阿里云yum源
RUN curl -o /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo
RUN sed -i -e '/mirrors.cloud.aliyuncs.com/d' -e '/mirrors.aliyuncs.com/d' /etc/yum.repos.d/CentOS-Base.repo
RUN yum makecache

# 安装sshd
RUN yum install -y openssh-server openssh-clients
RUN sed -i '/^HostKey/'d /etc/ssh/sshd_config
RUN echo 'HostKey /etc/ssh/ssh_host_rsa_key'>>/etc/ssh/sshd_config

# 生成 ssh-key
RUN ssh-keygen -t rsa -b 2048 -f /etc/ssh/ssh_host_rsa_key

# 更改 root 用户登录密码为
RUN echo 'root:aaaaaa' | chpasswd

# 开发 22 端口
EXPOSE 22

# 镜像运行时启动sshd
RUN mkdir -p /opt
RUN echo '#!/bin/bash' >> /opt/run.sh
RUN echo '/usr/sbin/sshd -D' >> /opt/run.sh
RUN chmod +x /opt/run.sh
CMD ["/opt/run.sh"]
构建
docker build -t centos_java8_sshd:1.0 ./
7.2运行容器, 测试镜像
docker run -d -p 2222:22 centos_java8_sshd:1.0

说明: 把容器的22端口映射到宿主机器的2222端口, 这样通过ssh连接宿主机器的2222端口就可以连接到容器了.
ssh root@192.168.14.112 -p 2222


第8章端口映射与容器互联
8.1端口映射
	在启动容器的时候，如果不指定对应参数，在容器外部是无法通过网络来访问容器内的 网络应用和服务的。

	使用 -p 来指定映射规则:  3000:22  表示宿主的3000映射到容器的22端口, 使用多次 -p 可以映射多个端口.
docker run -d -p 2222:22 -p 8888:80 centos_java8_sshd:1.0

查看容器端口绑定情况:
docker port 9b2e0f3478ff
8.2容器互相访问
端口映射并不是唯一把 docker 连接到另一个容器的方法。

docker 有一个连接系统允许将多个容器连接在一起，共享连接信息。

docker 连接会创建一个父子关系，其中父容器可以看到子容器的信息。

8.2.1容器命名
	创建容器的时候, Docker会自动为容器分配一个随机的名字.我们也可以指定一个好记的名字, 方便容器间的互联.

docker run -d --name hadoop102 centos_java8_sshd:1.0
说明:
--name 参数给容器起一个名字



8.2.2docker的网络
	docker还会给我们创建三个网络：bridge/host/none。我们可以通过network ls命令查看当前宿主机中所有的docker网络。

	其中，网桥bridge模式是在实际项目中常用的。接下来，以交互模式启动两个centos_java8_sshd 容器。在没有指定相关网络的情况下，容器都会连接到默认的bridge网络.
	
	创建两个容器: hadoo102和hadoop103, 检查bridge网络情况. 可以看到他们的ip地址.
docker run -d --name hadoop102 centos_java8_sshd:1.0
docker run -d --name hadoop103 centos_java8_sshd:1.0

docker network inspect bridge


注意:
1.通过ip地址我们在宿主机或者容器之间可以访问到对方

2.当是172.17.0.2 等这些ip地址不能固定, 每次启动容器的时候有可能会变化, 如果组建集群的话很不方便.
8.2.3自定义bridge网络, 实现容器间通讯
	docker daemon 实现了一个内嵌的 DNS server，使容器可以直接通过“容器名”通信。使用默认的bridge网络，不能通过DNS server实现通过容器名通信，但是使用自定义bridge网络可以做到通过容器名互相通信。
1.创建自定义bridge网络, 网络名atguigu
docker network create --driver bridge atguigu

2.删除hadoop102和hadoop103容器
docker rm -f hadoop102
docker rm -f hadoop103

3.新建并启动hadoop102和hadoop103容器, 并加入到atguigu网络
docker run -d --name atguigu102 --network atguigu centos_java8_sshd:1.0
docker run -d --name atguigu103 --network atguigu centos_java8_sshd:1.0



4.进入atguigu102, 测试是否可以ping通atguigu103


第9章教学模式下网络搭建
	为了节省资源, 假设我们所有容器都在hadoop102主机创建和启动, 则我们必须满足下面的网络互通情况:

目前情况: 
1.windows系统和hadoop102互通已经完成
2.容器和hadoop102也可以互相访问 
3.容器间也可以自由访问
4.容器访问windows访问不到!!!
5.windows访问容器也访问不到, 但是可以通过端口映射解决.(如果端口过多, 映射不方便)
6.容器ip地址最好还是固定的

如何解决前面网络不通的情况?

让容器使用桥接模式(vmware中的桥接), 与hadoop102处于同一个网段就可以解决了. 使用pipework工具可以满足我们的需求!

9.1安装pipwork
1.在宿主(hadoop102)上安装git(如果已经安装, 跳过该步骤)
sudo yum install -y git

2.下载pipework
git clone https://github.com/jpetazzo/pipework.git

3.把pipework脚本添加到path中
sudo cp pipework/pipework /usr/bin/
9.2配置网络
删除原来的容器: atguigu102和atguigu103
docker rm -f $(docker ps -aq)   # 删除所有容器, 慎用!!!

1.在宿主机上实现桥接网络
我的宿主机信息:
eth0: 192.168.14.112
网关: 192.168.14.1
DNS: 114.114.114.114
在宿主机上(hadoop102)执行如下命令:
sudo brctl addbr br0; \
sudo ip link set dev br0 up; \
sudo ip addr del 192.168.14.112/24 dev eth0 ; \
sudo ip addr add 192.168.14.112/24 dev br0 ; \
sudo brctl addif br0 eth0 ; \
sudo ip route add default via 192.168.14.1 dev br0
说明: 
a.sudo brctl addbr br0;  添加网桥 br0
b.sudo ip link set dev br0 up 启动网桥br0
c.sudo ip addr del 192.168.14.112/24 dev eth0 ; 给eth0去掉ip, 如果是eth33需要换成eth33
d.sudo ip addr add 192.168.14.112/24 dev br0 ; 给网桥分配ip(就使用刚才eth0去掉的ip)
e.sudo brctl addif br0 eth0 ; 把eth0 搭在br0上
f.sudo ip route add default via 192.168.14.1 dev br0 给br0添加新的路由(根据虚拟机网关自己指定)
g.需要注意中间会断网, 所以需要放置在一条语句执行
	

2.创建两个容器
docker run -d --name atguigu202 centos_java8_sshd:1.0
docker run -d --name atguigu203 centos_java8_sshd:1.0

3.给两个容器添加ip,并搭在br0上
sudo pipework  br0 atguigu202 192.168.14.202/24@192.168.14.1
sudo pipework  br0 atguigu203 192.168.14.203/24@192.168.14.1
	
	说明:
a.br0网桥名
b.atguigu202 容器名
c.192.168.14.202/24 容器ip  24是指的掩码
d.192.168.14.1 网关地址(根据自己的虚拟机来实际指定)
9.3测试网络是否OK
进入 atguigu202
ssh root@192.168.14.202



9.4最后说明
	我们前面的网桥搭建方案和容器ip分配方案都是临时临时生效, 当虚拟机重启或者容器重启之后会失效, 可以放入脚本中, 统一执行.

vim /home/atguigu/bin/docker.sh

# 启动容器
docker start atguigu202
docker start atguigu203

# 搭建网桥
sudo brctl addbr br0; \
sudo ip link set dev br0 up; \
sudo ip addr del 192.168.14.112/24 dev eth0 ; \
sudo ip addr add 192.168.14.112/24 dev br0 ; \
sudo brctl addif br0 eth0 ; \
sudo ip route add default via 192.168.14.1 dev br0

sleep 5

# 给容器配置ip和网关
sudo pipework  br0 atguigu202 192.168.14.202/24@192.168.14.1
sudo pipework  br0 atguigu203 192.168.14.203/24@192.168.14.1
第10章使用docker搭建集群
	理论上来说, 每个容器应该只运行一个应用, 但是考虑到实际情况, 我们仍然采用前面学习的虚拟机中集权搭建方案, 用一个容器替换原来的一个虚拟机.
	在3个容器中运行原来所有的集群分布式方案.
10.1集群规划
	我们集群需要3台虚拟机, 每台虚拟机配置略有不同, 我们只做3个镜像, 每个镜像对应集群中一台虚拟机
	hadoop162	hadoop163	hadoop164
NameNode	√		
SecondaryNameNode			√
DataNode	√	√	√
ResourceManager		√	
NodeManager	√	√	√
HMaster	√		
HRegionServer	√	√	√
Phoenix	√		
Redis(非集群)	√	√	√
mysql	√	√	√

10.2准备工作
把所有需要用的文件提前准备好.



10.3制作镜像和创建容器
各个Dockerfile文件和脚本具体内容见清单

执行脚本:build_all.sh 根据提示构建需要的镜像和容器
执行脚本: contains.sh start启动容器

详细说明参考: Readme.md
10.4测试镜像
容器集群启动成功之后, 使用ssh登录.
像在虚拟机中操作一样进行操作就可以了

但是需要注意的是, 很工具没有了, 如果使用, 可以在制作镜像的时候安装好