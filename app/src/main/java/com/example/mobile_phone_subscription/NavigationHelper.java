package com.example.mobile_phone_subscription;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;

/**
 * Segédosztály az oldalak közötti navigáció egységes kezelésére, animációkkal együtt.
 */
public class NavigationHelper {

    /**
     * Átnavigál a bejelentkezési oldalra, (MainActivity) animációval.
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     */
    public static void toMain(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);
        activity.startActivity(intent, options.toBundle());
    }

    /**
     * Átnavigál a regisztrációs oldalra (Register) animációval.
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     * @param secretKey A titkos kulcs, amit át kell adni.
     */
    public static void toRegister(Activity activity, int secretKey) {
        Intent intent = new Intent(activity, Register.class);
        intent.putExtra("SECRET_KEY", secretKey);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);
        activity.startActivity(intent, options.toBundle());
    }

    /**
     * Átnavigál a vásárlási oldalra (Shopping) animációval.
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     */
    public static void toShopping(Activity activity) {
        Intent intent = new Intent(activity, Shopping.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);
        activity.startActivity(intent, options.toBundle());
    }

    /**
     * Átnavigál a profil oldalra (Profile) animációval.
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     */
    public static void toProfile(Activity activity) {
        Intent intent = new Intent(activity, Profile.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);
        activity.startActivity(intent, options.toBundle());
    }

    /**
     * Átnavigál a csomag szerkesztő oldalra (PlanEditActivity) animációval.
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     * @param isNewPlan Új csomag létrehozása esetén true.
     */
    public static void toPlanEdit(Activity activity, boolean isNewPlan) {
        Intent intent = new Intent(activity, PlanEditActivity.class);
        intent.putExtra("IS_NEW_PLAN", isNewPlan);
        activity.startActivity(intent);
    }

    /**
     * Meglévő csomag szerkesztése esetén hívandó.
     * @param activity Az aktuális Activity.
     * @param plan A szerkesztendő Plan objektum.
     */
    public static void toPlanEdit(Activity activity, Plan plan) {
        Intent intent = new Intent(activity, PlanEditActivity.class);
        intent.putExtra("IS_NEW_PLAN", false);
        intent.putExtra("PLAN_ID", plan.getId());
        intent.putExtra("PLAN_NAME", plan.getName());
        intent.putExtra("PLAN_DETAILS", plan.getDetails());
        intent.putExtra("PLAN_PRICE", plan.getPrice());
        intent.putExtra("PLAN_IMAGE_URL", plan.getImageUrl());
        intent.putExtra("PLAN_DESCRIPTION", plan.getDescription());
        activity.startActivity(intent);
    }

    /**
     * Átnavigál a csomag információ oldalra (PlanInfoActivity) animációval.
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     * @param planId   A csomag azonosítója.
     * @param planName A csomag neve.
     * @param planDetails A csomag részletei.
     * @param planPrice A csomag ára.
     * @param planDescription A csomag leírása.
     */
    public static void toPlanInfo(Activity activity, String planId, String planName, String planDetails, int planPrice, String planDescription) {
        Intent intent = new Intent(activity, PlanInfoActivity.class);
        intent.putExtra("PLAN_ID", planId);
        intent.putExtra("PLAN_NAME", planName);
        intent.putExtra("PLAN_DETAILS", planDetails);
        intent.putExtra("PLAN_PRICE", planPrice);
        intent.putExtra("PLAN_DESCRIPTION", planDescription);
        activity.startActivity(intent);
    }

    /**
     * Átnavigál a vásárlási oldalra egyedi animációval (fade in/out).
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     */
    public static void toShoppingWithFade(Activity activity) {
        Intent intent = new Intent(activity, Shopping.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(
                activity, R.anim.fade_in, R.anim.fade_out);
        activity.startActivity(intent, options.toBundle());
    }
    /**
     * Átnavigál a profil oldalra egyedi animációval (fade in/out).
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     */
    public static void toMainWithFade(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(
                activity, R.anim.fade_in, R.anim.fade_out);
        activity.startActivity(intent, options.toBundle());
    }

    /**
     * Átnavigál a regisztrációs oldalra egyedi animációval (fade in/out).
     *
     * @param activity Az aktuális Activity, ahonnan a navigáció történik.
     * @param secretKey A titkos kulcs, amit át kell adni.
     */
    public static void toRegisterWithFade(Activity activity, int secretKey) {
        Intent intent = new Intent(activity, Register.class);
        intent.putExtra("SECRET_KEY", secretKey);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(
                activity, R.anim.fade_in, R.anim.fade_out);
        activity.startActivity(intent, options.toBundle());
    }
}