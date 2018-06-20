/*
 * *
 *  * Created by Salman Saleem on 6/21/18 12:43 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 6/21/18 12:38 AM
 *
 */

package com.tcv.vassistchat.fragments.bottomSheets;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tcv.vassistchat.R;
import com.tcv.vassistchat.activities.images.PickerBuilder;
import com.tcv.vassistchat.app.AppConstants;
import com.tcv.vassistchat.helpers.AppHelper;
import com.tcv.vassistchat.helpers.Files.FilesManager;
import com.tcv.vassistchat.helpers.PermissionHandler;
import com.tcv.vassistchat.presenters.groups.EditGroupPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 *
 *
 */

public class BottomSheetEditGroupImage extends BottomSheetDialogFragment {

    private View mView;
    @BindView(R.id.cameraBtn)
    FrameLayout cameraBtn;
    @BindView(R.id.galleryBtn)
    FrameLayout galleryBtn;
    private EditGroupPresenter mEditGroupPresenter;

    @Override
    public void onStart() {
        super.onStart();


    }

    private void setGalleryBtn() {
        if (PermissionHandler.checkPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
            if (AppHelper.isAndroid6()) {
                dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                getActivity().startActivityForResult(Intent.createChooser(intent, getActivity().getString(R.string.select_picture)), AppConstants.SELECT_PROFILE_PICTURE);
            } else {
                dismiss();
                new PickerBuilder(getActivity(), PickerBuilder.SELECT_FROM_GALLERY)
                        .setOnImageReceivedListener(imageUri -> {
                            Intent data = new Intent();
                            data.setData(imageUri);
                            ///   AppHelper.LogCat("new image SELECT_FROM_GALLERY" + imageUri);
                            mEditGroupPresenter.onActivityResult(AppConstants.SELECT_PROFILE_PICTURE, RESULT_OK, data);

                        })
                        .setImageName(getActivity().getString(R.string.app_name))
                        .setImageFolderName(getActivity().getString(R.string.app_name))
                        .setCropScreenColor(R.color.colorPrimary)
                        .withTimeStamp(false)
                        .setOnPermissionRefusedListener(() -> {
                            PermissionHandler.requestPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                        })
                        .start();
            }
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            PermissionHandler.requestPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    protected void sendToExternalApp() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Uri mProcessingPhotoUri;
            mProcessingPhotoUri = FilesManager.getImageFile(getActivity());
        if (mProcessingPhotoUri != null)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mProcessingPhotoUri);
        getActivity().startActivityForResult(cameraIntent, AppConstants.SELECT_PROFILE_CAMERA);
    }


    private void setCameraBtn() {
        if (PermissionHandler.checkPermission(getActivity(), Manifest.permission.CAMERA)) {
            AppHelper.LogCat("camera permission already granted.");
            if (AppHelper.isAndroid6()) {
                dismiss();
                sendToExternalApp();
            } else {
                dismiss();
                new PickerBuilder(getActivity(), PickerBuilder.SELECT_FROM_CAMERA)
                        .setOnImageReceivedListener(imageUri -> {
                            AppHelper.LogCat("new image SELECT_FROM_CAMERA " + imageUri);
                            Intent data = new Intent();
                            data.setData(imageUri);
                            mEditGroupPresenter.onActivityResult(AppConstants.SELECT_PROFILE_CAMERA, RESULT_OK, data);

                        })
                        .setImageName(getActivity().getString(R.string.app_name))
                        .setImageFolderName(getActivity().getString(R.string.app_name))
                        .setCropScreenColor(R.color.colorPrimary)
                        .withTimeStamp(false)
                        .setOnPermissionRefusedListener(() -> {
                            PermissionHandler.requestPermission(getActivity(), Manifest.permission.CAMERA);
                        })
                        .start();
            }
        } else {
            AppHelper.LogCat("Please request camera  permission.");
            PermissionHandler.requestPermission(getActivity(), Manifest.permission.CAMERA);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.content_bottom_sheet, container, false);
        ButterKnife.bind(this, mView);
        mEditGroupPresenter = new EditGroupPresenter(this);
        galleryBtn.setOnClickListener(v -> setGalleryBtn());
        cameraBtn.setOnClickListener(v -> setCameraBtn());
        return mView;
    }

    @Override
    public void onViewCreated(View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);
        initView();
    }

    public void initView() {

    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.content_bottom_sheet, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        int height = ((View) contentView.getParent()).getHeight() / 2;
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setPeekHeight(height);
            ((BottomSheetBehavior) behavior).setHideable(true);
        }

    }


    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

            switch (newState) {
                case BottomSheetBehavior.STATE_DRAGGING:
                    AppHelper.LogCat("state Dragging");
                    break;

                case BottomSheetBehavior.STATE_SETTLING:
                    AppHelper.LogCat("state Settling");
                    break;

                case BottomSheetBehavior.STATE_COLLAPSED:
                    AppHelper.LogCat("state Collapsed");

                    break;

                case BottomSheetBehavior.STATE_HIDDEN:
                    dismiss();
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    AppHelper.LogCat("state expended");

                    break;
            }


        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            AppHelper.LogCat("onSlide");
            bottomSheet.setNestedScrollingEnabled(false);
        }
    };

}