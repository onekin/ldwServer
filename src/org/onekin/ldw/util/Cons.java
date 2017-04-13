package org.onekin.ldw.util;


//CONSTANTS
public class Cons {

	//Twitter 
	public static final Integer EQUALS= 0; 
	/**
     * UTF-8 encoding
     */
    public static final String UTF8 = "UTF-8";

    /**
     * Request parameter for the "page"
     */
    public static final String PAGE_PARAM = "page";
    public static final String REDIRECT_PARAM = "redirect";

    /**
     * Pages
     */
    public static final String base_BASEURL = "/org/onekin/ldw";
    public static final String base_RESOURCEPAGE = "/org/onekin/ldw/resources/";
    public static final String base_TEMPLATEPAGE = "/org/onekin/ldw/templates/";
    public static final String base_IMAGEPAGE = "/resources/img";
    public static final String base_CSSPAGE = "/resources/css";
    public static final String APIDOC_PAGE = base_TEMPLATEPAGE + "apidoc.vm";
    public static final String HEALTH_PAGE = base_TEMPLATEPAGE + "healthchecker.html";
    public static final String LDBROWSER_PAGE = base_TEMPLATEPAGE + "ldbrowser.vm";
    public static final String REDIRECT_HEALTH_PAGE = base_TEMPLATEPAGE + "healthredirect.html";
    public static final String CSS_PAGE = base_RESOURCEPAGE + "dt-healthchecker.css";
    public static final String PROJECT_PAGE = base_TEMPLATEPAGE + "project.vm";
    public static final String MYWRAPPERS_PAGE = base_TEMPLATEPAGE + "mywrappers.vm";
    public static final String REGISTERWRAPPER_PAGE = base_TEMPLATEPAGE + "registerwrapper.vm";
     public static final String MYENVIRONMENTS_PAGE = base_TEMPLATEPAGE + "myenvironments.vm";
     public static final String PUBLISH_PAGE = base_TEMPLATEPAGE + "publish.vm";
     public static final String PUBLISHREDIRECTION_PAGE = base_TEMPLATEPAGE + "publishredirect.vm";
      public static final String REDIRECT_PAGE = base_TEMPLATEPAGE + "loginredirect.vm";

    /**
     * health keys
     */
     public static final String RESOURCESURL = "#WEBURL#";
     public static final String WRAPPERDIAGNOSTICS = "#WRAPPERDIAGNOSTICS#";
     public static final String CSSFILE = "#CSSFILE#";
     public static final String BLANKS = "#BLANKS#";
     public static final String OKS = "#OKS#";
     public static final String FAILS = "#FAILS#";
     public static final String AUTHS = "#AUTHS#";
     public static final String LOWQUALITY = "#LOWQUALITY#";
     
     public static final String BLANKS_key = "blank";
     public static final String OKS_key = "ok";
     public static final String FAILS_key = "fail";
     public static final String AUTHS_key = "auth";
     public static final String LOWQUALITY_key = "siblings";
         
    public static final String LOWQUALITY_CODE= "20";
    public static final String OKS_CODE= "200";
    public static final String OKSXXX_CODE= "2";
    public static final String BLANKS_CODE= "404";
    public static final String BLANKSXXX_CODE= "4";
    public static final String FAILS_CODE= "500";
    public static final String FAILSXXX_CODE= "5";
     public static final String CRITICALERROR_CODE= "800";
    public static final String LIFTING_FAILS_CODE= "700";
    public static final String AUTHS_CODE= "401";
    /**
     * Keys
     */
    public static final String APIDOC_key = "apidocumentation";
    public static final String QUALITY_key = "dqv";
    public static final String VOID_key = "void";
    public static final String ENTRYPOINT_key = "entrypoint";
    public static final String OPERATIONLOOKUP_key = "operation/webservicelookup";
    public static final String CLASS_key = "class";
    public static final String CREDENTIALS_key = "credentials";
    public static final String LOGGED_KEY = "logged";
    public static final String REQENVIRONMENT_KEY = "request_token";
    public static final String USERDATA_KEY = "user_data";
    public static final String USERNAME_KEY = "user_name";
    public static final String USERACCOUNT_KEY = "user_account";
    public static final String USERID_KEY = "user_id";
     public static final String FILE = "file";
    public static final String PUBLISH = "publish";
    
	 public static final String CORRECTDEREFS = "correctderefs";
	 public static final String DEREFS = "derefs";
	 public static final String IPSCOUNT = "ipcounts";
	 public static final String IPS = "ips";
	 public static final String WRAPPER = "wrapper";
	 public static final String METADATA = "metadata";
	 public static final String QUALITYMETADATA = "quality";
	 public static final String ENVIRONMENTVALUE = "value";
    public static final String ENVIRONMENTUSER = "user";
    public static final String ENVIRONMENTWRAPPER = "wrapper";
    public static final String ENVIRONMENTID = "envid";
    public static final String NAME_KEY = "wrappername";
    
    public static final String WRAPPER_KEY = "wrapper";
    public static final String WRAPPERID_KEY = "wrapperid";
    public static final String WRAPPERNAME_KEY = "wrapperName";
    public static final String URIPATTERN_KEY = "uripattern";
    public static final String HASURIEXAMPLE_KEY = "hasURIExamples";
    public static final String URIEXAMPLELIST_KEY = "URIExamplesList";
    public static final String ISMATCHINGCORRECT_KEY = "isMatchingCorrect";
    public static final String MATCHINGPARAMS_KEY = "MatchingParams";
    public static final String LIFTINGEXISTS_KEY = "LiftingExists";
    		
    public static final String ISLOWERINGCORRECT_KEY = "isLoweringCorrect";
    public static final String GETMESSAGELOWERING_KEY = "getMessageLowering";
    public static final String ISLIFTINGCORRECT_KEY = "isLiftingCorrect";
    public static final String GETMESSAGELIFTING_KEY = "getMessageLifting";
    public static final String ISVOID_KEY = "isVoid";
    		
    public static final String ISSYNTACTICALLYCORRECT_KEY = "isSyntacticallyCorrect";
    public static final String ALIASMATCHING_KEY = "matchesAliases";
    public static final String INCORRECTALIASMATCHING_KEY = "incorrectalias";
    
    public static final String ISDEREFERENTIALLYCORRECT_KEY = "isDereferentiallyCorrect";
    public static final String ISBACKWARDCOMPATIBLE_KEY = "isBackwardCompatible";
    public static final String ISTHEFIRST = "isTheFirst";
    public static final String GETLOSTPROPERTIES = "getLostProperties";
    public static final String URIPattern = "URIPattern";
    
    public static final String WRAPPERNAME = "name";
    public static final String ENVIRONMENT = "token";
    public static final String BASEURL_KEY = "base_url";
    public static final String SERVLETPATH_KEY = "service";
    public static final String WRAPPERS_KEY = "WRAPPERS";
    public static final String WRAPPERTYPE_KEY = "type";
    public static final String ENVIRONMENT_KEY = "ENVIRONMENTS";
    public static final String MESSAGES_KEY = "MESSAGES";
    public static final String RESULT_KEY = "REGISTERINGRESULT";
    public static final String IMG_KEY = "IMGURL";
    public static final String CSS_KEY = "CSSURL";

    /**
     * Values
     */
    public static final String BACKDOOR_USER = "https://me.yahoo.com/a/sNV6f8w51pkJiRNUNt55N.POl5nFYACZ9to-";
    public static final String TRUE_VALUE = "true";
    public static final String FALSE_VALUE = "false";
    public static final String NULL_VALUE = null;
    public static final String NOPROVIDED = "N/A";
    public static final String VOID = "";
    public static final String PUBLISHER= "http://rdf.onekin.org";
    public static final String CONTRIBUTOR= "https://developer.yahoo.com";

    public static final String STATUS= "status";
    
    public static final String VARCODINGBEGIN= "{";
    public static final String VARCODINGEND= "}";
    public static final String URLCODINGBEGIN= "(";
    public static final String URLCODINGEND= ")";
    
    
}
