# REST API Android demonstrator
This sample App for Android demonstrates the use of Arthika's REST API.
##### Table of Contents 

[Screenshots](#Screenshots)   
[Requirements](#Requirements)   
[Instructions](#Instructions)   

<a name="Screenshots"/>
### Screenshots:

Start:

![Sample App screenshot preview](Images/Android-Sample-App-0.png)

Getting prices:

![Sample App screenshot preview](Images/Android-Sample-App-1.png)

Watching prices:

![Sample App screenshot preview](Images/Android-Sample-App-2.png)
![Sample App screenshot preview](Images/Android-Sample-App-3.png)

Send order:

![Sample App screenshot preview](Images/Android-Sample-App-4.png)

Watching order:

![Sample App screenshot preview](Images/Android-Sample-App-5.png)

Cancel order:

![Sample App screenshot preview](Images/Android-Sample-App-6.png)

Modify order:

![Sample App screenshot preview](Images/Android-Sample-App-7.png)

Accounting:

![Sample App screenshot preview](Images/Android-Sample-App-8.png)

<a name="Requirements"/>
### Requirements:
You will need Android Studio at least 1.3.2 version, you can download these from
[Android Studio Download page](https://developer.android.com/sdk/index.html)

<a name="Instructions"/>
### Instructions:

##### 1) Pull repository contents.

##### 2) Open the project in Android Studio
You have two options:

2.a) Open the project into Android Studio (File -> Open -> Select ArthikaHFT folder)

2.b) Select "Open an existing Android Studio Project" in Android Studio launcher menu, then select ArthikaHFT folder.

#### 3) Run in Android Studio Emulator

Select Run -> Run 'app'.

If there is an emulator running you can choose one of them or launch new emulator selecting "Android virtual device".

### 4) Install in Android Device

For installing the app in your mobile device you must generate apk file.

In [Android Studio WebSite](https://developer.android.com/tools/publishing/app-signing.html) describes the steps.

Here's a summary:

Select Build -> Generate signed APK...

![Sample App screenshot preview](signstudio1.png)

You must choose a key store selecting "Choose existing..."

If you don't have a key store you must create a new one selecting "Create new..."

![Sample App screenshot preview](signstudio2.png)

Choose apk folder and "Build Type".

![Sample App screenshot preview](signstudio3.png)

Copy generated apk file to your mobile device, you can use a USB cable.

In your mobile device, execute this file for installing the app.



