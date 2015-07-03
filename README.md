MultiImagePicker
================

A library to pick multi images

Sample App (On playstore) : https://play.google.com/store/apps/details?id=net.yazeed44.imagepicker.sample

This library is built-in gallery to pick multiple images or capture new photos , and retrieve the path in the code


This library is inspired by Telegram image picker


![Demo](screenshots/albums.png)  ![Demo](screenshots/photos.png)



:app  is the sample application 


:imagepicker  is the library source code

Gradle Dependency (jCenter)
==========================
Just add the dependency to your build.gradle file
```gradle 
compile 'net.yazeed44.imagepicker:imagepicker:1.0.0' 
```

[ ![Download](https://api.bintray.com/packages/yazeed44/maven/multi-image-picker/images/download.svg) ](https://bintray.com/yazeed44/maven/multi-image-picker/_latestVersion)

### If jCenter is Having Issues (the library can't be resolved)

Add this to your app's build.gradle file:

```Gradle
repositories {
    maven { url 'https://dl.bintray.com/yazeed44/maven' }
}
```


Getting started
==========

It's easy

```java
private void pickImages(){
       
       //You can change many settings in builder like limit , Pick mode and colors
        new Picker.Builder(this,new MyPickListener())
        .build()
        .startActivity();
        
    }
```

    
    
```java


private class MyPickListener implements PickListener
{
@Override
public void onPickedSuccessfully(final String[] paths)
{

doSomethingWithImages(images);
}

@Override
public void onCancel(){
//User cancled the pick activity
}
}
            
```

##Contribution

### Questions

If you have any questions regarding MultiImagePicker,create an Issue

### Feature request

To create a new Feature request, open an issue 

I'll try to answer as soon as I find the time.

### Pull requests welcome

Feel free to contribute to MultiImagePicker.

Either you found a bug or have created a new and awesome feature, just create a pull request.


### Discuss
Join in the conversation , [Join us in telegram](https://telegram.me/joinchat/013dbd2b01ae8b2dcdb1147ac067c9ae).


