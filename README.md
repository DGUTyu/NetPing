# NetPing 网络自检
# Network self-check 2.2 (compatible with support:appcompat-v7:27)
# Tutorial for use
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
    implementation 'com.github.DGUTyu:NetPing:2.2'
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
        //Customize the default domain for NetPing.
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
If the application information is passed, it will be displayed on the Device.
```java
    NetConfigUtils.init(new NetConfig() {

        @Override
        public String getAppVersion() {
            return BuildConfig.VERSION_NAME;
        }

        @Override
        public String[] getAppTimeInfo() {
            //You can use custom methods or use the methods I provide, as follows.
            return CommonUtils.getAppTimeInfo(context);
        }

        @Override
        public String[] getAppDigest() {
            //You can use custom methods or use the methods I provide, as follows.
            return CommonUtils.getAppDigest(context);
        }

    });
```