# NetPing
# 网络自检 2.1测试版（兼容support:appcompat-v7:27）
# 使用教程：
## 1.Add it in your root build.gradle at the end of repositories:
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
## 2.Add the dependency
```groovy
dependencies {
    implementation 'com.github.DGUTyu:NetPing:2.1'
}
```
## 3.Use Demo
```java
NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(LoginActivity.this);
NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(LoginActivity.this, "https://www.baidu.com/");
```
## 4.Custom NetPing default domain name (Optional)
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //自定义NetPing默认域名
        NetConfigUtils.init(new NetConfig() {
            @Override
            public String getDefaultPingUrl() {
                //return super.getDefaultPingUrl();
                //return "https://www.baidu.com";
                return BuildConfig.H5_BASE_URL;
            }
        });
    }
}
```