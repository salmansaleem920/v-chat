/*
 * *
 *  * Created by Salman Saleem on 6/21/18 12:43 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 6/21/18 12:33 AM
 *
 */

package com.tcv.vassistchat.helpers.call;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;

import com.tcv.vassistchat.R;
import com.tcv.vassistchat.activities.call.CallActivity;
import com.tcv.vassistchat.activities.call.CallAlertActivity;
import com.tcv.vassistchat.animations.AnimationsUtil;
import com.tcv.vassistchat.app.AppConstants;
import com.tcv.vassistchat.app.WhatsCloneApplication;
import com.tcv.vassistchat.helpers.AppHelper;
import com.tcv.vassistchat.helpers.PermissionHandler;
import com.tcv.vassistchat.helpers.PreferenceManager;
import com.tcv.vassistchat.models.users.contacts.ContactsModel;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.socket.client.Ack;
import io.socket.client.Socket;

/**
 * Created by Salman Saleem on 12/21/16.
 *
 *
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype :
 */

public class CallManager {


    /**
     * method to call a user
     */
    public static void callContact(Activity mActivity, boolean isNeedFinish, boolean isVideoCall, int userID) {

        if (isVideoCall) {
            if (PermissionHandler.checkPermission(mActivity, Manifest.permission.CAMERA)) {

            } else {
                AppHelper.LogCat("Please request camera  permission.");
                PermissionHandler.requestPermission(mActivity, Manifest.permission.CAMERA);
            }

            if (PermissionHandler.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO)) {

            } else {
                AppHelper.LogCat("Please request Record audio permission.");
                PermissionHandler.requestPermission(mActivity, Manifest.permission.RECORD_AUDIO);
                return;
            }
        } else {

            if (PermissionHandler.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO)) {


            } else {
                AppHelper.LogCat("Please request Record audio permission.");
                PermissionHandler.requestPermission(mActivity, Manifest.permission.RECORD_AUDIO);
                return;
            }
        }


        if (!isNetworkAvailable(mActivity)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
            alert.setMessage(mActivity.getString(R.string.you_couldnt_call_this_user_network));
            alert.setPositiveButton(R.string.ok, (dialog, which) -> {
            });
            alert.setCancelable(false);
            alert.show();
        } else {
            WhatsCloneApplication app = (WhatsCloneApplication) mActivity.getApplication();
            Socket mSocket;
            mSocket = app.getSocket();
            if (mSocket != null) {
                if (!mSocket.connected())
                    mSocket.connect();
                if (mSocket.connected()) {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("recipientId", userID);
                        data.put("senderId", PreferenceManager.getID(mActivity));
                        AppHelper.LogCat("socket not null");
                        mSocket.emit(AppConstants.SOCKET_CALL_USER_PING, data, (Ack) argObjects -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance();
                            JSONObject dataString = (JSONObject) argObjects[0];
                            try {
                                boolean connected = dataString.getBoolean("connected");
                                String socketId = dataString.getString("socketId");
                                if (connected) {
                                    AppHelper.LogCat("User  connected and ready to call him connecteddd  " + socketId);
                                    realm.executeTransactionAsync(realm1 -> {
                                        ContactsModel contactsModel1 = realm1.where(ContactsModel.class).equalTo("id", userID).findFirst();
                                        contactsModel1.setSocketId(socketId);
                                        realm1.copyToRealmOrUpdate(contactsModel1);
                                    });
                                    makeCall(isNeedFinish, mActivity, socketId, isVideoCall, userID);
                                } else {
                                    AppHelper.LogCat("User  not connected and not ready to call him mess 2" + socketId);
                                    realm.executeTransactionAsync(realm1 -> {
                                        ContactsModel contactsModel1 = realm1.where(ContactsModel.class).equalTo("id", userID).findFirst();
                                        contactsModel1.setSocketId(null);
                                        realm1.copyToRealmOrUpdate(contactsModel1);
                                    });
                                    Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                                    mActivity.startActivity(mIntent);
                                    AnimationsUtil.setSlideInAnimation(mActivity);
                                }
                            } catch (JSONException e) {
                                Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                                mActivity.startActivity(mIntent);
                                AnimationsUtil.setSlideInAnimation(mActivity);
                            }
                            if (!realm.isClosed())
                                realm.close();
                        });
                    } catch (Exception e) {
                        AppHelper.LogCat("Exception" + e.getMessage());
                        Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                        mActivity.startActivity(mIntent);
                        AnimationsUtil.setSlideInAnimation(mActivity);
                    }
                } else {

                    Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                    mActivity.startActivity(mIntent);
                    AnimationsUtil.setSlideInAnimation(mActivity);
                }
            } else {
                Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                mActivity.startActivity(mIntent);
                AnimationsUtil.setSlideInAnimation(mActivity);
            }

        }
    }


    private static void makeCall(boolean isNeedFinish, Activity mActivity, String callerSocketId, boolean isVideoCall, int userID) {
        if (callerSocketId.isEmpty() || callerSocketId.equals(PreferenceManager.getSocketID(mActivity))) {
            return;
        }
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance();
        ContactsModel contactsModelCaller = realm.where(ContactsModel.class).equalTo("id", PreferenceManager.getID(mActivity)).findFirst();
        ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", userID).findFirst();
        String recipientPhone = contactsModel.getPhone();

        String callerImage = null;
        if (contactsModelCaller.getImage() != null)
            callerImage = contactsModel.getImage();

        String userImage = null;
        if (contactsModelCaller.getImage() != null)
            userImage = contactsModelCaller.getImage();

        Intent intent = new Intent(mActivity, CallActivity.class);
        intent.putExtra(AppConstants.USER_SOCKET_ID, PreferenceManager.getSocketID(mActivity));
        intent.putExtra(AppConstants.USER_PHONE, PreferenceManager.getPhone(mActivity));
        intent.putExtra(AppConstants.CALLER_SOCKET_ID, callerSocketId);
        intent.putExtra(AppConstants.CALLER_PHONE, recipientPhone);
        intent.putExtra(AppConstants.CALLER_IMAGE, callerImage);
        intent.putExtra(AppConstants.USER_IMAGE, userImage);
        intent.putExtra(AppConstants.IS_ACCEPTED_CALL, false);
        intent.putExtra(AppConstants.IS_VIDEO_CALL, isVideoCall);
        intent.putExtra(AppConstants.CALLER_ID, userID);
        mActivity.startActivity(intent);
        if (isNeedFinish)
            mActivity.finish();
        AnimationsUtil.setSlideInAnimation(mActivity);
        realm.close();
    }

    private static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
