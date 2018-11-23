
This is simple application used to understand the custom progress indicator for download any items. Also you can use this to show the status in percentage.
If you don’t want to show progress indicator you can disable it and it will show percentage text only. Vice versa also possible in this component.

This is open source. If anyone wants to customise it. You’re welcome!!!!


#### maven metadata.xml
```
<metadata>
<groupId>com.github.praveen2gemini</groupId>
<artifactId>CloudProgressBar</artifactId>
<version>0.0.2-SNAPSHOT</version>
<versioning>
<snapshot>
<timestamp>20181123.184205</timestamp>
<buildNumber>1</buildNumber>
</snapshot>
<lastUpdated>20181123184205</lastUpdated>
</versioning>
</metadata>
```



#### Add following line to app/build.gradle
```
implementation 'com.github.praveen2gemini:CloudProgressBar:0.0.2-SNAPSHOT@aar'
```

```
 <com.dpdlad.customprogressbar.DownloadProgressBar
            android:id="@+id/animated_cloud_progressbar2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:layout_margin="@dimen/margin_xlarge"
            cloud_progressbar:download_icon="@drawable/ic_cloud_download_black_24dp"
            cloud_progressbar:max="100"
            cloud_progressbar:show_indicator="true"
            cloud_progressbar:show_percentage="true"
            cloud_progressbar:primary_progress_color="@color/green_dark"
            cloud_progressbar:progress_background_color="@color/colorPrimary"
            cloud_progressbar:progress_text_color="@android:color/white"
            cloud_progressbar:secondary_progress_color="@color/green_light" />
```

The following attributes are optional by deafult it is true. DO NOT set false untill it is required really!!!!
```
            cloud_progressbar:show_indicator="true"
           cloud_progressbar:show_percentage="true"
```


# Sample Demo:

![Overview](screenshots/Screenshot_1542994038.png) ![During Download](screenshots/Screenshot_1542994054.png)
![ListView](screenshots/Screenshot_1542994082.png) ![List Detail View](screenshots/Screenshot_1542994121.png)
![Different Size](screenshots/Different_Size_progressbar.png)
![Different Size](screenshots/Without_percentage_and_Without_Indicator_BOTH.png)

