MultiImagePicker
================

A library to pick multi images

Sample App (On playstore) : https://play.google.com/store/apps/details?id=net.yazeed44.imagepicker.sample

This library is built-in gallery to pick multiple images or capture new photos , and retrieve the path in the code


This library is inspired by Telegram image picker


![Demo](screenshots/albums.png)  ![Demo](screenshots/photos.png)



:app  is the sample application 


:library  is the library source code

Gradle Dependency (jCenter)
==========================
Soooon



Adding to your project
======================
Add :library to your project as module Then compile it in build.gradle



Getting started
==========

It's easy

```java
private void pickImages(){
        final Intent pickIntent = new Intent(this, PickerActivity.class); 
        pickIntent.putExtra(PickerActivity.LIMIT_KEY, 6); // Set a limit , you can skip that if you want no limit

        startActivityForResult(pickIntent, PickerActivity.PICK_REQUEST); //Open gallery
    }
```

    
    
```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, requestCode, data);
        if (requestCode == PickerActivity.PICK_REQUEST && resultCode == RESULT_OK) {
            //No problemo

            final String[] paths = data.getStringArrayExtra(PickerActivity.PICKED_IMAGES_KEY);//Paths for chosen images (Organized)

            //Do what you want with paths
            
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

Join in the conversation, check out the [XDA Thread](http://forum.xda-developers.com/tools/programming/library-multi-image-picker-t2985724/post57775519#post57775519).




Change Log
==========

1.1:

-Better support for tablets

1.0:

-First release


TODO
====

-Better user expirenece

-easy integration

-Implement animations

-Add more translations

