package com.ping.android.presentation.view.fragment;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.ping.android.utils.RingtonePlayer;
import com.ping.android.utils.UiUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private View view;
    private QBRTCSurfaceView remoteFullScreenVideoView;
    private QBRTCSurfaceView localVideoView;
    private CameraState cameraState = CameraState.DISABLED_FROM_USER;
//    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private QBRTCVideoTrack localVideoTrack;

    private Map<Integer, QBRTCVideoTrack> videoTrackMap;
    //    private OpponentsFromCallAdapter opponentsAdapter;
    private LocalViewOnClickListener localViewOnClickListener;
    private boolean isRemoteShown;

    private int amountOpponents;
    private int userIDFullScreen;
//    private List<QBUser> allOpponents;
    private boolean connectionEstablished;
    private boolean allCallbacksInit;
    private boolean isCurrentCameraFront;
    private boolean isLocalVideoFullScreen;

    //private User opponentUser;
//    private UserRepository userRepository;

    private ImageView firstOpponentAvatarImageView;

    @Inject
    VideoCallPresenter presenter;
    VideoCallComponent component;
    private RingtonePlayer ringtonePlayer;
    private LinearLayout outgoingViewContainer;

    public static VideoConversationFragment newInstance(User opponentUser) {
        VideoConversationFragment fragment = new VideoConversationFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("opponentUser", opponentUser);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);
        outgoingViewContainer = view.findViewById(R.id.layout_background_outgoing_screen);
        presenter.create();
        //localVideoView.setOnTouchListener(new MyTouchListener());
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
    protected void initFields() {
        super.initFields();
//        userRepository = new UserRepository();
        localViewOnClickListener = new LocalViewOnClickListener();
        //amountOpponents = opponents.size();
//        allOpponents = Collections.synchronizedList(new ArrayList<QBUser>(opponents.size()));
//        allOpponents.addAll(opponents);

        //isPeerToPeerCall = opponents.size() == 1;
    }

    @Override
    protected void hideOutgoingScreen() {
        super.hideOutgoingScreen();
        outgoingViewContainer.setVisibility(View.GONE);
    }

    @Override
    public void onCallStarted() {
        super.onCallStarted();
        connectionEstablished = true;
        animateLocalViewToInitialPosition();
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }
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
            layoutParams.width = currentWidth;
            layoutParams.height = currentHeight;
            localVideoView.setLayoutParams(layoutParams);
        });
        animator.setDuration(500);
        animator.start();
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 150);
//        localVideoView.setLayoutParams(params);
    }

    public void setDuringCallActionBar() {
        actionButtonsEnabled(true);
    }

//    private void initVideoTrackSListener() {
//        if (currentSession != null) {
//            currentSession.addVideoTrackCallbacksListener(this);
//        }
//    }
//
//    private void removeVideoTrackSListener() {
//        if (currentSession != null) {
//            currentSession.removeVideoTrackCallbacksListener(this);
//        }
//    }

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
//            conversationFragmentCallbackListener.addTCClientConnectionCallback(this);
//            conversationFragmentCallbackListener.addRTCSessionEventsCallback(this);
//            initVideoTrackSListener();
            allCallbacksInit = true;
        }
    }

    @Override
    protected void hangup() {
        presenter.hangup();
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }
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
        Log.i(TAG, "initViews");
//        opponentViewHolders = new SparseArray<>(opponents.size());
        isRemoteShown = false;
        isCurrentCameraFront = true;
        localVideoView = view.findViewById(R.id.local_video_view);
        //initCorrectSizeForLocalView(view);
        localVideoView.setZOrderMediaOverlay(true);

        DragFrameLayout dragLayout = view.findViewById(R.id.drag_layout);
        dragLayout.setDragFrameController(captured -> {
        });

        dragLayout.addDragView(localVideoView);

        remoteFullScreenVideoView = view.findViewById(R.id.remote_video_view);
        remoteFullScreenVideoView.setOnClickListener(localViewOnClickListener);

        cameraToggle = view.findViewById(R.id.toggle_camera);
        cameraToggle.setVisibility(View.VISIBLE);

        timerChronometer = view.findViewById(R.id.chronometer_timer_call);

        firstOpponentAvatarImageView = view.findViewById(R.id.image_caller_avatar);

        actionButtonsEnabled(false);
        //restoreSession();
    }

//    private void restoreSession() {
//        Log.d(TAG, "restoreSession ");
//        if (currentSession.getState() != QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_ACTIVE) {
//            return;
//        }
//        Map<Integer, QBRTCVideoTrack> videoTrackMap = getVideoTrackMap();
//        if (!videoTrackMap.isEmpty()) {
//            onCallStarted();
//            for (final Iterator<Map.Entry<Integer, QBRTCVideoTrack>> entryIterator
//                 = videoTrackMap.entrySet().iterator(); entryIterator.hasNext(); ) {
//                final Map.Entry<Integer, QBRTCVideoTrack> entry = entryIterator.next();
//                Log.d(TAG, "check ability to restoreSession for opponentUser:" + entry.getKey());
//                //if connection with peer wasn't closed do restore it otherwise remove from collection
//                if (currentSession.getPeerChannel(entry.getKey()).getState() !=
//                        QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED) {
//                    Log.d(TAG, "execute restoreSession for opponentUser:" + entry.getKey());
//                    mainHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            //onConnectedToUser(currentSession, entry.getKey());
//                            onRemoteVideoTrackReceive(currentSession, entry.getValue(), entry.getKey());
//                        }
//                    }, LOCAL_TRACk_INITIALIZE_DELAY);
//                } else {
//                    entryIterator.remove();
//                }
//            }
//        }
//    }

    private void initCorrectSizeForLocalView(View rootView) {
        ViewGroup.LayoutParams params = localVideoView.getLayoutParams();
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();

        int screenWidthPx = displaymetrics.widthPixels;
        Log.d(TAG, "screenWidthPx " + screenWidthPx);
        params.width = (int) (screenWidthPx * 0.3);
        params.height = (params.width / 2) * 3;
        TransitionManager.beginDelayedTransition((ViewGroup) rootView);
        localVideoView.setLayoutParams(params);
    }

//    private void setGrid(int columnsCount) {
//        int gridWidth = view.getMeasuredWidth();
//        Log.i(TAG, "onGlobalLayout : gridWidth=" + gridWidth + " columnsCount= " + columnsCount);
//        float itemMargin = getResources().getDimension(R.dimen.grid_item_divider);
//        int cellSizeWidth = defineSize(gridWidth, columnsCount, itemMargin);
//        Log.i(TAG, "onGlobalLayout : cellSize=" + cellSizeWidth);
//        opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), currentSession, opponents, cellSizeWidth,
//                (int) getResources().getDimension(R.dimen.item_height));
//        opponentsAdapter.setAdapterListener(VideoConversationFragment.this);
//    }

    private Map<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        if (videoTrackMap == null) {
            videoTrackMap = new HashMap<>();
        }
        return videoTrackMap;
    }

    private int defineSize(int measuredWidth, int columnsCount, float padding) {
        return measuredWidth / columnsCount - (int) (padding * 2) - RECYCLE_VIEW_PADDING;
    }

//    private int defineColumnsCount() {
//        return opponents.size() - 1;
//    }


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

        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
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
//        releaseViewHolders();
//        removeConnectionStateListeners();
//        removeVideoTrackSListener();
        removeVideoTrackRenderers();
        releaseViews();
    }

//    private void releaseViewHolders() {
//        opponentViewHolders.clear();
//    }

//    private void removeConnectionStateListeners() {
//        conversationFragmentCallbackListener.removeRTCClientConnectionCallback(this);
//        conversationFragmentCallbackListener.removeRTCSessionEventsCallback(this);
//    }

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
        presenter.toggleAudio(isEnable);
    }

    private void switchCamera() {
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
        updateVideoView(isLocalVideoFullScreen ? remoteFullScreenVideoView : localVideoView, isCurrentCameraFront);
        toggleCamera(true);
    }

    private void runOnUiThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        presenter.toggleVideoLocal(isNeedEnableCam);
//        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
//
////            conversationFragmentCallbackListener.onSetVideoEnabled(isNeedEnableCam);
//        }
        if (connectionEstablished && !cameraToggle.isEnabled()) {
            cameraToggle.setEnabled(true);
        }
    }

    ////////////////////////////  callbacks from QBRTCClientVideoTracksCallbacks ///////////////////
    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");
        localVideoTrack = videoTrack;
        isLocalVideoFullScreen = true;
        cameraState = CameraState.NONE;


        if (localVideoView != null) {
            fillVideoView(localVideoView, localVideoTrack, false);
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack, final Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);

//        if (localVideoTrack != null) {
//            fillVideoView(localVideoView, localVideoTrack, false);
//        }
        isLocalVideoFullScreen = false;

        setDuringCallActionBar();
        fillVideoView(userID, remoteFullScreenVideoView, videoTrack, true);
        updateVideoView(remoteFullScreenVideoView, false);
    }
    /////////////////////////////////////////    end    ////////////////////////////////////////////

    //last opponent view is bind
//    @Override
//    public void OnBindLastViewHolder(final OpponentsFromCallAdapter.ViewHolder holder, final int position) {
//        Log.i(TAG, "OnBindLastViewHolder position=" + position);
//
//    }


//    @Override
//    public void onItemClick(int position) {
//        //int userId = opponentsAdapter.getItem(position);
//        Log.d(TAG, "USer onItemClick= " + userId);
//        if (!getVideoTrackMap().containsKey(userId) ||
//                currentSession.getPeerChannel(userId).getState().ordinal() == QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal()) {
//            return;
//        }
//
//        replaceUsersInAdapter(position);
//
//        updateViewHolders(position);
//
//        swapUsersFullscreenToPreview(userId);
//    }

//    private void replaceUsersInAdapter(int position) {
//        for (QBUser qbUser : allOpponents) {
//            if (qbUser.getId() == userIDFullScreen) {
//                opponentsAdapter.replaceUsers(position, qbUser);
//                break;
//            }
//        }
//    }

//    private void updateViewHolders(int position) {
//    }

    @SuppressWarnings("ConstantConditions")
//    private void swapUsersFullscreenToPreview(int userId) {
////      get opponentVideoTrack - opponent's video track from recyclerView
//        QBRTCVideoTrack opponentVideoTrack = getVideoTrackMap().get(userId);
//
////      get mainVideoTrack - opponent's video track from full screen
//        QBRTCVideoTrack mainVideoTrack = getVideoTrackMap().get(userIDFullScreen);
//
//        QBRTCSurfaceView remoteVideoView = findHolder(userId).getOpponentView();
//
//        if (mainVideoTrack != null) {
//            fillVideoView(0, remoteVideoView, mainVideoTrack);
//            Log.d(TAG, "_remoteVideoView enabled");
//        }
//        if (opponentVideoTrack != null) {
//            fillVideoView(userId, remoteFullScreenVideoView, opponentVideoTrack);
//            Log.d(TAG, "fullscreen enabled");
//        }
//    }


//    private void setRemoteViewMultiCall(int userID, QBRTCVideoTrack videoTrack) {
//        Log.d(TAG, "setRemoteViewMultiCall fillVideoView");
//        final OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userID);
//        if (itemHolder == null) {
//            Log.d(TAG, "itemHolder == null - true");
//            return;
//        }
//        final QBRTCSurfaceView remoteVideoView = itemHolder.getOpponentView();
//
//        if (remoteVideoView != null) {
//            remoteVideoView.setZOrderMediaOverlay(true);
//            updateVideoView(remoteVideoView, false);
//
//            Log.d(TAG, "onRemoteVideoTrackReceive fillVideoView");
//            if (isRemoteShown) {
//                Log.d(TAG, "USer onRemoteVideoTrackReceive = " + userID);
//                fillVideoView(userID, remoteVideoView, videoTrack, true);
//            } else {
//                isRemoteShown = true;
//                //opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
//                setDuringCallActionBar();
////                setRecyclerViewVisibleState();
//                //setOpponentsVisibility(View.VISIBLE);
//                fillVideoView(userID, remoteFullScreenVideoView, videoTrack);
//                updateVideoView(remoteFullScreenVideoView, false);
//            }
//        }
//    }

//    private void setRecyclerViewVisibleState() {
//    }

//    private OpponentsFromCallAdapter.ViewHolder getViewHolderForOpponent(Integer userID) {
//        OpponentsFromCallAdapter.ViewHolder holder = opponentViewHolders.get(userID);
//        if (holder == null) {
//            Log.d(TAG, "holder not found in cache");
//            holder = findHolder(userID);
//            if (holder != null) {
//                opponentViewHolders.append(userID, holder);
//            }
//        }
//        return holder;
//    }

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

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private void fillVideoView(int userId, QBRTCSurfaceView videoView, QBRTCVideoTrack videoTrack) {
        if (userId != 0) {
            userIDFullScreen = userId;
        }
        fillVideoView(userId, videoView, videoTrack, true);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror) {
        updateVideoView(surfaceViewRenderer, mirror, RendererCommon.ScalingType.SCALE_ASPECT_FILL);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror, RendererCommon.ScalingType scalingType) {
        Log.i(TAG, "updateVideoView mirror:" + mirror + ", scalingType = " + scalingType);
        surfaceViewRenderer.setScalingType(scalingType);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }

//    private void setStatusForOpponent(int userId, final String status) {
//        if (isPeerToPeerCall) {
//            return;
//        }
//
//        final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
//        if (holder == null) {
//            return;
//        }
//
//        holder.setStatus(status);
//    }

//    private void updateNameForOpponent(int userId, String newUserName) {
//        if (isPeerToPeerCall) {
//            //actionBar.setSubtitle(getString(R.string.opponent, newUserName));
//        } else {
//            OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
//            if (holder == null) {
//                Log.d("UPDATE_USERS", "holder == null");
//                return;
//            }
//
//            Log.d("UPDATE_USERS", "holder != null");
//            holder.setUserName(newUserName);
//        }
//    }

//    private void setProgressBarForOpponentGone(int userId) {
//        if (isPeerToPeerCall) {
//            return;
//        }
//        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
//        if (holder == null) {
//            return;
//        }
//
//        holder.getProgressBar().setVisibility(View.GONE);
//
//    }
//
//    private void setBackgroundOpponentView(final Integer userId) {
//        final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
//        if (holder == null) {
//            return;
//        }
//
//        if (userId != userIDFullScreen) {
//            holder.getOpponentView().setBackgroundColor(Color.parseColor("#000000"));
//        }
//    }

    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

//    @Override
//    public void onConnectedToUser(QBRTCSession qbrtcSession, final Integer userId) {
//        connectionEstablished = true;
//        setStatusForOpponent(userId, getString(R.string.text_status_connected));
//        setProgressBarForOpponentGone(userId);
//        allOpponentsTextView.setVisibility(View.INVISIBLE);
//        firstOpponentAvatarImageView.setVisibility(View.INVISIBLE);
//    }

//    @Override
//    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer userId) {
//        setStatusForOpponent(userId, getString(R.string.text_status_closed));
//        if (!isPeerToPeerCall) {
//            Log.d(TAG, "onConnectionClosedForUser videoTrackMap.remove(userId)= " + userId);
//            getVideoTrackMap().remove(userId);
//            setBackgroundOpponentView(userId);
//        }
//    }

//    @Override
//    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
//        setStatusForOpponent(integer, getString(R.string.text_status_disconnected));
//    }

    //////////////////////////////////   end     //////////////////////////////////////////


    /////////////////// Callbacks from CallActivity.QBRTCSessionUserCallback //////////////////////
//    @Override
//    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
//        setProgressBarForOpponentGone(userId);
//        setStatusForOpponent(userId, getString(R.string.text_status_no_answer));
//    }

//    @Override
//    public void onCallRejectByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
//        setStatusForOpponent(userId, getString(R.string.text_status_rejected));
//    }
//
//    @Override
//    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
//        setStatusForOpponent(userId, getString(R.string.accepted));
//    }

//    @Override
//    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
//        setStatusForOpponent(userId, getString(R.string.text_status_hang_up));
//        Log.d(TAG, "onReceiveHangUpFromUser userId= " + userId);
//        if (!isPeerToPeerCall) {
//            if (userId == userIDFullScreen) {
//                Log.d(TAG, "setAnotherUserToFullScreen call userId= " + userId);
//                setAnotherUserToFullScreen();
//            }
//        }
//    }

//    @Override
//    public void onSessionClosed(QBRTCSession session) {
//
//    }

    //////////////////////////////////   end     //////////////////////////////////////////

//    private void setAnotherUserToFullScreen() {
//        if (opponentsAdapter.getOpponents().isEmpty()) {
//            return;
//        }
//        int userId = opponentsAdapter.getItem(0);
////      get opponentVideoTrack - opponent's video track from recyclerView
//        QBRTCVideoTrack opponentVideoTrack = getVideoTrackMap().get(userId);
//        if (opponentVideoTrack == null) {
//            Log.d(TAG, "setAnotherUserToFullScreen opponentVideoTrack == null");
//            return;
//        }
//
//        fillVideoView(userId, remoteFullScreenVideoView, opponentVideoTrack);
//        Log.d(TAG, "fullscreen enabled");
//
//        OpponentsFromCallAdapter.ViewHolder itemHolder = findHolder(userId);
//        if (itemHolder != null) {
//            opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
//            itemHolder.getOpponentView().release();
//            Log.d(TAG, "onConnectionClosedForUser opponentsAdapter.removeItem= " + userId);
//        }
//    }

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
//        updateAllOpponentsList(newUsers);
        Log.d(TAG, "updateOpponentsList(), newUsers = " + newUsers);
        runUpdateUsersNames(newUsers);
    }

//    private void updateAllOpponentsList(ArrayList<QBUser> newUsers) {
//
//        for (int i = 0; i < allOpponents.size(); i++) {
//            for (QBUser updatedUser : newUsers) {
//                if (updatedUser.equals(allOpponents.get(i))) {
//                    allOpponents.set(i, updatedUser);
//                }
//            }
//        }
//    }

    private void runUpdateUsersNames(final ArrayList<QBUser> newUsers) {
        //need delayed for synchronization with recycler view initialization
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (QBUser user : newUsers) {
                    Log.d(TAG, "runUpdateUsersNames. foreach, opponentUser = " + user.getFullName());
                    //updateNameForOpponent(opponentUser.getId(), opponentUser.getFullName());
                }
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
        ringtonePlayer = new RingtonePlayer(getContext(), R.raw.beep);
        //ringtonePlayer.play(true);
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }


//    class DividerItemDecoration extends RecyclerView.ItemDecoration {
//
//        private int space;
//
//        public DividerItemDecoration(@NonNull Context context, @DimenRes int dimensionDivider) {
//            this.space = context.getResources().getDimensionPixelSize(dimensionDivider);
//        }
//
//        @Override
//        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//            outRect.set(space, space, space, space);
//        }
//    }

    class LocalViewOnClickListener implements View.OnClickListener {
        private long lastFullScreenClickTime = 0L;

        @Override
        public void onClick(View v) {
            if ((SystemClock.uptimeMillis() - lastFullScreenClickTime) < FULL_SCREEN_CLICK_DELAY) {
                return;
            }
            lastFullScreenClickTime = SystemClock.uptimeMillis();

            if (connectionEstablished) {
                setFullScreenOnOff();
            }
        }

        private void setFullScreenOnOff() {
//            if (actionBar.isShowing()) {
//                hideToolBarAndButtons();
//            } else {
//                showToolBarAndButtons();
//            }
        }

//        private void hideToolBarAndButtons() {
//            //actionBar.hide();
//
//            localVideoView.setVisibility(View.INVISIBLE);
//
//            if (!isPeerToPeerCall) {
//                shiftBottomListOpponents();
//            }
//        }

//        private void showToolBarAndButtons() {
//            //actionBar.show();
//
//            localVideoView.setVisibility(View.VISIBLE);

//            if (!isPeerToPeerCall) {
//                shiftMarginListOpponents();
//            }
//        }

        private void shiftBottomListOpponents() {
        }

        private void shiftMarginListOpponents() {
        }
    }

    public VideoCallComponent getComponent() {
        if (component == null) {
            component = getComponent(CallComponent.class).provideVideoCallComponent(new VideoCallModule(this));
        }
        return component;
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(null, shadowBuilder, view, 0);
                //view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }

//    class MyDragListener implements View.OnDragListener {
//        Drawable enterShape = getResources().getDrawable(
//                R.drawable.shape_droptarget);
//        Drawable normalShape = getResources().getDrawable(R.drawable.shape);
//
//        @Override
//        public boolean onDrag(View v, DragEvent event) {
//            int action = event.getAction();
//            switch (event.getAction()) {
//                case DragEvent.ACTION_DRAG_STARTED:
//                    // do nothing
//                    break;
//                case DragEvent.ACTION_DRAG_ENTERED:
//                    v.setBackgroundDrawable(enterShape);
//                    break;
//                case DragEvent.ACTION_DRAG_EXITED:
//                    v.setBackgroundDrawable(normalShape);
//                    break;
//                case DragEvent.ACTION_DROP:
//                    // Dropped, reassign View to ViewGroup
//                    View view = (View) event.getLocalState();
//                    ViewGroup owner = (ViewGroup) view.getParent();
//                    owner.removeView(view);
//                    LinearLayout container = (LinearLayout) v;
//                    container.addView(view);
//                    view.setVisibility(View.VISIBLE);
//                    break;
//                case DragEvent.ACTION_DRAG_ENDED:
//                    v.setBackgroundDrawable(normalShape);
//                default:
//                    break;
//            }
//            return true;
//        }
//    }
}


