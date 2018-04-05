package com.ping.android.presentation.view.fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.ping.android.activity.R;
import com.ping.android.dagger.loggedin.call.CallComponent;
import com.ping.android.dagger.loggedin.call.video.VideoCallComponent;
import com.ping.android.dagger.loggedin.call.video.VideoCallModule;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.VideoCallPresenter;
import com.ping.android.presentation.view.adapter.OpponentsFromCallAdapter;
import com.ping.android.presentation.view.custom.DragFrameLayout;
import com.ping.android.utils.ResourceUtils;
import com.ping.android.utils.UiUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;


/**
 * QuickBlox team
 */
public class VideoConversationFragment extends BaseConversationFragment implements VideoCallPresenter.View {
    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLS_COUNT = 3;
    private static final long TOGGLE_CAMERA_DELAY = 1000;
    private static final long LOCAL_TRACk_INITIALIZE_DELAY = 500;
    private static final int RECYCLE_VIEW_PADDING = 2;
    private static final long UPDATING_USERS_DELAY = 2000;
    private static final long FULL_SCREEN_CLICK_DELAY = 1000;
    private static final int REQUEST_CODE_ATTACHMENT = 100;

    private String TAG = VideoConversationFragment.class.getSimpleName();

    private ToggleButton cameraToggle;
    private ImageView firstOpponentAvatarImageView;
    private LinearLayout controlsLayout;

    private QBRTCSurfaceView remoteFullScreenVideoView;
    private QBRTCSurfaceView localVideoView;
    private CameraState cameraState = CameraState.DISABLED_FROM_USER;
    private QBRTCVideoTrack localVideoTrack;

    private Map<Integer, QBRTCVideoTrack> videoTrackMap;
    private boolean connectionEstablished;
    private boolean allCallbacksInit;
    private boolean isCurrentCameraFront;

    private Animation animation1;
    private Timer toggleControlsTimer;

    @Inject
    VideoCallPresenter presenter;
    VideoCallComponent component;
    private LinearLayout outgoingViewContainer;
    private boolean shouldToggleControlsVisibility = false;

    public static VideoConversationFragment newInstance() {
        VideoConversationFragment fragment = new VideoConversationFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        outgoingViewContainer = view.findViewById(R.id.layout_background_outgoing_screen);
        presenter.create();
        return view;
    }

    @Override
    protected void configureOutgoingScreen() {
        allOpponentsTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        ringingTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
    }

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_video_conversation;
    }

    @Override
    protected void hideOutgoingScreen() {
        super.hideOutgoingScreen();
        outgoingViewContainer.setVisibility(View.GONE);
        timerChronometer.setVisibility(View.VISIBLE);
        // TODO should run timer to hide controls
        shouldToggleControlsVisibility = true;
        startTimerHideControls();
    }

    @Override
    public void onCallStarted() {
        super.onCallStarted();
        connectionEstablished = true;
        animateLocalViewToInitialPosition();
    }

    public void setDuringCallActionBar() {
        actionButtonsEnabled(true);
    }

    @Override
    protected void actionButtonsEnabled(boolean inability) {
        super.actionButtonsEnabled(inability);
        cameraToggle.setEnabled(inability);
        // inactivate toggle buttons
        cameraToggle.setActivated(inability);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if (!allCallbacksInit) {
            allCallbacksInit = true;
        }
    }

    @Override
    protected void hangup() {
        presenter.hangup();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setHasOptionsMenu(true);
        getComponent().inject(this);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        controlsLayout = view.findViewById(R.id.layout_action_buttons);
        animation1 = AnimationUtils.loadAnimation(getContext(), R.anim.to_middle);

        Log.i(TAG, "initViews");
        isCurrentCameraFront = true;
        localVideoView = view.findViewById(R.id.local_video_view);
        localVideoView.setZOrderMediaOverlay(true);

        DragFrameLayout dragLayout = view.findViewById(R.id.drag_layout);
        dragLayout.setDragFrameController(captured -> {
        });
        dragLayout.addDragView(localVideoView);

        toggleControlsTimer = new Timer();
        dragLayout.setOnClickListener(v -> {
            if (shouldToggleControlsVisibility) {
                toggleControlsVisibility();
            }
        });

        remoteFullScreenVideoView = view.findViewById(R.id.remote_video_view);

        cameraToggle = view.findViewById(R.id.toggle_camera);
        cameraToggle.setVisibility(View.VISIBLE);

        timerChronometer = view.findViewById(R.id.chronometer_timer_call);
        timerChronometer.setVisibility(View.GONE);

        firstOpponentAvatarImageView = view.findViewById(R.id.image_caller_avatar);

        actionButtonsEnabled(true);
        //restoreSession();
    }

    private Map<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        if (videoTrackMap == null) {
            videoTrackMap = new HashMap<>();
        }
        return videoTrackMap;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // If opponentUser changed camera state few times and last state was CameraState.ENABLED_FROM_USER
        // than we turn on cam, else we nothing change
//        if (cameraState != CameraState.DISABLED_FROM_USER) {
//            toggleCamera(true);
//        }
    }


    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
//        if (cameraState != CameraState.DISABLED_FROM_USER) {
//            toggleCamera(false);
//        }

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (connectionEstablished) {
            allCallbacksInit = false;
        } else {
            Log.d(TAG, "We are in dialing process yet!");
        }
    }

    private void removeVideoTrackRenderers() {
        Log.d(TAG, "removeVideoTrackRenderers");
        Log.d(TAG, "remove opponents video Tracks");
        Map<Integer, QBRTCVideoTrack> videoTrackMap = getVideoTrackMap();
        for (QBRTCVideoTrack videoTrack : videoTrackMap.values()) {
            if (videoTrack.getRenderer() != null) {
                Log.d(TAG, "remove opponent video Tracks");
                videoTrack.removeRenderer(videoTrack.getRenderer());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        presenter.destroy();
        removeVideoTrackRenderers();
        releaseViews();
    }

    private void releaseViews() {
        if (localVideoView != null) {
            localVideoView.release();
        }
        if (remoteFullScreenVideoView != null) {
            remoteFullScreenVideoView.release();
        }
        remoteFullScreenVideoView = null;
    }

    @Override
    public void onCallStopped() {
        super.onCallStopped();
        Log.i(TAG, "onCallStopped");
    }

    protected void initButtonsListener() {
        super.initButtonsListener();
        cameraToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cameraState != CameraState.DISABLED_FROM_USER) {
                switchCamera();
            }
        });
    }

    @Override
    protected void toggleAudio(boolean isEnable) {
        if (connectionEstablished) {
            startTimerHideControls();
        }
        presenter.toggleAudio(isEnable);
    }

    private void switchCamera() {
        if (connectionEstablished) {
            startTimerHideControls();
        }
        if (cameraState == CameraState.DISABLED_FROM_USER) {
            return;
        }
        cameraToggle.setEnabled(false);
        presenter.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean b) {
                Log.d(TAG, "camera switched, bool = " + b);
                isCurrentCameraFront = b;
                toggleCameraInternal();
                localVideoView.setAnimation(animation1);
                localVideoView.startAnimation(animation1);
            }

            @Override
            public void onCameraSwitchError(String s) {
                Log.d(TAG, "camera switch error " + s);
                cameraToggle.setEnabled(true);
            }
        });
    }

    private void toggleCameraInternal() {
        Log.d(TAG, "Camera was switched!");
        updateVideoView(localVideoView, isCurrentCameraFront);
        toggleCamera(true);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        presenter.toggleVideoLocal(isNeedEnableCam);
        if (connectionEstablished && !cameraToggle.isEnabled()) {
            cameraToggle.setEnabled(true);
        }
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        return null;
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private void fillVideoView(int userId, QBRTCSurfaceView videoView, QBRTCVideoTrack videoTrack,
                               boolean remoteRenderer) {
        videoTrack.removeRenderer(videoTrack.getRenderer());
        videoTrack.addRenderer(new VideoRenderer(videoView));
        if (userId != 0) {
            getVideoTrackMap().put(userId, videoTrack);
        }
        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront);
        }
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void fillVideoView(QBRTCSurfaceView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        fillVideoView(0, videoView, videoTrack, remoteRenderer);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror) {
        updateVideoView(surfaceViewRenderer, mirror, RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror, RendererCommon.ScalingType scalingType) {
        Log.i(TAG, "updateVideoView mirror:" + mirror + ", scalingType = " + scalingType);
        surfaceViewRenderer.setScalingType(scalingType);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.conversation_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        super.onOpponentsListUpdated(newUsers);
        Log.d(TAG, "updateOpponentsList(), newUsers = " + newUsers);
        runUpdateUsersNames(newUsers);
    }

    @Override
    public void onLocalVideoTrackReceive(BaseSession qbrtcSession, QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");
        localVideoTrack = videoTrack;
        cameraState = CameraState.NONE;

        if (localVideoView != null) {
            fillVideoView(localVideoView, localVideoTrack, false);
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(BaseSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + integer);

        setDuringCallActionBar();
        fillVideoView(integer, remoteFullScreenVideoView, qbrtcVideoTrack, true);
        updateVideoView(remoteFullScreenVideoView, false);
    }

    private void runUpdateUsersNames(final ArrayList<QBUser> newUsers) {
        mainHandler.postDelayed(() -> {
            for (QBUser user : newUsers) {
                Log.d(TAG, "runUpdateUsersNames. foreach, opponentUser = " + user.getFullName());
            }
        }, UPDATING_USERS_DELAY);
    }

    @Override
    public void updateOpponentInfo(User opponentUser) {
        UiUtils.displayProfileImage(getContext(), firstOpponentAvatarImageView, opponentUser);
        allOpponentsTextView.setText(opponentUser.nickName);
    }

    @Override
    public void playRingtone() {
        //ringtonePlayer = new RingtonePlayer(getContext(), R.raw.beep);
        //ringtonePlayer.play(true);
    }

    private void toggleControlsVisibility() {
        resetTimerHideControls();
        boolean isVisible = controlsLayout.getVisibility() == View.VISIBLE;
        Transition transition = new Slide(Gravity.BOTTOM);
        TransitionManager.beginDelayedTransition((ViewGroup) getView(), transition);
        controlsLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        if (!isVisible) {
            startTimerHideControls();
        }
    }

    private void resetTimerHideControls() {
        if (toggleControlsTimer != null) {
            toggleControlsTimer.cancel();
        }
    }

    private void startTimerHideControls() {
        if (controlsLayout.getVisibility() != View.VISIBLE) {
            return;
        }
        if (toggleControlsTimer != null) {
            toggleControlsTimer.cancel();
        }
        toggleControlsTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(() -> toggleControlsVisibility());
            }
        };
        toggleControlsTimer.schedule(task, 5000);
    }

    private void animateLocalViewToInitialPosition() {
        int width = localVideoView.getWidth();
        int height = localVideoView.getHeight();
        int finalHeight = ResourceUtils.dpToPx(150);
        ValueAnimator animator = ValueAnimator.ofInt(height, finalHeight);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) localVideoView.getLayoutParams();
        layoutParams.setMarginStart(ResourceUtils.dpToPx(20));
        layoutParams.topMargin = ResourceUtils.dpToPx(20);
        animator.addUpdateListener(animation -> {
            int currentHeight = (int) animation.getAnimatedValue();
            int currentWidth = (int) ((double) currentHeight / height * width);
            layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = currentHeight;
            localVideoView.setLayoutParams(layoutParams);
        });
        animator.setDuration(500);
        animator.start();
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    public VideoCallComponent getComponent() {
        if (component == null) {
            component = getComponent(CallComponent.class).provideVideoCallComponent(new VideoCallModule(this));
        }
        return component;
    }
}


