package com.sdw.ivana.inertiavectorrace;

/**
 * Created by aurel on 5/20/2016.
 */
public interface Defaults {

    //Backendless keys
    String APPLICATION_ID = "A3E3A351-5092-8CA8-FF21-0AEF76A3B500";
    String SECRET_KEY = "9AD0905E-BA14-AF64-FF55-C2C6CC406600";
    String VERSION = "v1";
    String SERVER_URL = "http://api.backendless.com";

    //Google SignIn keys
    String SERVER_CLIENT_ID_DEBUG = "aqui_va_su_id_de_cliente_para_la_version_.debug";
    String SERVER_CLIENT_ID_RELEASE ="aqui_va_su_id_de_cliente_para_la_version_release";

    int SIGNED_OUT = 0;
    int GUEST_SIGNIN = 1;
    int GOOGLE_SIGNIN = 2;
    int INERTIA_SIGNIN = 3;
    int RC_LOGIN_SCREEN = 1000;
    int RC_ENTER_GAME = 1001;
}
