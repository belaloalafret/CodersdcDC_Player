package com.codersdc.codersdclibvideoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.codersdc.codersdclibvideoplayer.models.Sample;
import com.codersdc.codersdclibvideoplayer.models.TrackInfo;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.DefaultEventListener;
import com.google.android.exoplayer2.Player.DiscontinuityReason;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.framework.CastContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codersdc.codersdclibvideoplayer.helpers.DataConstants.MIME_TYPE_DASH;
import static com.codersdc.codersdclibvideoplayer.helpers.DataConstants.MIME_TYPE_HLS;
import static com.codersdc.codersdclibvideoplayer.helpers.DataConstants.MIME_TYPE_SS;
import static com.codersdc.codersdclibvideoplayer.helpers.DataConstants.MIME_TYPE_VIDEO_MP4;
import static com.google.android.exoplayer2.Player.STATE_IDLE;

/**
 * Manages players and an internal media queue for the ExoPlayer/Cast demo app.
 */
public final class PlayerManager extends DefaultEventListener implements CastPlayer.SessionAvailabilityListener {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    public static String srtUrl;
    public static String mLicenseServer = "";
    public static String mAxDrmMessage = "";
    private static SparseArray<DefaultTrackSelector.SelectionOverride> overrides = null;
    private static DefaultDataSourceFactory dataSourceFactory;
    private static boolean isFromFile;
    private static DefaultTrackSelector trackSelector;
    private static PlayerView localPlayerView;
    private static SimpleExoPlayer exoPlayer;
    private static ConcatenatingMediaSource dynamicConcatenatingMediaSource;
    private final PlayerControlView castControlView;
    private final CastPlayer castPlayer;
    private final ArrayList<Sample> mediaQueue;
    private final PlayerStateChangedListener playerStateChangedListener;
    private Context mContext;
    private boolean castMediaQueueCreationPending;
    private int currentItemIndex;
    private Player currentPlayer;

    //--------------------------------------------------------------------------------------------//
    public PlayerManager(
            Fragment fragment,
            PlayerStateChangedListener playerStateChangedListener,
            PlayerView localPlayerView,
            PlayerControlView castControlView,
            Context context,
            CastContext castContext,
            boolean isFromFile) {
        this.mContext = context;
        this.playerStateChangedListener = playerStateChangedListener;
        PlayerManager.localPlayerView = localPlayerView;
        this.castControlView = castControlView;
        PlayerManager.isFromFile = isFromFile;
        mediaQueue = new ArrayList<>();
        currentItemIndex = C.INDEX_UNSET;

        DefaultAllocator allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE * 3);
        DefaultLoadControl loadControl = new DefaultLoadControl(allocator, 5000, 15000,
                5000, 4500, -1,
                true);
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(context, "VideoPlayer"),
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true /* allowCrossProtocolRedirects */);
        dataSourceFactory = new DefaultDataSourceFactory(
                context,
                null,
                httpDataSourceFactory
        );

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context, null);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        DrmSessionManager<FrameworkMediaCrypto> sessionManager = null;
        try {
            sessionManager = buildDrmSessionManager(
                    context, getLicenseServer(), getAxDrmMessage());
        } catch (UnsupportedDrmException e) {
            e.printStackTrace();
        }
        if (!(getAxDrmMessage().equals("") && getLicenseServer().equals("")))
            exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl, sessionManager);
        else
            exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);

        exoPlayer.addListener(this);
        localPlayerView.setPlayer(exoPlayer);

        castPlayer = new CastPlayer(castContext);
        castPlayer.addListener(this);
        castPlayer.setSessionAvailabilityListener(this);
        castControlView.setPlayer(castPlayer);
    }

    //--------------------------------------------------------------------------------------------//
    public static PlayerManager createPlayerManager(Fragment fragment,
                                                    PlayerStateChangedListener playerStateChangedListener,
                                                    PlayerView localPlayerView,
                                                    PlayerControlView castControlView,
                                                    Context context,
                                                    CastContext castContext,
                                                    boolean isFromFile) {
        PlayerManager playerManager = new PlayerManager(fragment,
                playerStateChangedListener,
                localPlayerView,
                castControlView,
                context,
                castContext,
                isFromFile);
        playerManager.init();
        localPlayerView.getSubtitleView().setVisibility(View.GONE);
        return playerManager;
    }

    //--------------------------------------------------------------------------------------------//
    public static MediaSource buildMediaSource(Sample sample) {
        if (isFromFile) {
            Uri uri = Uri.fromFile(sample.file);
            return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        } else {
            Uri uri = Uri.parse(String.valueOf(sample.uri));
            switch (sample.mimeType) {
                case MIME_TYPE_SS:
                    return new SsMediaSource.Factory(
                            new DefaultSsChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                            .createMediaSource(uri);
                case MIME_TYPE_DASH:
                    return new DashMediaSource.Factory(
                            new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                            .createMediaSource(uri);
                case MIME_TYPE_HLS:
                    return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                case MIME_TYPE_VIDEO_MP4:
                    return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                default: {
                    throw new IllegalStateException("Unsupported type: " + sample.mimeType);
                }
            }
        }
    }

    //--------------------------------------------------------------------------------------------//
    private static MediaQueueItem buildMediaQueueItem(Sample sample) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, sample.name);
        MediaTrack arabicSubtitle = new MediaTrack.Builder(1, MediaTrack.TYPE_TEXT)
                .setName("Arabic Subtitle")
                .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                .setContentId(srtUrl)
                .build();
        List<MediaTrack> mediaTracks = new ArrayList<>();
        mediaTracks.add(arabicSubtitle);
        MediaInfo mediaInfo;
        if (sample.mimeType.equals(MIME_TYPE_HLS)) {
            mediaInfo = new MediaInfo.Builder(sample.uri)
                    .setStreamType(MediaInfo.STREAM_TYPE_LIVE).setContentType(MimeTypes.APPLICATION_M3U8)
                    .setMetadata(movieMetadata).setMediaTracks(mediaTracks).build();
        } else {
            mediaInfo = new MediaInfo.Builder(sample.uri)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setContentType(sample.mimeType)
                    .setMetadata(movieMetadata).build();
        }
        return new MediaQueueItem.Builder(mediaInfo).build();
    }

    //--------------------------------------------------------------------------------------------//
    public static boolean isSelectedQuality(int position, boolean isAuto) {
        TrackGroupArray trackGroupArray = trackSelector.getCurrentMappedTrackInfo().getTrackGroups(0);
        DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
        DefaultTrackSelector.SelectionOverride override = parameters.getSelectionOverride(0, trackGroupArray);
        if (isAuto) {
            return override == null;
        } else {
            return override != null && override.groupIndex == 0 && override.containsTrack(position);
        }
    }
    //--------------------------------------------------------------------------------------------//

    //--------------------------------------------------------------------------------------------//
    // Queue manipulation methods.
    public static DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    //--------------------------------------------------------------------------------------------//
    public static void buildSubtitle(int currentItemIndex, String srt, File file) {
        Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE, Format.NO_VALUE, "en", null, Format.OFFSET_SAMPLE_RELATIVE);
        Uri uri;
        if (isFromFile) {
            uri = Uri.fromFile(file);
        } else if (srt != null) {
            uri = Uri.parse(srt);
        } else {
            uri = Uri.parse("");
        }
        SingleSampleMediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(uri, textFormat, C.TIME_UNSET);
        MergingMediaSource mergedSource = new MergingMediaSource(dynamicConcatenatingMediaSource.getMediaSource(currentItemIndex), subtitleSource);
        exoPlayer.prepare(mergedSource, false, false);
    }

    //--------------------------------------------------------------------------------------------//
    public static void changeSubtitleTextFont(int fontSize) {
        localPlayerView.getSubtitleView().setFixedTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
    }

    //--------------------------------------------------------------------------------------------//
    public static void showSubtitle() {
        localPlayerView.getSubtitleView().setVisibility(View.VISIBLE);
    }

    public static void hideSubtitle() {
        localPlayerView.getSubtitleView().setVisibility(View.GONE);
    }

    public static TrackInfo[] initTrackInfo(String trackSelectionAuto) {
        ArrayList<TrackGroup> trackGroups = initTrackSelector();
        TrackInfo[] trackInfos = new TrackInfo[trackGroups.get(0).length + 1];
        for (int trackIndex = 0; trackIndex < trackGroups.get(0).length; trackIndex++) {
            trackInfos[trackIndex] = new TrackInfo(0, trackIndex, trackGroups.get(0).getFormat(trackIndex), null, false, trackGroups.get(0).getFormat(trackIndex).height, trackGroups.get(0).getFormat(trackIndex).width);
        }
        trackInfos[trackGroups.get(0).length] = new TrackInfo(0, 0, trackGroups.get(0).getFormat(0), trackSelectionAuto, true, 0, 0);
        return trackInfos;
    }

    public static ArrayList<TrackGroup> initTrackSelector() {
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

    public static void ClearOverrides() {
        if (overrides.size() > 0) {
            overrides.clear();
        }
    }

    private static void apply(DefaultTrackSelector.SelectionOverride override, TrackGroupArray trackGroupArray, DefaultTrackSelector defaultTrackSelector) {
        DefaultTrackSelector.Parameters parameters = defaultTrackSelector.getParameters();
        DefaultTrackSelector.ParametersBuilder builder = parameters.buildUpon();
        builder.clearSelectionOverrides(0).setRendererDisabled(0, false);
        if (override != null) {
            builder.setSelectionOverride(0, trackGroupArray, override);
        }
        defaultTrackSelector.setParameters(builder);
    }

    public static void setQuality(int position, int type) {
        ClearOverrides();
        TrackGroupArray trackGroupArray = PlayerManager.getTrackSelector().getCurrentMappedTrackInfo().getTrackGroups(0);
        DefaultTrackSelector defaultTrackSelector = PlayerManager.getTrackSelector();
        if (type == 0) { // 0 Auto
            apply(null, trackGroupArray, defaultTrackSelector);
        } else { // 1 Select Quality
            overrides.put(0, new DefaultTrackSelector.SelectionOverride(0, position));
            DefaultTrackSelector.SelectionOverride override = overrides.get(0);
            apply(override, trackGroupArray, defaultTrackSelector);
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
            if (duration > 0) {
                String toDur = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration), TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                String curDur = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(position), TimeUnit.MILLISECONDS.toMinutes(position) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(position)), TimeUnit.MILLISECONDS.toSeconds(position) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)));
                playerStateChangedListener.onPlayerStatusChanged(toDur, curDur);
            }
        } else {
            long duration = getCastPlayer() == null ? 0 : getCastPlayer().getDuration();
            long position = getCastPlayer() == null ? 0 : getCastPlayer().getCurrentPosition();
            if (duration > 0) {
                String toDur = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration), TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                String curDur = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(position), TimeUnit.MILLISECONDS.toMinutes(position) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(position)), TimeUnit.MILLISECONDS.toSeconds(position) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)));
                playerStateChangedListener.onPlayerStatusChanged(toDur, curDur);
            }
        }
    }

    //--------------------------------------------------------------------------------------------//
    public String getLicenseServer() {
        return mLicenseServer;
    }

    public String getAxDrmMessage() {
        return mAxDrmMessage;
    }

    //--------------------------------------------------------------------------------------------//
    private HttpDataSource.Factory buildHttpDataSourceFactory(Context context) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(context,
                PlayerManager.class.getName()), null);
    }
    //--------------------------------------------------------------------------------------------//

    //--------------------------------------------------------------------------------------------//
    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(Context context,
                                                                                  String licenseUrl, String drmToken) throws UnsupportedDrmException {
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(context));
        if (drmToken != null) {
            drmCallback.setKeyRequestProperty("X-AxDRM-Message", drmToken);
        }
        return new DefaultDrmSessionManager<>(C.WIDEVINE_UUID, FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID),
                drmCallback, null);
    }
    //--------------------------------------------------------------------------------------------//

    //--------------------------------------------------------------------------------------------//
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    //--------------------------------------------------------------------------------------------//

    //--------------------------------------------------------------------------------------------//
    private void setCurrentPlayer(Player currentPlayer) {
        if (this.currentPlayer == currentPlayer) {
            return;
        }
        // View management.
        if (currentPlayer == exoPlayer) {
            localPlayerView.setVisibility(View.VISIBLE);
            castControlView.hide();
        } else /* currentPlayer == castPlayer */ {
            localPlayerView.setVisibility(View.GONE);
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.release();
            }
            castControlView.show();
        }
        // Player state management.
        int windowIndex = C.INDEX_UNSET;
        boolean playWhenReady = false;
        if (this.currentPlayer != null) {
            int playbackState = this.currentPlayer.getPlaybackState();
            if (playbackState != Player.STATE_ENDED) {
                playWhenReady = this.currentPlayer.getPlayWhenReady();
                windowIndex = this.currentPlayer.getCurrentWindowIndex();
                if (windowIndex != currentItemIndex) {
                    windowIndex = currentItemIndex;
                }
            }
        }
        this.currentPlayer = currentPlayer;
        // Media queue management.
        castMediaQueueCreationPending = currentPlayer == castPlayer;
        if (currentPlayer == exoPlayer) {
            dynamicConcatenatingMediaSource = new ConcatenatingMediaSource();
        }
        // Playback transition.
        if (windowIndex != C.INDEX_UNSET) {
            setCurrentItem(windowIndex, 0, playWhenReady);
        }
    }

    //--------------------------------------------------------------------------------------------//
    public ConcatenatingMediaSource getDynamicConcatenating() {
        return dynamicConcatenatingMediaSource;
    }
    //--------------------------------------------------------------------------------------------//

    //--------------------------------------------------------------------------------------------//
    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }
    //--------------------------------------------------------------------------------------------//

    /**
     * Plays a specified queue item in the current player.
     *
     * @param itemIndex The index of the item to play.
     */
    public void selectQueueItem(int itemIndex) {
        setCurrentItem(itemIndex, C.TIME_UNSET, true);
    }

    /**
     * Returns the index of the currently played item.
     */
    public int getCurrentItemIndex() {
        return currentItemIndex;
    }

    /**
     * Appends {@code sample} to the media queue.
     *
     * @param sample The {@link Sample} to append.
     */
    public void addItem(Sample sample) {
        mediaQueue.add(sample);
        if (currentPlayer == exoPlayer) {
            dynamicConcatenatingMediaSource.addMediaSource(buildMediaSource(sample));
        } else {
            castPlayer.addItems(buildMediaQueueItem(sample));
        }
    }

    //--------------------------------------------------------------------------------------------//
    public CastPlayer getCastPlayer() {
        return castPlayer;
    }

    /**
     * Dispatches a given {@link KeyEvent} to the corresponding view of the current player.
     *
     * @param event The {@link KeyEvent}.
     * @return Whether the event was handled by the target view.
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (currentPlayer == exoPlayer) {
            return localPlayerView.dispatchKeyEvent(event);
        } else /* currentPlayer == castPlayer */ {
            return castControlView.dispatchKeyEvent(event);
        }
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
        trackSelector = null;
    }

    //--------------------------------------------------------------------------------------------//
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        updateCurrentItemIndex();
        updateSeekBar();
        switch (playbackState) {
            case STATE_IDLE:
                playerStateChangedListener.onPlayerStateIdle();
                buildSubtitle(0, mediaQueue.get(0).srt, null);
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

    //--------------------------------------------------------------------------------------------//
    @Override
    public void onPositionDiscontinuity(@DiscontinuityReason int reason) {
        updateCurrentItemIndex();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
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

    //--------------------------------------------------------------------------------------------//
    private void init() {
        setCurrentPlayer(castPlayer.isCastSessionAvailable() ? castPlayer : exoPlayer);
    }

    //--------------------------------------------------------------------------------------------//
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
    //--------------------------------------------------------------------------------------------//

    /**
     * Starts playback of the item at the given position.
     *
     * @param itemIndex     The index of the item to play.
     * @param positionMs    The position at which playback should start.
     * @param playWhenReady Whether the player should proceed when ready to do so.
     */
    private void setCurrentItem(int itemIndex, long positionMs, boolean playWhenReady) {
        maybeSetCurrentItemAndNotify(itemIndex);
        if (castMediaQueueCreationPending) {
            MediaQueueItem[] items = new MediaQueueItem[mediaQueue.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = buildMediaQueueItem(mediaQueue.get(i));
            }
            castMediaQueueCreationPending = false;
            castPlayer.loadItems(items, itemIndex, positionMs, Player.REPEAT_MODE_OFF);
        } else {
            currentPlayer.seekTo(itemIndex, positionMs);
            currentPlayer.setPlayWhenReady(playWhenReady);
        }
        buildSubtitle(itemIndex, mediaQueue.get(itemIndex).srt, mediaQueue.get(itemIndex).srtFile);
    }

    //--------------------------------------------------------------------------------------------//
    private void maybeSetCurrentItemAndNotify(int currentItemIndex) {
        if (this.currentItemIndex != currentItemIndex) {
            int oldIndex = this.currentItemIndex;
            this.currentItemIndex = currentItemIndex;
        }
    }
    //--------------------------------------------------------------------------------------------//

    /**
     * Listener for changes in the player status.
     */
    public interface PlayerStateChangedListener {
        void onPlayerStatusChanged(String toDur, String curDur);

        void onPlayerStateIdle();

        void onPlayerStateBuffering();

        void onPlayerStateEnded();

        void onPlayerStateReady();
    }
    //--------------------------------------------------------------------------------------------//
}
