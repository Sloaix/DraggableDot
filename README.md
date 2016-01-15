#DraggableDot

A little dot that can be dragged with animation in full screen. 

------

## Demo

![](https://github.com/lsxiao/DraggableDot/blob/master/demo.gif?raw=true)



##Start 
###gradle
####step-1

```groovy
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```
####step-2

```groovy
dependencies {
        compile 'com.github.lsxiao:DraggableDot:-SNAPSHOT'
}
```


##How to use

####step-1
first you must attach draggableLayout to your activity.

```java
DraggableLayout.attachToActivity(yourActivity);
```

####step-2
then layout in xml.

```xml
<com.lsxiao.library.DotView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:xls_circle_color="@color/colorAccent"
    app:xls_content_color="@android:color/white"
    app:xls_radius="8dp"
    app:xls_content="20"
    app:xls_text_size="10sp"/>
```

####step-3
you can listen the state change by implement this listener.

```java
/**
 * the callback interface.
 */
public interface onDotStateChangedListener {
    void onStretch(DotView dotView);

    void onDrag(DotView dotView);

    //the dotView will be invisible.
    void onDismissed(DotView dotView);
}

```

## License

MIT