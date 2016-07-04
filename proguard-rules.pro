# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in e:\IDE\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#【优化处理，优化系数为5。不做优化-dontoptimize】
-optimizationpasses 5
#【优化方式】
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#【混淆时不产生混合的类名】
-dontusemixedcaseclassnames

#【不忽略非公共的库类】
-dontskipnonpubliclibraryclasses

#【不做预校验】
-dontpreverify

#【处理过程中显示更多运行信息】
-verbose

#【保护使用反射机制的类】
-keepattributes *Annotation*

#【保护使用泛型的类】
-keepattributes Signature

-keepattributes InnerClasses

-keepattributes InnerClasses,EnclosingMethod

#【保护使用本地方法的类，即使用NDK相关类】
-keepclasseswithmembernames class * {
    native <methods>;
}

#【保护枚举类】
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#【保护Parcelable类】
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

#【保护Serializable类】
-keepnames class * implements java.io.Serializable

# 保留Serializable 序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
   static final long serialVersionUID;
   private static final java.io.ObjectStreamField[] serialPersistentFields;
   !static !transient <fields>;
   private void writeObject(java.io.ObjectOutputStream);
   private void readObject(java.io.ObjectInputStream);
   java.lang.Object writeReplace();
   java.lang.Object readResolve();
}
# 对于带有回调函数onXXEvent的，不能混淆
-keepclassmembers class * {
    void *(**On*Event);
}

#=========================Android API相关====start==================================
#【保护Android相关组件类】
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.v8.**

# 保留Activity中的方法参数是view的方法，
# 从而我们在layout里面编写onClick就不会影响
-keepclassmembers class * extends android.app.Activity {
    public void * (android.view.View);
}
# 保留自定义控件(继承自View)不能被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get* ();
}
################common###############
-keep class com.itcalf.renhe.dto.** { *; } #实体类不参与混淆
-keep class com.itcalf.renhe.bean.** { *; } #实体类不参与混淆
-keep class cn.sharp.android.ncr.ocr.OCRItems #实体类不参与混淆
-keep class com.itcalf.renhe.widget.** { *; } #自定义控件不参与混淆
-keep class com.itcalf.renhe.view.** { *; } #自定义控件不参与混淆

################支付宝##################
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.android.phone.mrpc.** { *; }

################gson##################
-keep class com.google.gson.** {*;}
#-keep class com.google.**{*;}
-keep class com.google.gson.stream.** { *; }
-keep class com.google.** {
    <fields>;
    <methods>;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-dontwarn com.google.gson.**

################httpmime/httpcore##########
-keep class org.apache.http.** {*;}
-dontwarn org.apache.http.**

-keep class org.hamcrest.** {*;}
-dontwarn org.hamcrest.**

 #不混淆R类
-keep public class com.itcalf.renhe.R$*{
    public static final int *;
}
#【保护R资源索引类】
-keepclassmembers class **.R$* {
    public static <fields>;
}
#【忽略android.webkit.webview相关警告】
-dontwarn android.webkit.**

#如果项目中用到了WebView的复杂操作，请加入以下代码
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}
#对JavaScript的处理
-keepclassmembers class com.itcalf.renhe.view.WebViewForIndustryCircle$JSInterfacel {
    <methods>;
}
####################umeng##################
-keep class com.umeng.analytics.** {*;}
-dontwarn com.umeng.analytics.**

#-keep public class * extends com.umeng.**
#-keep public class * extends com.umeng.analytics.**
#-keep public class * extends com.umeng.common.**
#-keep public class * extends com.umeng.newxp.**
-dontwarn com.umeng.**
-keep class com.umeng.**
-keep class com.umeng.** { *; }
-keep public class * extends com.umeng.**
-keep public class com.umeng.fb.ui.ThreadView {
}

#【Sina】
-keep class com.sina.sso.** {*;}

#【Tencent】
-keep class com.tencent.** {*;}

#【WeiXin】
-keep class com.tencent.mm.sdk.modelbiz.**{*;}

#【grpc】
-dontwarn io.grpc.**
-keep class cn.renhe.heliao.idl.**{*;}
-keep class javax.annotation.**{*;}
-keep public class * extends cn.renhe.heliao.idl.base.BaseRequest
-keep public class * extends cn.renhe.heliao.idl.base.BaseResponse

#【eventbus】
-keepclassmembers class **{
    public void onEvent*(**);
}

#【okhttp】
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-keep class com.squareup.okhttp.** { *;}

#【multidex】
-keep class android.support.multidex.** { *;}

#【litepal】
-keep class org.litepal.** { *; }
-keep class * extends org.litepal.crud.DataSupport{*;}

#【okhttp】
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-keep class com.squareup.okhttp.** { *;}

#【阿里云】
-dontwarn anet.channel.**
-dontwarn com.spdu.**
-dontwarn com.taobao.**
-dontwarn org.android.**
-dontwarn com.alibaba.**
-keep class com.alibaba.** { *;}
-keep class anet.channel.** { *;}
-keep class com.spdu.** { *;}
-keep class com.taobao.** { *;}
-keep class org.android.** { *;}

####################zxing#####################
-keep class com.google.zxing.** {*;}
-dontwarn com.google.zxing.**

#【butterknife】
#-dontwarn butterknife.**
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}
-dontwarn butterknife.Views$InjectViewProcessor
-dontwarn com.gc.materialdesign.views.**
#【google】
-dontwarn com.google.common.**
-keep class com.google.common.**{*;}
-keep class com.google.protobuf.**{*;}
#【baidu】
-dontwarn com.baidu.**
-keep class com.baidu.**{*;}

#【imageselector】
-keep class com.itcalf.renhe.context.imageselector.**{*;}

###################other####################
-keep class * extends java.lang.annotation.Annotation { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class  com.itcalf.renhe.dto.** {
    <fields>;
    <methods>;
}

-dontwarn android.support.**
-keep class android.support.** { *; }

-dontwarn com.nhaarman.listviewanimations.appearance.StickyListHeadersAdapterDecorator
-keep class com.nhaarman.listviewanimations.appearance.StickyListHeadersAdapterDecorator

-dontwarn se.emilsjolander.stickylistheaders.StickyListHeadersListView
-keep class se.emilsjolander.stickylistheaders.StickyListHeadersListView

#【Pinyin4j】
-keep class com.hp.hpl.sparta.** { *; }
-keep class net.sourceforge.pinyin4j.format.** { *; }

-dontwarn demo.Pinyin4jAppletDemo
-keep class demo.Pinyin4jAppletDemo

-dontwarn demo.Pinyin4jAppletDemo.**
-keep class demo.Pinyin4jAppletDemo.**{*;}

-keep class demo.Pinyin4jAppletDemo.** {
    <fields>;
    <methods>;
}


-dontwarn javax.swing.**
-keep class javax.swing.**{*;}

-dontwarn java.awt.**
-keep class java.awt.**{*;}

-dontwarn com.chensl.mylib.**
-keep class com.chensl.mylib.**{*;}

-dontwarn com.viewpagerindicator.LinePageIndicator
-keep class com.viewpagerindicator.LinePageIndicator

-keep class com.xiaomi.**{*;}
-keep class com.huawei.**{*;}
-keep class org.apache.thrift.**{*;}

-dontwarn org.json.**
-keep class org.json.**{*;}

-dontwarn android.app.Notification
-keep class android.app.Notification

-dontwarn android.net.SSLCertificateSocketFactory
-keep class android.net.SSLCertificateSocketFactory

#【RotatePhotoView】
-keep class com.chensl.mylib.**{*;}
-keep class uk.co.senab.photoview.**{*;}

#【MaiKeXun】
-keep class cn.maketion.ctrl.appencrypt.**{*;}
-keep class cn.ocrsdk.**{*;}