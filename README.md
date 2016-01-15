#DraggableDot 
[![](https://jitpack.io/v/lsxiao/DraggableDot.svg)](https://jitpack.io/#lsxiao/DraggableDot)

A little dot that can be dragged with animation in full screen. 

**the DotView will be draw above the content view of your activity.**

------

## Demo

![](https://github.com/lsxiao/DraggableDot/blob/master/demo.gif?raw=true)



##Dependence 
###step-1

###gradle 
```groovy
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

###maven
```
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```


###step-2

###gradle 
```groovy
dependencies {
        compile 'com.github.lsxiao:DraggableDot:0.1.1'
}
```

###maven
```
<dependency>
    <groupId>com.github.lsxiao</groupId>
    <artifactId>DraggableDot</artifactId>
    <version>0.1.1</version>
</dependency>
```
##How to use it

###step-1
first you must attach draggableLayout to your activity.

```java
DraggableLayout.attachToActivity(yourActivity);
```

###step-2
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

###step-3
you can listen the state change by implement this listener.

```java
DotView dotView = (DotView) findViewById(R.id.dot);
dotView.setOnDotStateChangedListener(new DotView.onDotStateChangedListener() {
    @Override
    public void onStretch(DotView dotView) {
        Log.d("xls", "onStretch");
    }

    @Override
    public void onDrag(DotView dotView) {
        Log.d("xls", "onDrag");
    }

    @Override
    public void onDismissed(DotView dotView) {
        Log.d("xls", "onDismissed");
    }
});
```

## License

MIT