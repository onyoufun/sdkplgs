# sdkplgs

* 该项目包含两部分，基础框架（sdkface）和功能插件（sdkplgs）。

# gradle

* 在项目根目录的gradle.properties文件中添加:
```
# 私有仓库的访问配置
username=linxcool
authToken=jp_tp0kvqep5bka5527hmflbe597h
# 部分仓库依赖androidx
android.useAndroidX=true
```

* gradle7以下版本，在项目根目录的build.gradle文件中添加:
```
allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
    // 如果引用了ironsource，则需要添加以下依赖库
    maven {
        url 'https://android-sdk.is.com/'
    }
 }
```

* gradle7以上版本，在项目根目录的gradle.setting文件中添加:
```
dependencyResolutionManagement {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            url "https://jitpack.io"
            credentials { username authToken }
        }
        // 如果引用了ironsource，则需要添加以下依赖库
        maven {
            url 'https://android-sdk.is.com/'
        }
    }
}
```

* 在app/build.gradle文件中添加依赖:
```    
implementation 'com.github.linxcool.sdkface:platform:1.0.5'
implementation 'com.github.linxcool.sdkplgs:googleplay:1.0.4'
implementation 'com.github.linxcool.sdkplgs:ironsource:1.0.4'
implementation 'com.github.linxcool.sdkplgs:meta:1.0.4'
implementation 'com.github.linxcool.sdkplgs:firebase:1.0.4'
```

[![](https://jitpack.io/v/linxcool/sdkface.svg)](https://jitpack.io/#linxcool/sdkface)

# 其他注意事项

* 【IronSource: Admob】从V17.0.0(适配器版本4.3.1)起，要求应用ID添加AndroidManifest作为标记
```
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXX"/>
```

* 【Meta: 登录】需要在res/string.xml中配置以下信息
```
<string name="facebook_app_id">270103748649637</string>
<string name="fb_login_protocol_scheme">fb270103748649637</string>
<string name="facebook_client_token">142504a4d4d413d0f26201e0e0cad018</string>
```

* 【Firebase】需下载google-services.json文件放到与build.gradle相同的目录下，且在build.gradle中配置以下信息：
```
plugins {
    id 'com.google.gms.google-services' version '4.3.10'
}
```
