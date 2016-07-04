package com.itcalf.renhe.command;

import android.content.Context;

import com.itcalf.renhe.dto.ContactProfile;
import com.itcalf.renhe.dto.Profile;

public interface IProfileCommand {

	Profile showProfile(String viewSId, String sid, String adSId, Context context) throws Exception;

	ContactProfile showContactProfile(String viewSId, String sid, String adSId, Context context) throws Exception;

	Profile showIMNoFriendsProfile(String openId, String sid, String adSId, Context context) throws Exception;

	ContactProfile showIMContactProfile(String openId, String sid, String adSId, Context context) throws Exception;

}
