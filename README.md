# NetPing 网络自检
# Network self-check 2.4 (compatible with support:appcompat-v7:27)
## Development environment
### Using Gradle plugin version 3.5.0 and above.
```groovy
classpath 'com.android.tools.build:gradle:3.5.0'
```
```groovy
distributionUrl=https\://services.gradle.org/distributions/gradle-5.4.1-all.zip
```
### Some versions of the Gradle plugin may also require setting the NDK version.
### You can set it in the local.properties file, for example:
```groovy
ndk.dir=D\:\\SDK\\ndk\\22.1.7171670
```

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
    implementation 'com.github.DGUTyu:NetPing:2.4'
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
You can also customize the title bar.Clicking the title bar will exit the current page.
```java
    NetConfigUtils.init(new NetConfig() {

        @Override
        public int getTitleBarLayoutId() {
            //default TitleBarLayout
            //return super.getTitleBarLayoutId();
            //cancel TitleBarLayout
            //return NetConfig.NOT_LAYOUT_ID;
            //custom TitleBarLayout
            return R.layout.title_bar_layout;
        }
    });
```