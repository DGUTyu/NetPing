# NetPing
# 网络自检 1.0正式版
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
    implementation 'com.github.DGUTyu:NetPing:1.0'
}
```
## 3.Use Demo
```java
NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(LoginActivity.this);
NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(LoginActivity.this, "https://www.baidu.com/");
```
