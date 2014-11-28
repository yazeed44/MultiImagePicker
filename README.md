MultiImagePicker
================

A library to pick multi images

Sample App (On playstore) : Soon

This library is built-in gallery to pick multiple images or capture new photos , and retrieve the path in the code

<b>Getting started</b> <br />

It's easy

```java
private void pickImages(){
        final Intent pickIntent = new Intent(this, PickerActivity.class); 
        pickIntent.putExtra(PickerActivity.LIMIT_KEY, 6); // Set a limit

        startActivityForResult(pickIntent, PickerActivity.PICK_REQUEST); //Open gallery
    }
    ```
    
    Now you wait till user choose his wanted images , then retrieve it 
    
    ```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, requestCode, data);
        if (requestCode == PickerActivity.PICK_REQUEST && resultCode == RESULT_OK) {
            //No problemo

            final String[] paths = data.getStringArrayExtra(PickerActivity.PICKED_IMAGES_KEY);//Paths for chosen images (Organized)

            //Do what you want with paths
            
            ```

