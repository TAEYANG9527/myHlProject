package com.itcalf.renhe.database.sqlite;

public class TablesConstant {

    /**
     * 用户表
     */
    public static final String USER_TABLE = "RHLOGINUSER_HL_V1";
    public static final String USER_TABLE_COLUMN_USERID = "USERID";
    public static final String USER_TABLE_COLUMN_EMAIL = "EMAIL";
    public static final String USER_TABLE_COLUMN_NAME = "NAME";
    public static final String USER_TABLE_COLUMN_PASSWORD = "PASSWORD";
    public static final String USER_TABLE_COLUMN_REMEMBER = "REMEMBER";
    public static final String USER_TABLE_COLUMN_USERFACE = "USERFACE";
    public static final String USER_TABLE_COLUMN_LOGINTIME = "LOGINTIME";
    public static final String USER_TABLE_COLUMN_MOBILE = "MOBILE";
    public static final String USER_TABLE_COLUMN_LOGINACCOUNT = "LOGINACCOUNT";// 新增，用来标识用户登录是使用手机还是邮箱
    public static final String USER_TABLE_COLUMN_SID = "SID";// 会员加密后的id用于请求档案等一些页面
    public static final String USER_TABLE_COLUMN_ADSID = "ADSID";// 加密后用户id和密码的信息 以后的每次请求中都要带上它
    public static final String USER_TABLE_COLUMN_TITLE = "TITLE";// 加密后用户id和密码的信息 以后的每次请求中都要带上它
    public static final String USER_TABLE_COLUMN_COMPANY = "COMPANY";// 加密后用户id和密码的信息 以后的每次请求中都要带上它
    public static final String USER_TABLE_COLUMN_IMVALID = "IMVALID";// 加密后用户id和密码的信息 以后的每次请求中都要带上它
    public static final String USER_TABLE_COLUMN_IMOPENID = "IMOPENID";// 加密后用户id和密码的信息 以后的每次请求中都要带上它
    public static final String USER_TABLE_COLUMN_LOCATION = "LOCATION";//地址

    public static final String USER_TABLE_COLUMN_PRO = "PRO";//省份
    public static final String USER_TABLE_COLUMN_CITY = "CITY";//城市
    public static final String USER_TABLE_COLUMN_INDUSTRY = "INDUSTRY";//行业
    public static final String USER_TABLE_COLUMN_ACCOUNT_TYPE = "ACCOUNT_TYPE";//会员类型，vip，铂金等
    public static final String USER_TABLE_COLUMN_REALNAME = "REALNAME";//是否是实名认证会员

    //把email 改成  SID 为主键，modify by 2015.4.10
    public static final String SQL_USER = "CREATE TABLE if not exists " + USER_TABLE + "(" + USER_TABLE_COLUMN_EMAIL
            + " TEXT UNIQUE, " + USER_TABLE_COLUMN_USERID + " NUMERIC NOT NULL, " + USER_TABLE_COLUMN_PASSWORD + " TEXT, "
            + USER_TABLE_COLUMN_NAME + " TEXT NOT NULL, " + USER_TABLE_COLUMN_REMEMBER + " NUMERIC NOT NULL DEFAULT 0, "
            + USER_TABLE_COLUMN_USERFACE + " TEXT, " + USER_TABLE_COLUMN_LOGINTIME + " TEXT, " + USER_TABLE_COLUMN_MOBILE
            + " TEXT UNIQUE, " + USER_TABLE_COLUMN_SID + " TEXT PRIMARY KEY NOT NULL, " + USER_TABLE_COLUMN_ADSID
            + " TEXT UNIQUE, " + USER_TABLE_COLUMN_TITLE + " TEXT, " + USER_TABLE_COLUMN_COMPANY + " TEXT, "
            + USER_TABLE_COLUMN_IMVALID + " NUMERIC NOT NULL DEFAULT 0, " + USER_TABLE_COLUMN_IMOPENID + " NUMERIC, "
            + USER_TABLE_COLUMN_LOGINACCOUNT + " TEXT NOT NULL," + USER_TABLE_COLUMN_LOCATION + " TEXT," + USER_TABLE_COLUMN_PRO
            + " INTEGER," + USER_TABLE_COLUMN_CITY + " INTEGER," + USER_TABLE_COLUMN_INDUSTRY + " TEXT,"
            + USER_TABLE_COLUMN_ACCOUNT_TYPE + " INTEGER," + USER_TABLE_COLUMN_REALNAME + " NUMERIC DEFAULT 0)";
    /**
     * 联系人表
     */
    public static final String CONTACT_TABLE = "CONTACT_ADDVIP_ADDIM_ADDBLOCK_HL";
    public static final String CONTACT_TABLE_COLUMN_ID = "ID";
    public static final String CONTACT_TABLE_COLUMN_NAME = "NAME";
    public static final String CONTACT_TABLE_COLUMN_EMAIL = "EMAIL";//email字段
    public static final String CONTACT_TABLE_COLUMN_MYSID = "MYSID";
    public static final String CONTACT_TABLE_COLUMN_CONTACTFACE = "CONTACTFACE";
    public static final String CONTACT_TABLE_COLUMN_JOB = "JOB";
    public static final String CONTACT_TABLE_COLUMN_COMPANY = "COMPANY";
    public static final String CONTACT_TABLE_COLUMN_ACCOUNTTYPE = "ACCOUNTTYPE";
    public static final String CONTACT_TABLE_COLUMN_REALNAME = "REALNAME";
    public static final String CONTACT_TABLE_COLUMN_BLOCKEDCONTACT = "BLOCKEDCONTACT";
    public static final String CONTACT_TABLE_COLUMN_BEBLOCKED = "BEBLOCKED";
    public static final String CONTACT_TABLE_COLUMN_ISIMVALID = "IMVALID";
    public static final String CONTACT_TABLE_COLUMN_IMID = "IMID";
    public static final String CONTACT_TABLE_COLUMN_MOBILE = "MOBILE";
    public static final String CONTACT_TABLE_COLUMN_TEL = "TEL";

    public static final String CONTACT_TABLE_COLUMN_CARDID = "CARDID";
    public static final String CONTACT_TABLE_COLUMN_VCARDCONTENT = "VCARDCONTENT";
    public static final String CONTACT_TABLE_COLUMN_PROFILESOURCETYPE = "PROFILESOURCETYPE";
    public static final String CONTACT_TABLE_COLUMN_SHORTNAME = "SHORTNAME";
    public static final String CONTACT_TABLE_COLUMN_COLORINDEX = "COLORINDEX";

    public static final String SQL_CONTACT = "CREATE TABLE if not exists " + CONTACT_TABLE + "(" + CONTACT_TABLE_COLUMN_ID
            + " TEXT, " + CONTACT_TABLE_COLUMN_NAME + " NOT NULL, " + CONTACT_TABLE_COLUMN_MYSID + " TEXT NOT NULL, "
            + CONTACT_TABLE_COLUMN_CONTACTFACE + " NULL, " + CONTACT_TABLE_COLUMN_JOB + " TEXT, " + CONTACT_TABLE_COLUMN_COMPANY
            + " TEXT," + CONTACT_TABLE_COLUMN_ACCOUNTTYPE + " INTEGER," + CONTACT_TABLE_COLUMN_REALNAME + " INTEGER,"
            + CONTACT_TABLE_COLUMN_BLOCKEDCONTACT + " INTEGER," + CONTACT_TABLE_COLUMN_BEBLOCKED + " INTEGER,"
            + CONTACT_TABLE_COLUMN_ISIMVALID + " INTEGER," + CONTACT_TABLE_COLUMN_IMID + " INTEGER," + CONTACT_TABLE_COLUMN_MOBILE
            + " TEXT," + CONTACT_TABLE_COLUMN_TEL + " TEXT," + CONTACT_TABLE_COLUMN_CARDID + " INTEGER,"
            + CONTACT_TABLE_COLUMN_VCARDCONTENT + " TEXT," + CONTACT_TABLE_COLUMN_PROFILESOURCETYPE + " INTEGER,"
            + CONTACT_TABLE_COLUMN_EMAIL + " TEXT,"
            + CONTACT_TABLE_COLUMN_SHORTNAME + " TEXT," + CONTACT_TABLE_COLUMN_COLORINDEX + " INTEGER)";

    /**
     * 保存联系人列表是否成功 根据email 判断  Obsolete to 2015.4.13; 先更新为 根据sid 去判定
     */
    public static final String CONTACTISSAVE_TABLE = "CONTACT_ISSAVE_ADDIM_ADDBLOCK_HL";
    //	public static final String CONTACTISSAVE_EMAIL = "EMAIL";
    public static final String CONTACTISSAVE_SID = "SID";
    public static final String CONTACTISSAVE_MAXCID = "MAXCID";
    public static final String CONTACTISSAVE_MAXLASTUPDATEDDATE = "MAXLASTUPDATEDDATE";
    public static final String CONTACTISSAVE_MAXMOBILEID = "MAXMOBILEID";
    public static final String CONTACTISSAVE_MAXCARDID = "MAXCARDID";
    public static final String SQL_CONTACTISSAVE = "CREATE TABLE if not exists " + CONTACTISSAVE_TABLE + "(" + CONTACTISSAVE_SID
            + " TEXT PRIMARY KEY, " + CONTACTISSAVE_MAXCID + " INTEGER, " + CONTACTISSAVE_MAXLASTUPDATEDDATE + " LONG,"
            + CONTACTISSAVE_MAXMOBILEID + " INTEGER, " + CONTACTISSAVE_MAXCARDID + " INTEGER)";

    /**
     * Email 联系人表
     */
    public static final String EMAIL_CONTACT_TABLE = "EMAIL_CONTACTS";
    //	public static final String EMAIL_CONTACT_TABLE_COLUMN_MYEMAIL = "MYEMAIL";//我的邮箱，作为身份标识
    public static final String EMAIL_CONTACT_TABLE_COLUMN_MYSID = "MYSID";//我的邮箱，作为身份标识
    public static final String EMAIL_CONTACT_TABLE_COLUMN_CREATETIME = "CREATETIME";//保存时间
    public static final String EMAIL_CONTACT_TABLE_COLUMN_ID = "OUTLOOKID";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_NAME = "NAME";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_EMAIL = "EMAIL";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_ISRENHEMEMBER = "ISRENHEMEMBER";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_MEMBERSID = "MEMBERSID";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_ISSELF = "ISSELF";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_ISCONNECTION = "ISCONNECTION";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_ISINVITE = "ISINVITE";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_ISBEINVITED = "ISBEINVITED";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_INVITEID = "INVITEID";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_INVITETYPE = "INVITETYPE";
    public static final String EMAIL_CONTACT_TABLE_COLUMN_AVATAR = "AVATAR";//新增字段头像
    public static final String EMAIL_CONTACT_TABLE_COLUMN_COMPANY = "COMPANY";//公司
    public static final String EMAIL_CONTACT_TABLE_COLUMN_POSITION = "POSITION";//职位

    public static final String SQL_EMAIL_CONTACT = "CREATE TABLE if not exists " + EMAIL_CONTACT_TABLE + "("
            + EMAIL_CONTACT_TABLE_COLUMN_MYSID + " TEXT NOT NULL, " + EMAIL_CONTACT_TABLE_COLUMN_CREATETIME + " TEXT, "
            + EMAIL_CONTACT_TABLE_COLUMN_ID + " INTEGER, " + EMAIL_CONTACT_TABLE_COLUMN_NAME + " TEXT , "
            + EMAIL_CONTACT_TABLE_COLUMN_EMAIL + " TEXT NOT NULL, " + EMAIL_CONTACT_TABLE_COLUMN_ISRENHEMEMBER + " INTEGER, "
            + EMAIL_CONTACT_TABLE_COLUMN_MEMBERSID + " TEXT, " + EMAIL_CONTACT_TABLE_COLUMN_ISSELF + " INTEGER,"
            + EMAIL_CONTACT_TABLE_COLUMN_ISCONNECTION + " INTEGER," + EMAIL_CONTACT_TABLE_COLUMN_ISINVITE + " INTEGER,"
            + EMAIL_CONTACT_TABLE_COLUMN_ISBEINVITED + " INTEGER," + EMAIL_CONTACT_TABLE_COLUMN_INVITEID + " INTEGER,"
            + EMAIL_CONTACT_TABLE_COLUMN_INVITETYPE + " INTEGER," + EMAIL_CONTACT_TABLE_COLUMN_AVATAR + " TEXT,"
            + EMAIL_CONTACT_TABLE_COLUMN_COMPANY + " TEXT," + EMAIL_CONTACT_TABLE_COLUMN_POSITION + " TEXT)";

    /**
     * Mobile 联系人表
     */
    public static final String MOBILE_CONTACT_TABLE = "MOBILE_CONTACTS";
    //	public static final String MOBILE_CONTACT_TABLE_COLUMN_MYEMAIL = "MYEMAIL";//我的邮箱，作为身份标识
    public static final String MOBILE_CONTACT_TABLE_COLUMN_MYSID = "MYSID";//我的SID，作为身份标识
    public static final String MOBILE_CONTACT_TABLE_COLUMN_MAXID = "MAXID";//最后一次取数据的maxid
    public static final String MOBILE_CONTACT_TABLE_COLUMN_ID = "CONTACTID";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_NAME = "NAME";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_ISRENHEMEMBER = "ISRENHEMEMBER";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_MEMBERSID = "MEMBERSID";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_ISSELF = "ISSELF";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_ISCONNECTION = "ISCONNECTION";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_ISINVITE = "ISINVITE";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_ISBEINVITED = "ISBEINVITED";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_INVITEID = "INVITEID";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_INVITETYPE = "INVITETYPE";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_EMAILITEMS = "EMAILITEMS";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_MOBILEITEMS = "MOBILEITEMS";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_TELEPHONEITEMS = "TELEPHONEITEMS";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_TELEPHONEOTHERITEMS = "TELEPHONEOTHERITEMS";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_ADDRESSITEMS = "ADDRESSITEMS";
    public static final String MOBILE_CONTACT_TABLE_COLUMN_OTHERITEMS = "OTHERITEMS";
    //新增字段 add V5.3.2  2015.10.22
    public static final String MOBILE_CONTACT_TABLE_COLUMN_AVATAR = "AVATAR";//新增字段头像
    public static final String MOBILE_CONTACT_TABLE_COLUMN_COMPANY = "COMPANY";//公司
    public static final String MOBILE_CONTACT_TABLE_COLUMN_POSITION = "POSITION";//职位


    public static final String SQL_MOBILE_CONTACT = "CREATE TABLE if not exists " + MOBILE_CONTACT_TABLE + "("
            + MOBILE_CONTACT_TABLE_COLUMN_MYSID + " TEXT NOT NULL, " + MOBILE_CONTACT_TABLE_COLUMN_MAXID + " INTEGER, "
            + MOBILE_CONTACT_TABLE_COLUMN_ID + " TEXT , " + MOBILE_CONTACT_TABLE_COLUMN_NAME + " TEXT , "
            + MOBILE_CONTACT_TABLE_COLUMN_ISRENHEMEMBER + " INTEGER, " + MOBILE_CONTACT_TABLE_COLUMN_MEMBERSID + " TEXT , "
            + MOBILE_CONTACT_TABLE_COLUMN_ISSELF + " INTEGER," + MOBILE_CONTACT_TABLE_COLUMN_ISCONNECTION + " INTEGER,"
            + MOBILE_CONTACT_TABLE_COLUMN_ISINVITE + " INTEGER," + MOBILE_CONTACT_TABLE_COLUMN_ISBEINVITED + " INTEGER,"
            + MOBILE_CONTACT_TABLE_COLUMN_INVITEID + " INTEGER," + MOBILE_CONTACT_TABLE_COLUMN_INVITETYPE + " INTEGER,"
            + MOBILE_CONTACT_TABLE_COLUMN_EMAILITEMS + " TEXT , " + MOBILE_CONTACT_TABLE_COLUMN_MOBILEITEMS + " TEXT , "
            + MOBILE_CONTACT_TABLE_COLUMN_TELEPHONEITEMS + " TEXT , " + MOBILE_CONTACT_TABLE_COLUMN_TELEPHONEOTHERITEMS
            + " TEXT , " + MOBILE_CONTACT_TABLE_COLUMN_ADDRESSITEMS + " TEXT , " + MOBILE_CONTACT_TABLE_COLUMN_OTHERITEMS
            + " TEXT," + MOBILE_CONTACT_TABLE_COLUMN_AVATAR + " TEXT,"
            + MOBILE_CONTACT_TABLE_COLUMN_COMPANY + " TEXT," + MOBILE_CONTACT_TABLE_COLUMN_POSITION + " TEXT)";
    // public static final String SETTINGS_TABLE = "USER_SETTINGS";
    // public static final String SETTINGS_TABLE_COLUMN_TONE = "TONE";
    // public static final String SETTINGS_TABLE_COLUMN_PUSHCOMMENT =
    // "PUSHCOMMENT";
    // public static final String SETTINGS_TABLE_COLUMN_PUSHATME = "PUSHATME";
    // public static final String SETTINGS_TABLE_COLUMN_PUSHFANS = "PUSHFANS";
    //
    // public static final String SQL_SETTINGS = "CREATE TABLE " +
    // SETTINGS_TABLE
    // + "(" + USER_TABLE_COLUMN_USERNAME + " TEXT PRIMARY KEY NOT NULL, "
    // + SETTINGS_TABLE_COLUMN_TONE + " NUMERIC DEFAULT 0, "
    // + SETTINGS_TABLE_COLUMN_PUSHCOMMENT + " NUMERIC DEFAULT 0, "
    // + SETTINGS_TABLE_COLUMN_PUSHATME + " NUMERIC DEFAULT 0, "
    // + SETTINGS_TABLE_COLUMN_PUSHFANS + " NUMERIC DEFAULT 0)";

}
