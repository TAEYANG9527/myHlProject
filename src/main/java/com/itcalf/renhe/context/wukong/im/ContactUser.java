package com.itcalf.renhe.context.wukong.im;

/**用户信息
 * Created by zhongqian.wzq on 2014/10/15.
 */
public class ContactUser {
	public String nickname;
	public String openid;
	public String secret;

	public ContactUser(String nickname, String openid, String secret) {
		this.nickname = nickname;
		this.openid = openid;
		this.secret = secret;
	}
}
