package com.carlosefonseca.common.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;
import com.carlosefonseca.common.CFApp;
import de.greenrobot.event.EventBus;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class AudioPlayer {

    private static final String TAG = CodeUtils.getTag(AudioPlayer.class);

    private static final Context c = CFApp.getContext();
    public static final int STOPPED = 0;
    public static final int PLAYING = 1;
    public static final int PAUSED = 2;

    @Nullable private static MediaPlayer currentMediaPlayer;
    private static File currentFile;

    private static Queue<MediaPlayerWrapper> queue = new LinkedList<>();

    public static class AudioPlayerNotification {
        public final Status status;
        public final File file;
        public final MediaPlayer mediaPlayer;

        public enum Status { PLAY, PAUSE, STOP}

        public AudioPlayerNotification(Status status, @Nullable File file, @Nullable MediaPlayer mediaPlayer) {
            this.status = status;
            this.file = file;
            this.mediaPlayer = mediaPlayer;
        }

        @Override
        public String toString() {
            return String.format("AudioNotif:%-5s-%s", status, file != null ? file.getName() : "");
        }

        public static void PostStart(@Nullable File file) {
            EventBus.getDefault().postSticky(new AudioPlayerNotification(Status.PLAY, file, null));
        }

        public static void PostStart(@Nullable File file, MediaPlayer mediaPlayer) {
            EventBus.getDefault().postSticky(new AudioPlayerNotification(Status.PLAY, file, mediaPlayer));
        }

        public static void PostPause(File file) {
            EventBus.getDefault().postSticky(new AudioPlayerNotification(Status.PAUSE, file, null));
        }

        public static void PostPause(File file, MediaPlayer mediaPlayer) {
            EventBus.getDefault().postSticky(new AudioPlayerNotification(Status.PAUSE, file, mediaPlayer));
        }
        public static void PostStop(File file) {
            EventBus.getDefault().postSticky(new AudioPlayerNotification(Status.STOP, file, null));
        }

        public static void register(Object object) {
            EventBus.getDefault().register(object, AudioPlayerNotification.class);
        }

        public static void unregister(Object object) {
            EventBus.getDefault().unregister(object, AudioPlayerNotification.class);
        }
    }

//    // SINGLETON CRAP
//    static AudioPlayer instance = null;
//
//
//    public static AudioPlayer getInstance() {
//        if (instance == null) {
//            instance = new AudioPlayer();
//        }
//        return instance;
//    }
    // END SINGLETON CRAP

    static void play() {
        Log.i(TAG, "Poping queue (Q size: " + queue.size() + ")");
        MediaPlayerWrapper mediaPlayerWrapper = queue.poll();
        if (mediaPlayerWrapper != null) play(mediaPlayerWrapper.mediaPlayer, mediaPlayerWrapper.file);
    }

    /**
     * THE PLAY METHOD
     */
    private static void play(@NonNull MediaPlayer mediaPlayer, @Nullable File file) {
        stop();

        currentFile = file;
        currentMediaPlayer = mediaPlayer;
        currentMediaPlayer.setOnCompletionListener(onEnd.instance);
        currentMediaPlayer.start();


        AudioPlayerNotification.PostStart(currentFile, mediaPlayer);

        Log.v(TAG, "" + (file != null ? file.getName() : "???") + " Playing.");
    }


    /**
     * Starts playing the file, stopping another that was playing.
     */
    public static void playFile(File audioFile) {
        if (!audioFile.exists()) {
            Log.i(TAG, "File " + audioFile + " doesn't exist.");
            CodeUtils.toast("File " + audioFile + " doesn't exist.");
            return;
        }
        MediaPlayer mediaPlayerForFile = getMediaPlayerForFile(c, audioFile);
        if (mediaPlayerForFile != null) {
            play(mediaPlayerForFile, audioFile);
        }
    }

    /**
     * Starts playing the file, stopping another that was playing.
     */
    public static void playOrResumeFile(File audioFile) {
        if (!audioFile.exists()) {
            Log.i(TAG, "File " + audioFile + " doesn't exist.");
            CodeUtils.toast("File " + audioFile + " doesn't exist.");
            return;
        }
        if (currentFile != null && currentFile.equals(audioFile) && currentMediaPlayer != null &&
            !currentMediaPlayer.isPlaying()) {
            resume();
        } else {
            MediaPlayer mediaPlayerForFile = getMediaPlayerForFile(c, audioFile);
            if (mediaPlayerForFile != null) {
                play(mediaPlayerForFile, audioFile);
            }
        }
    }

    public static void stop() {
        if (isPlaying()) {
            if (currentMediaPlayer != null) currentMediaPlayer.stop();
            AudioPlayerNotification.PostStop(currentFile);
            currentMediaPlayer = null;
            currentFile = null;
            queue.clear();
        }
        EventBus.getDefault().removeStickyEvent(AudioPlayer.AudioPlayerNotification.class);
    }

    /**
     * Adds the file to the queue or simply plays the file is nothing is playing
     */
    public static void queueFile(@NonNull File audioFile) {
        if (!isPlaying()) {
            playFile(audioFile);
        } else {
            if (!audioFile.exists()) {
                Log.i(TAG, "" + audioFile + " doesn't exist.");
                return;
            }
            MediaPlayerWrapper mp = getWrappedMediaPlayerForFile(c, audioFile);
            if (mp != null) {
                queue.add(mp);
                Log.v(TAG, "" + audioFile.getName() + " queued. (Q size: "+queue.size()+")");
            }
        }
    }


    @Nullable
    public static MediaPlayerWrapper getWrappedMediaPlayerForFile(Context c, File audioFile) {
        if (!audioFile.exists()) {
            Log.i(TAG, "" + audioFile + " doesn't exist.");
            return null;
        }
        return new MediaPlayerWrapper(getMediaPlayerForFile(c, audioFile), audioFile);
    }


    /**
     * Creates a MediaPlayer object.
     *
     * @return New media player or null if it couldn't be created.
     */
    @Nullable
    public static MediaPlayer getMediaPlayerForFile(Context c, File audioFile) {
        if (audioFile.exists() && audioFile.isFile()) {
            Log.v(TAG, "" + audioFile.getName() + " Setting up...");
            MediaPlayer mediaPlayer = MediaPlayer.create(c, Uri.fromFile(audioFile));
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(onEnd.instance);
            } else {
                Log.e(TAG, new Exception("Failed to create MediaPlayer for audioFile " + audioFile.getName()));
            }
            return mediaPlayer;
        } else {
            Log.i(TAG, "" + audioFile.getName() + " doesn't exist!");
            Toast.makeText(c, "Audio File " + audioFile.getName() + " doesn't exist!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    public static void pause() {
        if (currentMediaPlayer == null) {
            Log.e(TAG, new Exception("Media player not initialized"));
            return;
        }
        currentMediaPlayer.pause();
        AudioPlayerNotification.PostPause(currentFile, currentMediaPlayer);
    }

    public static void resume() {
        if (currentMediaPlayer == null) {
            Log.e(TAG, new Exception("Media player not initialized"));
            return;
        }
        currentMediaPlayer.start();
        AudioPlayerNotification.PostStart(currentFile, currentMediaPlayer);
    }

/*
    private void pauseAudio() {
        if (currentMediaPlayer == null) {
            Log.e(TAG, new Exception("Media player not initialized"));
            return;
        }
        currentMediaPlayer.pause();
    }
*/


    public static boolean isPlaying() {
        return currentMediaPlayer != null && currentMediaPlayer.isPlaying();
    }

    public static int getStatus() {
        if (currentMediaPlayer != null) {
            return currentMediaPlayer.isPlaying()
                   ? PLAYING
                   : currentMediaPlayer.getCurrentPosition() < currentMediaPlayer.getDuration() ? PAUSED : STOPPED;
        } else {
            return STOPPED;
        }
    }

    @Nullable
    public static MediaPlayer getMediaPlayer() {
        return currentMediaPlayer;
    }

    static class onEnd implements MediaPlayer.OnCompletionListener {
        static onEnd instance = new onEnd();

        private onEnd() { }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            AudioPlayerNotification.PostStop(currentFile);
            play();
        }
    }

    public static File getCurrentFile() {
        return currentFile;
    }

    static class MediaPlayerWrapper {
        MediaPlayer mediaPlayer;
        File file;

        MediaPlayerWrapper(@Nullable MediaPlayer mediaPlayer, File file) {
            this.mediaPlayer = mediaPlayer;
            this.file = file;
        }
    }
}
