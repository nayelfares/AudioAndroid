package com.media.myapplication;

public class SpinnerData {
    private String mText;
    private int mId;

    public SpinnerData(String text, int id) {
        mText = text;
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    @Override
    public String toString() {
        return mText;
    }
}

