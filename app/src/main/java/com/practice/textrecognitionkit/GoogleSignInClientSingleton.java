package com.practice.textrecognitionkit;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class GoogleSignInClientSingleton {
    private static FirebaseAuth mAuth;
    private static GoogleSignInOptions gso;
    private static GoogleSignInClient mGoogleSignInClient;
    static GoogleSignInAccount account;

    /*
    1.FirebaseAuth：Firebase 身份驗證服務的入口，可以使用它來處理用戶的身份驗證，例如創建、註冊、登錄和登出用戶等。

    2.GoogleSignInOptions：定義了與 Google 登錄相關的選項，例如請求用戶授權哪些範圍、應用程序的客戶端 ID、重定向 URI 等。
    可以通過創建 GoogleSignInOptions 對象來配置這些選項，然後將其傳遞給 GoogleSignInClient 的構造函數來創建 GoogleSignInClient 對象。

    3.GoogleSignInClient：管理與 Google 登錄服務的交互，例如請求用戶授權、獲取授權令牌、獲取用戶資料等。
    可以使用 GoogleSignInClient 對象來創建用戶的 GoogleSignInAccount 對象、請求用戶授權、獲取用戶資料等。

    4.GoogleSignInAccount：代表已經通過 Google 登錄的用戶。可以使用 GoogleSignInAccount 對象來獲取用戶的資料、
    獲取用戶的授權令牌、判斷用戶是否已經通過 Google 登錄等。

    簡而言之，FirebaseAuth 用於處理用戶的身份驗證，GoogleSignInOptions 用於配置與 Google 登錄相關的選項，
    GoogleSignInClient 用於管理與 Google 登錄服務的交互，而 GoogleSignInAccount 則用於代表已經通過 Google 登錄的用戶。
    */

    // 私有構造方法
    private GoogleSignInClientSingleton() {}

    // 公有方法
    public static synchronized FirebaseAuth getFirebaseAuth() {
        if(mAuth==null){
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    // 公有方法
    public static synchronized GoogleSignInClient getGoogleSignInClient(Context context) {
        if(mGoogleSignInClient==null){
            gso =new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("1096904363111-mvd44v7dvtj6oungsh8l9o1do8uiuoc2.apps.googleusercontent.com")
                    .requestEmail()
                    .build();
            mGoogleSignInClient= GoogleSignIn.getClient(context, gso);
        }
        return mGoogleSignInClient;
    }
}
