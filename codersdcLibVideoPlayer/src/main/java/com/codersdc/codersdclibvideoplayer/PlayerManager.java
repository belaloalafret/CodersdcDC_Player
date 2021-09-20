package com.codersdc.codersdclibvideoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import com.codersdc.codersdclibvideoplayer.helpers.DemoUtil;
import com.codersdc.codersdclibvideoplayer.models.Sample;
import com.codersdc.codersdclibvideoplayer.models.TrackInfo;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.gms.cast.framework.CastContext;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.util.MimeTypes.APPLICATION_SUBRIP;

public class PlayerManager implements Player.EventListener, SessionAvailabilityListener {

    private final ArrayList<Sample> mediaQueue;
    private final PlayerControlView castControlView;
    private final PlayerView localPlayerView;
    private final SimpleExoPlayer exoPlayer;
    private final DataSource.Factory dataSourceFactory;
    private final DefaultTrackSelector trackSelector;
    private final boolean isFromFile;
    private final MediaSourceFactory mediaSourceFactory;
    private final CastPlayer castPlayer;
    private final PlayerStateChangedListener playerStateChangedListener;
    private SparseArray<DefaultTrackSelector.SelectionOverride> overrides = null;
    private List<MediaItem> mediaItems;
    private boolean startAutoPlay;
    private Player currentPlayer;
    private int currentItemIndex;

    public PlayerManager(
            PlayerView localPlayerView,
            PlayerControlView castControlView,
            Context context,
            CastContext castContext,
            boolean isFromFile,
            PlayerStateChangedListener playerStateChangedListener) {

        this.playerStateChangedListener = playerStateChangedListener;
        this.castControlView = castControlView;
        this.isFromFile = isFromFile;
        this.localPlayerView = localPlayerView;
        this.mediaQueue = new ArrayList<>();
        currentItemIndex = C.INDEX_UNSET;

        dataSourceFactory = DemoUtil.getDataSourceFactory(/* context= */ context);
        DefaultTrackSelector.ParametersBuilder builder =
                new DefaultTrackSelector.ParametersBuilder(/* context= */ context);
        DefaultTrackSelector.Parameters trackSelectorParameters = builder.build();
        trackSelector = new DefaultTrackSelector(/* context= */ context);
        trackSelector.setParameters(trackSelectorParameters);

        RenderersFactory renderersFactory =
                DemoUtil.buildRenderersFactory(/* context= */ context, false);
        mediaSourceFactory =
                new DefaultMediaSourceFactory(dataSourceFactory);

        exoPlayer =
                new SimpleExoPlayer.Builder(/* context= */ context, renderersFactory)
                        .setMediaSourceFactory(mediaSourceFactory)
                        .setTrackSelector(trackSelector)
                        .build();


        exoPlayer.addListener(new PlayerEventListener());
        exoPlayer.addAnalyticsListener(new EventLogger(trackSelector));


        exoPlayer.setPlayWhenReady(startAutoPlay);
        localPlayerView.setPlayer(exoPlayer);


        castPlayer = new CastPlayer(castContext);
        castPlayer.addListener(this);
        castPlayer.setSessionAvailabilityListener(this);

        if (castControlView != null) {
            castControlView.setPlayer(castPlayer);
        }

        clearStartPosition();

        setCurrentPlayer(castPlayer.isCastSessionAvailable() ? castPlayer : exoPlayer);
    }

    public static PlayerManager createPlayerManager(
            PlayerView localPlayerView,
            PlayerControlView castControlView,
            Context context,
            CastContext castContext,
            boolean isFromFile,
            PlayerStateChangedListener playerStateChangedListener) {

        PlayerManager playerManager = new PlayerManager(
                localPlayerView,
                castControlView,
                context,
                castContext,
                isFromFile,
                playerStateChangedListener);
        playerManager.init();
        return playerManager;
    }

    public TrackInfo[] initTrackInfo(String trackSelectionAuto) {
        ArrayList<TrackGroup> trackGroups = initTrackSelector();
        TrackInfo[] trackInfos = new TrackInfo[trackGroups.get(0).length + 1];
        for (int trackIndex = 0; trackIndex < trackGroups.get(0).length; trackIndex++) {
            trackInfos[trackIndex] = new TrackInfo(0, trackIndex, trackGroups.get(0).getFormat(trackIndex), null, false, trackGroups.get(0).getFormat(trackIndex).height, trackGroups.get(0).getFormat(trackIndex).width);
        }
        trackInfos[trackGroups.get(0).length] = new TrackInfo(0, 0, trackGroups.get(0).getFormat(0), trackSelectionAuto, true, 0, 0);
        return trackInfos;
    }

    public ArrayList<TrackGroup> initTrackSelector() {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = getTrackSelector().getCurrentMappedTrackInfo();
        ArrayList<TrackGroup> trackGroups = new ArrayList<>();
        if (mappedTrackInfo != null) {
            for (int i = 0; i < mappedTrackInfo.getTrackGroups(0).length; i++) {
                trackGroups.add(mappedTrackInfo.getTrackGroups(0).get(i));
            }
        }
        overrides = new SparseArray<>();
        int maxOverrides = Math.min(overrides.size(), 1);
        for (int i = 0; i < maxOverrides; i++) {
            DefaultTrackSelector.SelectionOverride override = overrides.get(i);
            overrides.put(override.groupIndex, override);
        }
        return trackGroups;
    }

    public void ClearOverrides() {
        if (overrides.size() > 0) {
            overrides.clear();
        }
    }

    private void apply(DefaultTrackSelector.SelectionOverride override, TrackGroupArray trackGroupArray, DefaultTrackSelector defaultTrackSelector) {
        DefaultTrackSelector.Parameters parameters = defaultTrackSelector.getParameters();
        DefaultTrackSelector.ParametersBuilder builder = parameters.buildUpon();
        builder.clearSelectionOverrides(0).setRendererDisabled(0, false);
        if (override != null) {
            builder.setSelectionOverride(0, trackGroupArray, override);
        }
        defaultTrackSelector.setParameters(builder);
    }

    public void setQuality(int position, int type) {
        ClearOverrides();
        TrackGroupArray trackGroupArray = null;
        if (getTrackSelector().getCurrentMappedTrackInfo() != null) {
            trackGroupArray = getTrackSelector().getCurrentMappedTrackInfo().getTrackGroups(0);
        }
        DefaultTrackSelector defaultTrackSelector = getTrackSelector();
        if (type == 0) { // 0 Auto
            apply(null, trackGroupArray, defaultTrackSelector);
        } else { // 1 Select Quality
            overrides.put(0, new DefaultTrackSelector.SelectionOverride(0, position));
            DefaultTrackSelector.SelectionOverride override = overrides.get(0);
            apply(override, trackGroupArray, defaultTrackSelector);
        }
    }

    public boolean isSelectedQuality(int position, boolean isAuto) {
        TrackGroupArray trackGroupArray = null;
        if (trackSelector.getCurrentMappedTrackInfo() != null) {
            trackGroupArray = trackSelector.getCurrentMappedTrackInfo().getTrackGroups(0);
        }
        DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
        DefaultTrackSelector.SelectionOverride override = parameters.getSelectionOverride(0, trackGroupArray);
        if (isAuto) {
            return override == null;
        } else {
            return override != null && override.groupIndex == 0 && override.containsTrack(position);
        }
    }

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public void buildSubtitle(MediaItem mediaItem, String srt, File file) {
        Uri subtitleUri;
        if (isFromFile) {
            subtitleUri = Uri.fromFile(file);
        } else if (srt != null) {
            subtitleUri = Uri.parse(srt);
        } else {
            subtitleUri = Uri.parse("");
        }

        MediaItem.Subtitle subtitle = new MediaItem.Subtitle(subtitleUri, APPLICATION_SUBRIP, null, C.SELECTION_FLAG_DEFAULT);
        SingleSampleMediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(subtitle, C.TIME_UNSET);
        MergingMediaSource mergedSource = new MergingMediaSource(mediaSourceFactory.createMediaSource(mediaItem), subtitleSource);
        exoPlayer.setMediaSource(mergedSource);
        exoPlayer.prepare();
    }

    public void changeSubtitleTextFont(int fontSize) {
        if (localPlayerView != null && localPlayerView.getSubtitleView() != null) {
            localPlayerView.getSubtitleView().setFixedTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }
    }

    public void showSubtitle() {
        if (localPlayerView != null && localPlayerView.getSubtitleView() != null) {
            localPlayerView.getSubtitleView().setVisibility(View.VISIBLE);
        }
    }

    public void hideSubtitle() {
        if (localPlayerView != null && localPlayerView.getSubtitleView() != null) {
            localPlayerView.getSubtitleView().setVisibility(View.GONE);
        }
    }

    /**
     * This method is used to update videoPlayer's  seekBar
     */
    @SuppressLint("DefaultLocale")
    public void updateSeekBar() {
        if (getCurrentPlayer() == getExoPlayer()) {
            long duration = getExoPlayer() == null ? 0 : getExoPlayer().getDuration();
            long position = getExoPlayer() == null ? 0 : getExoPlayer().getCurrentPosition();
            long bufferedPosition = getExoPlayer() == null ? 0 : getExoPlayer().getBufferedPosition();

            if (duration > 0) {
                String toDur = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration), TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                String curDur = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(position), TimeUnit.MILLISECONDS.toMinutes(position) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(position)), TimeUnit.MILLISECONDS.toSeconds(position) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)));
                playerStateChangedListener.onPlayerStatusChanged(toDur, curDur, duration, position, bufferedPosition);
            }
        } else {
            long duration = getCastPlayer() == null ? 0 : getCastPlayer().getDuration();
            long position = getCastPlayer() == null ? 0 : getCastPlayer().getCurrentPosition();
            long bufferedPosition = getCastPlayer() == null ? 0 : getCastPlayer().getBufferedPosition();
            if (duration > 0) {
                String toDur = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration), TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                String curDur = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(position), TimeUnit.MILLISECONDS.toMinutes(position) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(position)), TimeUnit.MILLISECONDS.toSeconds(position) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)));
                playerStateChangedListener.onPlayerStatusChanged(toDur, curDur, duration, position, bufferedPosition);
            }
        }
    }

    /**
     * Appends {@code sample} to the media queue.
     *
     * @param sample The {@link Sample} to append.
     */
    public void addItem(Sample sample) {
        mediaQueue.add(sample);
    }

    /**
     * Plays a specified queue item in the current player.
     *
     * @param itemIndex The index of the item to play.
     */
    public void selectQueueItem(int itemIndex) {
        setCurrentItem(itemIndex);
    }

    /**
     * Releases the manager and the players that it holds.
     */
    public void release() {
        currentItemIndex = C.INDEX_UNSET;
        mediaQueue.clear();
        castPlayer.setSessionAvailabilityListener(null);
        castPlayer.release();
        localPlayerView.setPlayer(null);
        exoPlayer.release();
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public CastPlayer getCastPlayer() {
        return castPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    private void setCurrentPlayer(Player currentPlayer) {
        if (this.currentPlayer == currentPlayer) {
            return;
        }
        // View management.
        if (currentPlayer == exoPlayer) {
            localPlayerView.setVisibility(View.VISIBLE);
            if (castControlView != null)
                castControlView.hide();
        } else /* currentPlayer == castPlayer */ {
            localPlayerView.setVisibility(View.GONE);
            if (castControlView != null)
                castControlView.show();
        }

        // Player state management.
        long playbackPositionMs = C.TIME_UNSET;
        int windowIndex = C.INDEX_UNSET;
        boolean playWhenReady = false;

        Player previousPlayer = this.currentPlayer;
        // Save state from the previous player.
        if (previousPlayer != null) {
            int playbackState = previousPlayer.getPlaybackState();
            if (playbackState != Player.STATE_ENDED) {
                playbackPositionMs = previousPlayer.getCurrentPosition();
                playWhenReady = previousPlayer.getPlayWhenReady();
                windowIndex = previousPlayer.getCurrentWindowIndex();
                if (windowIndex != currentItemIndex) {
                    playbackPositionMs = C.TIME_UNSET;
                    windowIndex = currentItemIndex;
                }
            }
            previousPlayer.stop();
            previousPlayer.clearMediaItems();
        }

        this.currentPlayer = currentPlayer;
        // Media queue management.
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        for (int i = 0; i < mediaQueue.size(); i++) {

            MediaItem.Subtitle subtitle =
                    new MediaItem.Subtitle(
                            Uri.parse(mediaQueue.get(i).srt),
                            APPLICATION_SUBRIP, // The correct MIME type.
                            "en"); // The subtitle language. May be null.


            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(mediaQueue.get(i).uri)
                    .setMimeType(mediaQueue.get(i).mimeType)
                    .setSubtitles(Lists.newArrayList(subtitle))
                    .build();

            mediaItems.add(mediaItem);
        }

        currentPlayer.setMediaItems(mediaItems, windowIndex, playbackPositionMs);
        currentPlayer.setPlayWhenReady(playWhenReady);
        currentPlayer.prepare();
    }

    private void init() {
        setCurrentPlayer(castPlayer.isCastSessionAvailable() ? castPlayer : exoPlayer);
    }

    private void updateCurrentItemIndex() {
        try {
            if (currentPlayer != null) {
                int playbackState = currentPlayer.getPlaybackState();
                maybeSetCurrentItemAndNotify(
                        playbackState != STATE_IDLE && playbackState != Player.STATE_ENDED
                                ? currentPlayer.getCurrentWindowIndex() : C.INDEX_UNSET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCurrentItem(int itemIndex) {
        maybeSetCurrentItemAndNotify(itemIndex);
        if (currentPlayer.getCurrentTimeline().getWindowCount() != mediaQueue.size()) {
            // This only happens with the cast player. The receiver app in the cast device clears the
            // timeline when the last item of the timeline has been played to end.
            mediaItems = new ArrayList<>();
            for (int i = 0; i < mediaQueue.size(); i++) {
                MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(mediaQueue.get(i).uri)
                        .setMimeType(mediaQueue.get(i).mimeType)
                        .build();
                mediaItems.add(mediaItem);
            }
            currentPlayer.setMediaItems(mediaItems, itemIndex, C.TIME_UNSET);
        } else {
            currentPlayer.seekTo(itemIndex, C.TIME_UNSET);
        }
        currentPlayer.setPlayWhenReady(true);

        buildSubtitle(mediaItems.get(itemIndex), mediaQueue.get(itemIndex).srt, mediaQueue.get(itemIndex).srtFile);
    }

    /**
     * Returns the size of the media queue.
     */
    public int getMediaQueueSize() {
        return mediaQueue.size();
    }

    /**
     * Returns the item at the given index in the media queue.
     *
     * @param position The index of the item.
     * @return The item at the given index in the media queue.
     */
    public Sample getItem(int position) {
        return mediaQueue.get(position);
    }

    private void maybeSetCurrentItemAndNotify(int currentItemIndex) {
        if (this.currentItemIndex != currentItemIndex) {
            this.currentItemIndex = currentItemIndex;
        }
    }

    protected void clearStartPosition() {
        startAutoPlay = true;
    }

    @Override
    public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
        updateCurrentItemIndex();
    }

    @Override
    public void onCastSessionAvailable() {
        setCurrentPlayer(castPlayer);
    }


    @Override
    public void onCastSessionUnavailable() {
        setCurrentPlayer(exoPlayer);
    }

    /**
     * Listener for changes in the player status.
     */
    public interface PlayerStateChangedListener {
        void onPlayerStatusChanged(String toDur, String curDur, long duration, long position, long bufferedPosition);

        void onPlayerStateIdle();

        void onPlayerStateBuffering();

        void onPlayerStateEnded();

        void onPlayerStateReady();
    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onPlaybackStateChanged(@Player.State int playbackState) {
            updateCurrentItemIndex();
            updateSeekBar();
            switch (playbackState) {
                case STATE_IDLE:
                    playerStateChangedListener.onPlayerStateIdle();
                    break;

                case Player.STATE_BUFFERING:
                    playerStateChangedListener.onPlayerStateBuffering();
                    break;

                case Player.STATE_ENDED:
                    playerStateChangedListener.onPlayerStateEnded();
                    break;

                case Player.STATE_READY:
                    playerStateChangedListener.onPlayerStateReady();
                    break;

                default:
                    break;
            }
        }


        public void onPlayerError(@NonNull ExoPlaybackException e) {
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(@NonNull TrackGroupArray trackGroups, @NonNull TrackSelectionArray trackSelections) {
        }
    }

}
