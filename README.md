# PullToRefresh
Implement the effect of pull to refresh.
[![](https://jitpack.io/v/RainbleNi/PullToRefresh.svg)](https://jitpack.io/#RainbleNi/PullToRefresh)

#Usage
 Add it in your root build.gradle at the end of repositories:
```java
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```
Add the dependency
```java
dependencies {
	        compile 'com.github.RainbleNi:PullToRefresh:0.0.1'
	}
```

###Usage in xml
For example, add a grid view in layout
```xml
<com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout    
   android:id="@+id/ptf_layout"    
   android:layout_width="match_parent"
   android:layout_height="match_parent">    
       <GridView        
            android:id="@+id/gridview"        
            android:layout_width="match_parent"
            android:layout_height="match_parent"        
            android:numColumns="2"/>
</com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout>
```
in upper case, we only add the content view in the layout, so header view is in default style.

We can also define your own header layout.
```xml
<com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout    
   android:id="@+id/ptf_layout"    
   android:layout_width="match_parent"
   android:layout_height="match_parent">

       <TextView    
            android:id="@+id/header"    
            android:layout_width="match_parent"   
            android:layout_height="50dp" 
            android:text="loading"   
            android:gravity="center"/>
    
       <GridView        
            android:id="@+id/gridview"        
            android:layout_width="match_parent"
            android:layout_height="match_parent"        
            android:numColumns="2"/>
</com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout>
```
We will automatically identify the first child as header and second child as content view.

You can also define header or content in layout xml:
```xml
<com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout    
    xmlns:android="http://schemas.android.com/apk/res/android"    
    xmlns:ptf="http://schemas.android.com/apk/res-auto"    
    android:layout_width="match_parent"    
    android:layout_height="match_parent"    
    ptf:header_layout="@layout/header"    
    ptf:content_layout="@layout/content"/>
```

If you content include a scroll view (ListView, GridView, ScrollView)，but the scroll view is not the root of the content. In order to perform currently, you should assign the scroll view in xml:
```xml
ptf:scroll_id="@+id/listview"
```

###Usage in java

Register refresh callback
```java
ptfLayout = (PullToRefreshLayout) root.findViewById(R.id.ptf_layout);
ptfLayout.setRefreshCallback(new PullToRefreshLayout.RefreshCallback() {  
  
  @Override    
  public void onRefresh() {
        //do refresh
  }
});
```
After refresh completed, you should notify the ui:
```java
ptfLayout.refreshComplete();
```

If your header view is change along with the refreshing state, you should register the HeaderUICallback:
```java
ptfLayout.setHeaderUICallback(new PullToRefreshLayout.HeaderUICallback() {    
    @Override
    public void onStatePullToRefresh() {
        headView.setText("Pull to refresh");    
    }   
    @Override
    public void onStateReleaseToRefresh() {
        headView.setText("Release to refresh");
    }
    @Override
    public void onStateRefreshing() {
        headView.setText("In refreshing");
    }
    @Override
    public void onStateComplete() {
        headView.setText("Refresh completed");
    }
});
```

Some extend function:
 ```java
//start auto refresh without MotionEvent
ptflayout.autoRefresh()
//set Animation duration
 {@link #setScrollAnimationDuration(int)}
//set refresh critical position
 {@link #setRefreshingLine(float)}
//set the coefficient of friction of pull to refresh action
 {@link #setCoefficientOfFriction(float)}
```

#Advantage
1 easily to use

2 more effectively and smoothly

Gif

![ScreenRecord_2016-05-24-20-05-32.gif](http://upload-images.jianshu.io/upload_images/2067811-f98b79d0c34e91b6.gif?imageMogr2/auto-orient/strip)
