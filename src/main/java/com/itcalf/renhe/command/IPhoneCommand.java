package com.itcalf.renhe.command;

import android.content.Context;

import com.itcalf.renhe.dto.Version;

public interface IPhoneCommand {

	/**
	 * @param context
	 * @throws Exception
	 */
	Version getLastedVersion(Context context) throws Exception;

}
