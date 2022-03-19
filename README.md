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
implementation 'com.github.linxcool.sdkface:platform:1.0.1'
implementation 'com.github.linxcool.sdkplgs:googleplay:1.0.0'
implementation 'com.github.linxcool.sdkplgs:ironsource:1.0.0'
...
```

[![](https://jitpack.io/v/linxcool/sdkface.svg)](https://jitpack.io/#linxcool/sdkface)

