package com.carlosefonseca.common.test;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.FileDownloader;
import com.carlosefonseca.common.utils.Log;
import junit.framework.Assert;

import java.io.File;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.carlosefonseca.common.utils.ListUtils.list;

public class FileDownloaderTest extends AndroidTestCase {

    private static final java.lang.String TAG = CodeUtils.getTag(FileDownloaderTest.class);

    @LargeTest
    public void testDownload() {
        Log.setConsoleLogging(true);
        final ReentrantLock lock = new ReentrantLock();
        final Condition notDone = lock.newCondition();
        FileDownloader.setNotifier(new FileDownloader.Notification() {
            @Override
            public void start(int downloads) {

            }

            @Override
            public void finished() {
                super.finished();
                lock.lock();
                try {
                    Log.i(TAG, "SIGNALING!");
                    notDone.signal();
                } finally {
                    lock.unlock();
                }
            }

            @Override
            public void queueUpdate(int i) {

            }

            @Override
            public void fileFailed(String failedFile) {

            }
        });
        final String url = "http://search.maven.org/remotecontent?filepath=com/cathive/fonts/fonts-roboto/2012023.1/fonts-roboto-2012023.1-sources.jar";
        final FileDownloader.Download download = new FileDownloader.Download(url,
                                                                             new File(getContext().getExternalCacheDir(),
                                                                                      "test_file")
        );
        Assert.assertFalse(download.file.exists());
        FileDownloader.downloadFiles(list(download));
        lock.lock();
        try {
            Log.i(TAG, "waiting!");
            notDone.await();
            Log.i(TAG, "RUNNING!");
            Assert.assertTrue(download.file.exists());
        } catch (InterruptedException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }
}
