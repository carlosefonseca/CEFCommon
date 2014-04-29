package com.carlosefonseca.common.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;
import com.carlosefonseca.common.CFApp;
import de.greenrobot.event.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class AudioPlayer {

    private static final String TAG = CodeUtils.getTag(AudioPlayer.class);

    private static final Context c = CFApp.getContext();

    private static MediaPlayer currentMediaPlayer;
    private static File currentFile;

    private static Queue<MediaPlayerWrapper> queue = new LinkedList<MediaPlayerWrapper>();

    @Deprecated
    private static final ArrayList<AudioPlayerListener> playerListeners = new ArrayList<AudioPlayerListener>();

    @Deprecated
    public interface AudioPlayerListener {
        void onAudioStart(MediaPlayer mediaPlayer, File file);
        void onAudioStop(MediaPlayer mediaPlayer, File file);
    }

    public static class AudioPlayerNotification {
        public final Status status;
        public final File file;

        public enum Status { PLAY, PAUSE, STOP}

        public AudioPlayerNotification(Status status, File file) {
            this.status = status;
            this.file = file;
        }

        public static void PostStart(File file) {
            EventBus.getDefault().postSticky(new AudioPlayerNotification(Status.PLAY, file));
        }
        public static void PostPause(File file) {
            EventBus.getDefault().postSticky(new AudioPlayerNotification(Status.PAUSE, file));
        }
        public static void PostStop(File file) {
            EventBus.getDefault().postSticky(new AudioPlayerNotification(Status.STOP, file));
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
     * @param mediaPlayer
     * @param file
     */
    private static void play(@NotNull MediaPlayer mediaPlayer, @Nullable File file) {
        stop();

        currentMediaPlayer = mediaPlayer;
        currentMediaPlayer.setOnCompletionListener(onEnd.instance);
        currentMediaPlayer.start();

        currentFile = file;

        for (AudioPlayerListener playerListener : playerListeners) {
            playerListener.onAudioStart(mediaPlayer, file);
        }
        AudioPlayerNotification.PostStart(currentFile);

        Log.v(TAG, "" + (file != null ? file.getName() : "???") + " Playing.");
    }


    /**
     * Starts playing the file, stopping another that was playing.
     */
    public static void playFile(File audioFile) {
        if (!audioFile.exists()) {
            Log.i(TAG, "File " + audioFile + " doesn't exist.");
            return;
        }
        MediaPlayer mediaPlayerForFile = getMediaPlayerForFile(c, audioFile);
        if (mediaPlayerForFile != null) {
            play(mediaPlayerForFile, audioFile);
        }
    }

    public static void stop() {
        if (isPlaying()) {
            currentMediaPlayer.stop();
            for (AudioPlayerListener playerListener : playerListeners) {
                playerListener.onAudioStop(currentMediaPlayer, currentFile);
            }
            AudioPlayerNotification.PostStop(currentFile);
            currentMediaPlayer = null;
            queue.clear();
        }
    }

    /**
     * Adds the file to the queue or simply plays the file is nothing is playing
     *
     * @param audioFile
     */
    public static void queueFile(@NotNull File audioFile) {
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


    @Deprecated
    public static void addPlayerListener(AudioPlayerListener l) {
        if (!playerListeners.contains(l)) playerListeners.add(l);
    }

    @Deprecated
    public static void removePlayerListener(AudioPlayerListener l) {
        playerListeners.remove(l);
    }

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
        if (audioFile.exists()) {
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

    MediaPlayer.OnCompletionListener playEnded = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            pause();
        }
    };


    public static void pause() {
        if (currentMediaPlayer == null) {
            Log.e(TAG, new Exception("Media player not initialized"));
            return;
        }
        currentMediaPlayer.pause();
        AudioPlayerNotification.PostPause(currentFile);
    }

    public static void resume() {
        if (currentMediaPlayer == null) {
            Log.e(TAG, new Exception("Media player not initialized"));
            return;
        }
        currentMediaPlayer.start();
        AudioPlayerNotification.PostStart(currentFile);
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

    static class onEnd implements MediaPlayer.OnCompletionListener {
        static onEnd instance = new onEnd();

        private onEnd() {
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            for (AudioPlayerListener playEndListener : playerListeners) {
                playEndListener.onAudioStop(mediaPlayer, currentFile);
            }
            AudioPlayerNotification.PostStop(currentFile);
            play();
        }
    }

    static class MediaPlayerWrapper {
        MediaPlayer mediaPlayer;
        File file;

        MediaPlayerWrapper(MediaPlayer mediaPlayer, File file) {
            this.mediaPlayer = mediaPlayer;
            this.file = file;
        }
    }
}
