package com.ping.android.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class PuzzleActivity extends CoreActivity implements View.OnClickListener {

    private FirebaseStorage storage;

    private ImageView btBack;
    private ToggleButton btPuzzle;
    private ImageView ivPuzzle;

    private String conversationID, messageID;
    private String imageURL, imageLocalName, imageLocalPath, imageLocalFolder;
    private Boolean puzzledstatus;

    private Bitmap originalBitmap;
    private Bitmap puzzledBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        conversationID = getIntent().getStringExtra("CONVERSATION_ID");
        messageID = getIntent().getStringExtra("MESSAGE_ID");
        imageURL = getIntent().getStringExtra("IMAGE_URL");
        puzzledstatus = getIntent().getBooleanExtra("PUZZLE_STATUS", true);
        bindViews();
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (StringUtils.isNotEmpty(conversationID) && StringUtils.isNotEmpty(messageID)) {
            ServiceManager.getInstance().updateMarkStatus(conversationID, messageID, !btPuzzle.isChecked());
        }
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.puzzle_back);
        btBack.setOnClickListener(this);
        btPuzzle = (ToggleButton) findViewById(R.id.puzzle_toggle);
        btPuzzle.setOnClickListener(this);
        ivPuzzle = (ImageView) findViewById(R.id.puzzle_image);
    }

    private void init() {
        storage = FirebaseStorage.getInstance();
        imageLocalPath = this.getExternalFilesDir(null).getAbsolutePath();
        imageLocalName = CommonMethod.getFileNameFromFirebase(imageURL);
        imageLocalPath = imageLocalPath + File.separator + imageLocalName;
        File imageLocal = new File(imageLocalPath);
        imageLocalName = imageLocal.getName();
        imageLocalFolder = imageLocal.getParent();
        CommonMethod.createFolder(imageLocalFolder);

        if (imageLocal.exists()) {
            originalBitmap = BitmapFactory.decodeFile(imageLocalPath);
            puzzledBitmap = CommonMethod.puzzleImage(originalBitmap, 3);
            btPuzzle.setChecked(!puzzledstatus);
            if (puzzledstatus) {
                ivPuzzle.setImageBitmap(puzzledBitmap);
            } else {
                ivPuzzle.setImageBitmap(originalBitmap);
            }
        } else {
            StorageReference imageReference = storage.getReferenceFromUrl(imageURL);
            imageReference.getFile(imageLocal).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    originalBitmap = BitmapFactory.decodeFile(imageLocalPath);
                    puzzledBitmap = CommonMethod.puzzleImage(originalBitmap, 3);
                    btPuzzle.setChecked(!puzzledstatus);
                    if (puzzledstatus) {
                        ivPuzzle.setImageBitmap(puzzledBitmap);
                    } else {
                        ivPuzzle.setImageBitmap(originalBitmap);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.puzzle_back:
                exit();
                break;
            case R.id.puzzle_toggle:
                puzzleImage();
                break;
        }
    }

    public void exit() {
        finish();
    }

    public void puzzleImage() {
        if (btPuzzle.isChecked()) {
            ivPuzzle.setImageBitmap(originalBitmap);
        } else {
            ivPuzzle.setImageBitmap(puzzledBitmap);
        }
        if (StringUtils.isNotEmpty(conversationID) && StringUtils.isNotEmpty(messageID)) {
            ServiceManager.getInstance().updateMarkStatus(conversationID, messageID, !btPuzzle.isChecked());
        }
    }

}
