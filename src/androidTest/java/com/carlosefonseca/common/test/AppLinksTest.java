package com.carlosefonseca.common.test;

import android.test.AndroidTestCase;
import com.carlosefonseca.common.utils.AppLinks;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;

import java.io.IOException;

public class AppLinksTest extends AndroidTestCase {

    private static final String TAG = CodeUtils.getTag(AppLinksTest.class);

    public void testDownload() throws IOException {
        Log.setConsoleLogging(true);
        AppLinks.download("https://www.youtube.com/user/Apple");
    }
}
