package com.ping.android.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.ping.android.activity.R;
import com.ping.android.adapter.OpponentsFromCallAdapter;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.utils.UiUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * QuickBlox team
 */
public class VideoConversationFragment extends BaseConversationFragment implements Serializable, QBRTCClientVideoTracksCallbacks,
        QBRTCSessionStateCallback, QBRTCSessionEventsCallback, OpponentsFromCallAdapter.OnAdapterEventListener {

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
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private boolean isPeerToPeerCall;
    private QBRTCVideoTrack localVideoTrack;

    private Map<Integer, QBRTCVideoTrack> videoTrackMap;
    private OpponentsFromCallAdapter opponentsAdapter;
    private LocalViewOnClickListener localViewOnClickListener;
    private boolean isRemoteShown;

    private int amountOpponents;
    private int userIDFullScreen;
    private List<QBUser> allOpponents;
    private boolean connectionEstablished;
    private boolean allCallbacksInit;
    private boolean isCurrentCameraFront;
    private boolean isLocalVideoFullScreen;

    private User opponentUser;
    private UserRepository userRepository;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);
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
        userRepository = new UserRepository();
        localViewOnClickListener = new LocalViewOnClickListener();
        amountOpponents = opponents.size();
        allOpponents = Collections.synchronizedList(new ArrayList<QBUser>(opponents.size()));
        allOpponents.addAll(opponents);

        isPeerToPeerCall = opponents.size() == 1;
    }

    public void setDuringCallActionBar() {
        actionButtonsEnabled(true);
    }

    private void initVideoTrackSListener() {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(this);
        }
    }

    private void removeVideoTrackSListener() {
        if (currentSession != null) {
            currentSession.removeVideoTrackCallbacksListener(this);
        }
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
            conversationFragmentCallbackListener.addTCClientConnectionCallback(this);
            conversationFragmentCallbackListener.addRTCSessionEventsCallback(this);
            initVideoTrackSListener();
            allCallbacksInit = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setHasOptionsMenu(true);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        Log.i(TAG, "initViews");
        opponentViewHolders = new SparseArray<>(opponents.size());
        isRemoteShown = false;
        isCurrentCameraFront = true;
        localVideoView = (QBRTCSurfaceView) view.findViewById(R.id.local_video_view);
        initCorrectSizeForLocalView();
        localVideoView.setZOrderMediaOverlay(true);

        remoteFullScreenVideoView = (QBRTCSurfaceView) view.findViewById(R.id.remote_video_view);
        remoteFullScreenVideoView.setOnClickListener(localViewOnClickListener);

        cameraToggle = (ToggleButton) view.findViewById(R.id.toggle_camera);
        cameraToggle.setVisibility(View.VISIBLE);

        timerChronometer = (Chronometer) view.findViewById(R.id.chronometer_timer_call);

        ImageView firstOpponentAvatarImageView = (ImageView) view.findViewById(R.id.image_caller_avatar);

        userRepository.getUserByQbId(opponents.get(0).getId(), (error, data) -> {
            if (error != null) return;
            opponentUser = (User) data[0];
            UiUtils.displayProfileImage(getContext(), firstOpponentAvatarImageView, opponentUser);

            allOpponentsTextView.setText(opponentUser.getDisplayName());
        });

        actionButtonsEnabled(false);
        restoreSession();
    }

    private void restoreSession() {
        Log.d(TAG, "restoreSession ");
        if (currentSession.getState() != QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_ACTIVE) {
            return;
        }
        onCallStarted();
        Map<Integer, QBRTCVideoTrack> videoTrackMap = getVideoTrackMap();
        if (!videoTrackMap.isEmpty()) {
            for (final Iterator<Map.Entry<Integer, QBRTCVideoTrack>> entryIterator
                 = videoTrackMap.entrySet().iterator(); entryIterator.hasNext(); ) {
                final Map.Entry<Integer, QBRTCVideoTrack> entry = entryIterator.next();
                Log.d(TAG, "check ability to restoreSession for user:" + entry.getKey());
                //if connection with peer wasn't closed do restore it otherwise remove from collection
                if (currentSession.getPeerChannel(entry.getKey()).getState() !=
                        QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED) {
                    Log.d(TAG, "execute restoreSession for user:" + entry.getKey());
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onConnectedToUser(currentSession, entry.getKey());
                            onRemoteVideoTrackReceive(currentSession, entry.getValue(), entry.getKey());
                        }
                    }, LOCAL_TRACk_INITIALIZE_DELAY);
                } else {
                    entryIterator.remove();
                }
            }
        }
    }

    private void initCorrectSizeForLocalView() {
        ViewGroup.LayoutParams params = localVideoView.getLayoutParams();
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();

        int screenWidthPx = displaymetrics.widthPixels;
        Log.d(TAG, "screenWidthPx " + screenWidthPx);
        params.width = (int) (screenWidthPx * 0.3);
        params.height = (params.width / 2) * 3;
        localVideoView.setLayoutParams(params);
    }

    private void setGrid(int columnsCount) {
        int gridWidth = view.getMeasuredWidth();
        Log.i(TAG, "onGlobalLayout : gridWidth=" + gridWidth + " columnsCount= " + columnsCount);
        float itemMargin = getResources().getDimension(R.dimen.grid_item_divider);
        int cellSizeWidth = defineSize(gridWidth, columnsCount, itemMargin);
        Log.i(TAG, "onGlobalLayout : cellSize=" + cellSizeWidth);
        opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), currentSession, opponents, cellSizeWidth,
                (int) getResources().getDimension(R.dimen.item_height));
        opponentsAdapter.setAdapterListener(VideoConversationFragment.this);
    }

    private Map<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        if (videoTrackMap == null) {
            videoTrackMap = new HashMap<>();
        }
        return videoTrackMap;
    }

    private int defineSize(int measuredWidth, int columnsCount, float padding) {
        return measuredWidth / columnsCount - (int) (padding * 2) - RECYCLE_VIEW_PADDING;
    }

    private int defineColumnsCount() {
        return opponents.size() - 1;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(true);
        }
    }


    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(false);
        }

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
        releaseViewHolders();
        removeConnectionStateListeners();
        removeVideoTrackSListener();
        removeVideoTrackRenderers();
        releaseViews();
    }

    private void releaseViewHolders() {
        opponentViewHolders.clear();
    }

    private void removeConnectionStateListeners() {
        conversationFragmentCallbackListener.removeRTCClientConnectionCallback(this);
        conversationFragmentCallbackListener.removeRTCSessionEventsCallback(this);
    }

    private void releaseViews() {
        if (localVideoView != null) {
            localVideoView.release();
        }
        if (remoteFullScreenVideoView != null) {
            remoteFullScreenVideoView.release();
        }
        remoteFullScreenVideoView = null;
        if (!isPeerToPeerCall) {
            releseOpponentsViews();
        }
    }

    @Override
    public void onCallStopped() {
        super.onCallStopped();
        Log.i(TAG, "onCallStopped");
    }

    protected void initButtonsListener() {
        super.initButtonsListener();

        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cameraState != CameraState.DISABLED_FROM_USER) {
                    switchCamera();
                }
            }
        });
    }

    private void switchCamera() {
        if (cameraState == CameraState.DISABLED_FROM_USER) {
            return;
        }
        cameraToggle.setEnabled(false);
        conversationFragmentCallbackListener.onSwitchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
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
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            conversationFragmentCallbackListener.onSetVideoEnabled(isNeedEnableCam);
        }
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


        if (remoteFullScreenVideoView != null) {
            fillVideoView(remoteFullScreenVideoView, localVideoTrack, false);
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack, final Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);

        if (localVideoTrack != null) {
            fillVideoView(localVideoView, localVideoTrack, false);
        }
        isLocalVideoFullScreen = false;

        if (isPeerToPeerCall) {
            setDuringCallActionBar();
            fillVideoView(userID, remoteFullScreenVideoView, videoTrack, true);
            updateVideoView(remoteFullScreenVideoView, false);
        } else {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRemoteViewMultiCall(userID, videoTrack);
                }
            }, LOCAL_TRACk_INITIALIZE_DELAY);
        }
    }
    /////////////////////////////////////////    end    ////////////////////////////////////////////

    //last opponent view is bind
    @Override
    public void OnBindLastViewHolder(final OpponentsFromCallAdapter.ViewHolder holder, final int position) {
        Log.i(TAG, "OnBindLastViewHolder position=" + position);

    }


    @Override
    public void onItemClick(int position) {
        int userId = opponentsAdapter.getItem(position);
        Log.d(TAG, "USer onItemClick= " + userId);
        if (!getVideoTrackMap().containsKey(userId) ||
                currentSession.getPeerChannel(userId).getState().ordinal() == QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal()) {
            return;
        }

        replaceUsersInAdapter(position);

        updateViewHolders(position);

        swapUsersFullscreenToPreview(userId);
    }

    private void replaceUsersInAdapter(int position) {
        for (QBUser qbUser : allOpponents) {
            if (qbUser.getId() == userIDFullScreen) {
                opponentsAdapter.replaceUsers(position, qbUser);
                break;
            }
        }
    }

    private void updateViewHolders(int position) {
    }

    @SuppressWarnings("ConstantConditions")
    private void swapUsersFullscreenToPreview(int userId) {
//      get opponentVideoTrack - opponent's video track from recyclerView
        QBRTCVideoTrack opponentVideoTrack = getVideoTrackMap().get(userId);

//      get mainVideoTrack - opponent's video track from full screen
        QBRTCVideoTrack mainVideoTrack = getVideoTrackMap().get(userIDFullScreen);

        QBRTCSurfaceView remoteVideoView = findHolder(userId).getOpponentView();

        if (mainVideoTrack != null) {
            fillVideoView(0, remoteVideoView, mainVideoTrack);
            Log.d(TAG, "_remoteVideoView enabled");
        }
        if (opponentVideoTrack != null) {
            fillVideoView(userId, remoteFullScreenVideoView, opponentVideoTrack);
            Log.d(TAG, "fullscreen enabled");
        }
    }


    private void setRemoteViewMultiCall(int userID, QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "setRemoteViewMultiCall fillVideoView");
        final OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userID);
        if (itemHolder == null) {
            Log.d(TAG, "itemHolder == null - true");
            return;
        }
        final QBRTCSurfaceView remoteVideoView = itemHolder.getOpponentView();

        if (remoteVideoView != null) {
            remoteVideoView.setZOrderMediaOverlay(true);
            updateVideoView(remoteVideoView, false);

            Log.d(TAG, "onRemoteVideoTrackReceive fillVideoView");
            if (isRemoteShown) {
                Log.d(TAG, "USer onRemoteVideoTrackReceive = " + userID);
                fillVideoView(userID, remoteVideoView, videoTrack, true);
            } else {
                isRemoteShown = true;
                opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
                setDuringCallActionBar();
                setRecyclerViewVisibleState();
                //setOpponentsVisibility(View.VISIBLE);
                fillVideoView(userID, remoteFullScreenVideoView, videoTrack);
                updateVideoView(remoteFullScreenVideoView, false);
            }
        }
    }

    private void setRecyclerViewVisibleState() {
    }

    private OpponentsFromCallAdapter.ViewHolder getViewHolderForOpponent(Integer userID) {
        OpponentsFromCallAdapter.ViewHolder holder = opponentViewHolders.get(userID);
        if (holder == null) {
            Log.d(TAG, "holder not found in cache");
            holder = findHolder(userID);
            if (holder != null) {
                opponentViewHolders.append(userID, holder);
            }
        }
        return holder;
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        return null;
    }


    private void releseOpponentsViews() {
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

    private void setStatusForOpponent(int userId, final String status) {
        if (isPeerToPeerCall) {
            return;
        }

        final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
        if (holder == null) {
            return;
        }

        holder.setStatus(status);
    }

    private void updateNameForOpponent(int userId, String newUserName) {
        if (isPeerToPeerCall) {
            //actionBar.setSubtitle(getString(R.string.opponent, newUserName));
        } else {
            OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
            if (holder == null) {
                Log.d("UPDATE_USERS", "holder == null");
                return;
            }

            Log.d("UPDATE_USERS", "holder != null");
            holder.setUserName(newUserName);
        }
    }

    private void setProgressBarForOpponentGone(int userId) {
        if (isPeerToPeerCall) {
            return;
        }
        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null) {
            return;
        }

        holder.getProgressBar().setVisibility(View.GONE);

    }

    private void setBackgroundOpponentView(final Integer userId) {
        final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
        if (holder == null) {
            return;
        }

        if (userId != userIDFullScreen) {
            holder.getOpponentView().setBackgroundColor(Color.parseColor("#000000"));
        }
    }

    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, final Integer userId) {
        connectionEstablished = true;
        setStatusForOpponent(userId, getString(R.string.text_status_connected));
        setProgressBarForOpponentGone(userId);
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.text_status_closed));
        if (!isPeerToPeerCall) {
            Log.d(TAG, "onConnectionClosedForUser videoTrackMap.remove(userId)= " + userId);
            getVideoTrackMap().remove(userId);
            setBackgroundOpponentView(userId);
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.text_status_disconnected));
    }

    //////////////////////////////////   end     //////////////////////////////////////////


    /////////////////// Callbacks from CallActivity.QBRTCSessionUserCallback //////////////////////
    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
        setProgressBarForOpponentGone(userId);
        setStatusForOpponent(userId, getString(R.string.text_status_no_answer));
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.text_status_rejected));
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.accepted));
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.text_status_hang_up));
        Log.d(TAG, "onReceiveHangUpFromUser userId= " + userId);
        if (!isPeerToPeerCall) {
            if (userId == userIDFullScreen) {
                Log.d(TAG, "setAnotherUserToFullScreen call userId= " + userId);
                setAnotherUserToFullScreen();
            }
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession session) {

    }

    //////////////////////////////////   end     //////////////////////////////////////////

    private void setAnotherUserToFullScreen() {
        if (opponentsAdapter.getOpponents().isEmpty()) {
            return;
        }
        int userId = opponentsAdapter.getItem(0);
//      get opponentVideoTrack - opponent's video track from recyclerView
        QBRTCVideoTrack opponentVideoTrack = getVideoTrackMap().get(userId);
        if (opponentVideoTrack == null) {
            Log.d(TAG, "setAnotherUserToFullScreen opponentVideoTrack == null");
            return;
        }

        fillVideoView(userId, remoteFullScreenVideoView, opponentVideoTrack);
        Log.d(TAG, "fullscreen enabled");

        OpponentsFromCallAdapter.ViewHolder itemHolder = findHolder(userId);
        if (itemHolder != null) {
            opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
            itemHolder.getOpponentView().release();
            Log.d(TAG, "onConnectionClosedForUser opponentsAdapter.removeItem= " + userId);
        }
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

    private void startScreenSharing() {
        conversationFragmentCallbackListener.onStartScreenSharing();
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        super.onOpponentsListUpdated(newUsers);
        updateAllOpponentsList(newUsers);
        Log.d(TAG, "updateOpponentsList(), newUsers = " + newUsers);
        runUpdateUsersNames(newUsers);
    }

    private void updateAllOpponentsList(ArrayList<QBUser> newUsers) {

        for (int i = 0; i < allOpponents.size(); i++) {
            for (QBUser updatedUser : newUsers) {
                if (updatedUser.equals(allOpponents.get(i))) {
                    allOpponents.set(i, updatedUser);
                }
            }
        }
    }

    private void runUpdateUsersNames(final ArrayList<QBUser> newUsers) {
        //need delayed for synchronization with recycler view initialization
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (QBUser user : newUsers) {
                    Log.d(TAG, "runUpdateUsersNames. foreach, user = " + user.getFullName());
                    updateNameForOpponent(user.getId(), user.getFullName());
                }
            }
        }, UPDATING_USERS_DELAY);
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }


    class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public DividerItemDecoration(@NonNull Context context, @DimenRes int dimensionDivider) {
            this.space = context.getResources().getDimensionPixelSize(dimensionDivider);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }
    }

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

        private void hideToolBarAndButtons() {
            //actionBar.hide();

            localVideoView.setVisibility(View.INVISIBLE);

            if (!isPeerToPeerCall) {
                shiftBottomListOpponents();
            }
        }

        private void showToolBarAndButtons() {
            //actionBar.show();

            localVideoView.setVisibility(View.VISIBLE);

            if (!isPeerToPeerCall) {
                shiftMarginListOpponents();
            }
        }

        private void shiftBottomListOpponents() {
        }

        private void shiftMarginListOpponents() {
        }
    }
}


