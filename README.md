# 旅途AI - 后端服务

> AI驱动的个性化智能旅游规划小程序 — Spring Boot 3.x 后端

## 技术栈

| 组件 | 版本     | 说明 |
|------|--------|------|
| Java | 17      | LTS版本 |
| Spring Boot | 3.3.4  | 核心框架 |
| MyBatis-Plus | 3.5.7  | ORM框架 |
| Sa-Token | 1.38.0 | 鉴权框架 |
| Redis | 7.x    | 缓存/Token存储 |
| MySQL | 8.0    | 关系型数据库 |
| Hutool | 5.8.26 | 工具库 |
| Knife4j | 4.5.0  | API文档 |

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 修改配置文件

编辑 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lv_ai?...
    username: root
    password: 你的数据库密码

# 微信小程序配置
wechat:
  mini:
    app-id: 你的微信AppID
    app-secret: 你的微信AppSecret

# AI配置（选择一个提供商）
ai:
  provider: deepseek    # 可选: deepseek / qwen / openai
  deepseek:
    api-key: 你的DeepSeek API Key

# 腾讯地图（用于位置选择）
tencent:
  map:
    key: 你的腾讯地图Key
```

### 3. 启动服务

```bash
mvn spring-boot:run
```

或打包后运行：

```bash
mvn clean package -DskipTests
java -jar target/lv-ai-backend-1.0.0.jar
```

服务默认启动在 `http://localhost:8080`

### 4. 访问API文档

启动后访问：`http://localhost:8080/api/doc.html`

## 项目结构

```
src/main/java/com/lvai/
├── LvAiApplication.java         # 启动类
├── common/
│   ├── Result.java              # 统一响应结果
│   ├── ResultCode.java          # 响应码常量
│   ├── BusinessException.java   # 业务异常
│   └── GlobalExceptionHandler.java  # 全局异常处理
├── config/
│   ├── SaTokenConfig.java       # Sa-Token + CORS配置
│   ├── MybatisPlusConfig.java   # 分页插件配置
│   └── MetaObjectHandlerConfig.java # 自动填充create/update时间
├── entity/                      # 实体类
│   ├── User.java
│   ├── TravelPlan.java
│   ├── TravelDay.java
│   ├── TravelItem.java
│   ├── UserCollection.java
│   ├── AiGenerationLog.java
│   └── PlanExecutionRecord.java
├── mapper/                      # MyBatis-Plus Mapper接口
├── service/                     # Service接口
│   └── impl/                   # Service实现
│       ├── UserServiceImpl.java      # 微信登录逻辑
│       ├── TravelPlanServiceImpl.java # 行程管理
│       └── AiServiceImpl.java        # AI调用（DeepSeek/通义/OpenAI）
├── controller/                  # 控制器
│   ├── UserController.java      # 用户/登录
│   ├── TravelPlanController.java # 行程管理
│   ├── ExecutionController.java  # 打卡/记账
│   ├── DiscoverController.java   # 发现/攻略
│   └── ProfileController.java   # 个人中心
├── dto/                         # 请求DTO
└── vo/                          # 响应VO
```

## 主要接口

### 用户模块
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/login | 微信一键登录 |
| GET | /api/user/info | 获取用户信息 |
| PUT | /api/user/info | 更新用户信息 |
| POST | /api/user/logout | 退出登录 |
| GET | /api/user/invite-code | 获取邀请码 |
| POST | /api/user/bind-couple | 绑定情侣 |

### 行程管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/plan/create | **AI生成行程（核心）** |
| GET | /api/plan/{planId} | 获取行程详情 |
| GET | /api/plan/list | 我的行程列表 |
| PUT | /api/plan/{planId} | 更新行程 |
| DELETE | /api/plan/{planId} | 删除行程 |
| PUT | /api/plan/{planId}/status | 更新行程状态 |
| POST | /api/plan/{planId}/collect | 收藏/取消 |

### 执行记录
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/execution/check-in | 打卡/记账/日记 |
| GET | /api/execution/records/{planId} | 执行记录列表 |
| GET | /api/execution/stats/{planId} | 费用统计 |

### 发现/个人中心
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/discover/list | 公开行程列表 |
| GET | /api/discover/hot | 热门目的地 |
| GET | /api/profile/collections | 我的收藏 |
| GET | /api/profile/footprints | 我的足迹 |
| GET | /api/profile/stats | 旅行统计 |

## AI接入说明

系统Prompt位于 `src/main/resources/prompt/travel-system-prompt.txt`，可根据业务需要自定义修改。

**切换AI提供商**，修改 `application.yml`：
```yaml
ai:
  provider: deepseek   # 改为 qwen 或 openai
```

**AI输出格式**：AI必须以JSON格式返回，系统会自动解析并存储到数据库。

## 常见问题

**Q: 微信登录失败？**
A: 检查 `wechat.mini.app-id` 和 `wechat.mini.app-secret` 是否正确，确保在微信公众平台开通了相关权限。

**Q: AI生成超时？**
A: OkHttp的超时设置为120秒，AI接口首次响应较慢属正常。可适当增大 `readTimeout`。

**Q: Redis连接失败？**
A: 检查Redis服务是否启动，默认连接 `localhost:6379`，无密码。

## 部署说明

```bash
# 打包
mvn clean package -DskipTests

# 运行（指定配置文件）
java -jar target/lv-ai-backend-1.0.0.jar --spring.profiles.active=prod

# Docker运行（可选）
docker build -t lv-ai-backend .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod lv-ai-backend
```
